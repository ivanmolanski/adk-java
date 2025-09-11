"""Chat Endpoint providing AIManager persona interaction and optional viral research invocation."""
from __future__ import annotations

from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
import logging
from datetime import datetime

from app.services.ai_client import AIClient
from app.repositories.viral_repositories import LearningStore, LearningRepository
from app.services.compliance import apply_compliance
from app.models.database import get_session
from sqlalchemy.ext.asyncio import AsyncSession
from app.agents.trend_analyzer import TrendAnalyzer, ViralPostData

logger = logging.getLogger(__name__)

chat_router = APIRouter()

SYSTEM_PROMPT = (
  "You are AIManager, an AI Command Center for a high-end medical aesthetics practice (MD Aesthetics). "
  "Tone: professional, clinical, authoritative, reassuring. You may be asked to analyze social content, suggest compliant hooks, or generate educational explanations. "
  "NEVER use the word 'Botox' – prefer 'Tox', 'Neuromodulator', or 'Neurotoxin'. If user asks for pricing, respond that consultation is required."
)

class ChatMessage(BaseModel):
  role: str
  content: str

class ChatRequest(BaseModel):
  messages: List[ChatMessage] = Field(..., description="Conversation history including user message (last).")
  model: Optional[str] = None
  invoke_research: bool = False
  research_posts: Optional[List[Dict[str, Any]]] = None  # raw posts if provided ad hoc
  limit_learning: int = 10

class ChatResponse(BaseModel):
  reply: str
  model_used: str
  research: Optional[Dict[str, Any]] = None
  used_learning_items: int
  timestamp: datetime = Field(default_factory=datetime.utcnow)

async def _perform_inline_research(raw_posts: List[Dict[str, Any]], session: AsyncSession) -> Dict[str, Any]:
  analyzer = TrendAnalyzer()
  posts: List[ViralPostData] = []
  for p in raw_posts:
    try:
      posts.append(ViralPostData(
        id=str(p.get('id') or p.get('post_id') or p.get('url') or f"inline-{len(posts)}"),
        platform=p.get('platform','unknown'),
        profile=p.get('profile','unknown'),
        caption=p.get('caption',''),
        hashtags=p.get('hashtags',[]) or [],
        engagement_rate=float(p.get('engagement_rate',0)),
        likes=int(p.get('likes',0)),
        comments=int(p.get('comments',0)),
        shares=int(p.get('shares',0) or 0),
        views=int(p.get('views',0) or 0),
        post_url=p.get('post_url') or p.get('url') or '',
        scraped_at=p.get('scraped_at') or datetime.utcnow()
      ))
    except Exception as exc:  # noqa: BLE001
      logger.warning("Skipping malformed post for inline research: %s", exc)
  heuristics = analyzer.analyze_batch(posts)
  analyses_payload = []
  posts_payload = [p.model_dump() for p in posts]
  for h in heuristics:
    analyses_payload.append({
      'post_id': h.post_id,
      'hook': h.hook,
      'cta': h.cta,
      'content_category': h.content_category.value if hasattr(h.content_category,'value') else str(h.content_category),
      'relevance_score': h.relevance_score,
      'virality_score': h.virality_score,
      'summary': h.summary,
      'key_themes': h.key_themes,
      'engagement_factors': h.engagement_factors,
      'compliance_notes': h.compliance_notes,
      'analyzed_at': h.analyzed_at
    })
  repo = LearningRepository(session)
  await repo.persist_batch(posts_payload, analyses_payload, drafts=[])
  await session.commit()
  return {
    'count': len(heuristics),
    'analyses': analyses_payload
  }

@chat_router.post('/chat', response_model=ChatResponse)
async def chat(request: ChatRequest, session: AsyncSession = Depends(get_session)) -> ChatResponse:
  if not request.messages:
    raise HTTPException(status_code=400, detail="No messages provided")
  ai = AIClient()
  # Increment chat_requests early for visibility (even if AI fails)
  try:
    import main  # type: ignore
    if hasattr(main, 'app') and hasattr(main.app.state, 'metrics'):
      main.app.state.metrics['chat_requests'] += 1
  except Exception:  # noqa: BLE001
    pass
  # Retrieve learning tail
  learning_items = await LearningStore.tail(request.limit_learning)
  learning_context = ''
  if learning_items:
    # build concise context summary
    snippets = []
    for item in learning_items:
      if item.get('type') == 'analysis':
        snippets.append(f"Post {item.get('post_id')}: hook={item.get('hook')} cat={item.get('content_category')} summary={item.get('summary')}")
    learning_context = '\n'.join(snippets[-request.limit_learning:])
  research_block = None
  if request.invoke_research:
    raw_posts = request.research_posts or []
    if not raw_posts:
      # If no posts provided, we simply note that no posts were supplied.
      research_block = {'warning':'invoke_research true but no research_posts provided'}
    else:
      try:
        research_block = await _perform_inline_research(raw_posts, session)
      except Exception as exc:  # noqa: BLE001
        logger.error("Inline research failed: %s", exc)
        research_block = {'error': str(exc)}
  # Compose messages for model
  model_messages = [ { 'role': 'system', 'content': SYSTEM_PROMPT } ]
  if learning_context:
    model_messages.append({'role':'system','content': f"Recent learned analyses (most recent last):\n{learning_context}"})
  if research_block:
    model_messages.append({'role':'system','content': f"Fresh inline research results: {research_block}"})
  for m in request.messages:
    model_messages.append({'role': m.role, 'content': m.content})
  ai_success = False
  try:
    raw = await ai.generate(messages=model_messages, model=request.model)
    ai_success = True
  except PermissionError as exc:  # explicit auth/config issue
    logger.error("AI generation permission error: %s", exc)
    # metrics: auth_failures
    try:
      import main  # type: ignore
      if hasattr(main, 'app') and hasattr(main.app.state, 'metrics'):
        main.app.state.metrics['auth_failures'] += 1
    except Exception:  # noqa: BLE001
      pass
    await ai.close()
    raise HTTPException(status_code=401, detail=str(exc))
  except Exception as exc:  # noqa: BLE001
    logger.error("AI generation failed: %s", exc)
    # heuristics: detect rate limit vs other upstream
    msg_lower = str(exc).lower()
    try:
      import main  # type: ignore
      if hasattr(main, 'app') and hasattr(main.app.state, 'metrics'):
        if 'rate limit' in msg_lower or '429' in msg_lower:
          main.app.state.metrics['rate_limit_events'] += 1
        else:
          main.app.state.metrics['upstream_failures'] += 1
    except Exception:  # noqa: BLE001
      pass
    await ai.close()
    # Upstream errors after retries exhausted -> 502 Bad Gateway semantic
    raise HTTPException(status_code=502, detail=f"AI upstream failure: {exc}")
  finally:
    if not ai_success:
      try:
        await ai.close()
      except Exception:  # noqa: BLE001
        pass
  reply_text = raw.strip()
  compliance_result = apply_compliance(reply_text)
  # Update metrics if available
  try:  # best-effort, do not fail chat if metrics missing
    from fastapi import Request  # local import to avoid circular
  except Exception:  # noqa: BLE001
    Request = None  # type: ignore
  # FastAPI dependency injection not used here; we access global app via logger hierarchy if needed
  # Instead we increment via global variable on LearningStore (not ideal but simple). We'll attempt to find running app.
  try:
    import main  # type: ignore
    if hasattr(main, 'app') and hasattr(main.app.state, 'metrics') and ai_success:
      main.app.state.metrics['ai_calls'] += 1
  except Exception:  # noqa: BLE001
    pass
  return ChatResponse(
    reply=compliance_result['compliant'],
    model_used=request.model or ai.default_model,
    research=research_block,
    used_learning_items=len(learning_items)
  )
