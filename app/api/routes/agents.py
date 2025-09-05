from fastapi import APIRouter, HTTPException
from typing import Dict, Any, List, Optional
import logging

from ...models.schemas import (
    AgentRequest, AgentResponse,
    TrendAnalysisRequest, TrendAnalysisResponse,
    ContentCreationRequest, ContentCreationResponse,
    ComplianceCheckRequest, ComplianceCheckResponse
)
from ...agents.trend_analyzer import TrendAnalyzerAgent
from ...agents.content_creator import ContentCreatorAgent  
from ...agents.compliance_agent import ComplianceAgent

router = APIRouter()
logger = logging.getLogger(__name__)

# Initialize agents
trend_analyzer = TrendAnalyzerAgent()
content_creator = ContentCreatorAgent()
compliance_agent = ComplianceAgent()

@router.get("/")
async def list_agents() -> Dict[str, Any]:
    """List available agents and their capabilities"""
    return {
        "agents": {
            "trend_analyzer": {
                "name": "TrendAnalyzer",
                "description": "Analyzes viral social media posts to identify trends and hooks",
                "capabilities": [
                    "Extract hooks and CTAs",
                    "Categorize content",
                    "Calculate virality scores",
                    "Generate recommendations"
                ],
                "endpoint": "/agents/trend-analyzer"
            },
            "content_creator": {
                "name": "ContentCreator", 
                "description": "Creates superior MD Aesthetics content based on viral trend analysis",
                "capabilities": [
                    "Generate MD Aesthetics branded content",
                    "Ensure compliance with brand guidelines",
                    "Create multiple content variations",
                    "Calculate brand voice scores"
                ],
                "endpoint": "/agents/content-creator"
            },
            "compliance_agent": {
                "name": "ComplianceAgent",
                "description": "Ensures content compliance with MD Aesthetics guidelines and regulations",
                "capabilities": [
                    "Check forbidden words",
                    "Validate content structure", 
                    "Verify brand voice alignment",
                    "Auto-fix common issues"
                ],
                "endpoint": "/agents/compliance"
            }
        }
    }

@router.post("/trend-analyzer", response_model=TrendAnalysisResponse)
async def run_trend_analyzer(request: TrendAnalysisRequest):
    """Run trend analysis agent"""
    try:
        result = await trend_analyzer.execute(request)
        return TrendAnalysisResponse(**result)
    except Exception as e:
        logger.error(f"TrendAnalyzer error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Trend analysis failed: {str(e)}")

@router.post("/content-creator", response_model=ContentCreationResponse) 
async def run_content_creator(request: ContentCreationRequest):
    """Run content creation agent"""
    try:
        result = await content_creator.execute(request)
        return ContentCreationResponse(**result)
    except Exception as e:
        logger.error(f"ContentCreator error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Content creation failed: {str(e)}")

@router.post("/compliance", response_model=ComplianceCheckResponse)
async def run_compliance_check(request: ComplianceCheckRequest):
    """Run compliance checking agent"""
    try:
        result = await compliance_agent.execute(request)
        return ComplianceCheckResponse(**result)
    except Exception as e:
        logger.error(f"ComplianceAgent error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Compliance check failed: {str(e)}")

@router.get("/trend-analyzer/state/{session_id}")
async def get_trend_analyzer_state(session_id: str):
    """Get trend analyzer agent state"""
    state = trend_analyzer.get_state(session_id)
    if not state:
        raise HTTPException(status_code=404, detail="Session not found")
    return state.dict()

@router.get("/content-creator/state/{session_id}")
async def get_content_creator_state(session_id: str):
    """Get content creator agent state"""
    state = content_creator.get_state(session_id)
    if not state:
        raise HTTPException(status_code=404, detail="Session not found")
    return state.dict()

@router.get("/compliance/state/{session_id}")
async def get_compliance_state(session_id: str):
    """Get compliance agent state"""
    state = compliance_agent.get_state(session_id)
    if not state:
        raise HTTPException(status_code=404, detail="Session not found")
    return state.dict()

@router.delete("/state/{session_id}")
async def clear_agent_state(session_id: str):
    """Clear agent state for all agents"""
    trend_analyzer.clear_state(session_id)
    content_creator.clear_state(session_id)
    compliance_agent.clear_state(session_id)
    
    return {"message": f"Cleared state for session {session_id}"}

@router.post("/pipeline/analyze-create-check")
async def agent_pipeline(
    post_data: dict,
    target_services: List[str] = ["Duo-C-Lift", "SkinTyte", "Radiesse"],
    tone: str = "educational",
    session_id: Optional[str] = None
):
    """Run complete agent pipeline: analyze -> create -> check"""
    
    try:
        # Import here to avoid circular imports
        from ...models.schemas import CompetitorPostCreate, PostAnalysis
        
        # Step 1: Trend Analysis
        post_create = CompetitorPostCreate(**post_data)
        analysis_request = TrendAnalysisRequest(
            post_data=post_create,
            session_id=session_id
        )
        
        analysis_result = await trend_analyzer.execute(analysis_request, session_id)
        
        if not analysis_result["success"]:
            return {
                "success": False,
                "error": "Trend analysis failed",
                "details": analysis_result
            }
        
        # Step 2: Content Creation
        post_analysis = PostAnalysis(**analysis_result["data"]["analysis"])
        creation_request = ContentCreationRequest(
            trend_analysis=post_analysis,
            target_services=target_services,
            tone=tone,
            session_id=session_id
        )
        
        creation_result = await content_creator.execute(creation_request, session_id)
        
        if not creation_result["success"]:
            return {
                "success": False,
                "error": "Content creation failed", 
                "details": creation_result
            }
        
        # Step 3: Compliance Check
        from ...models.schemas import GeneratedContentCreate
        generated_content = GeneratedContentCreate(**creation_result["data"]["generated_content"])
        compliance_request = ComplianceCheckRequest(
            content=generated_content,
            strict_mode=False,
            session_id=session_id
        )
        
        compliance_result = await compliance_agent.execute(compliance_request, session_id)
        
        return {
            "success": True,
            "session_id": session_id,
            "results": {
                "analysis": analysis_result["data"],
                "content_creation": creation_result["data"],
                "compliance_check": compliance_result["data"]
            }
        }
        
    except Exception as e:
        logger.error(f"Pipeline error: {str(e)}")
        return {
            "success": False,
            "error": f"Pipeline failed: {str(e)}"
        }

@router.get("/performance")
async def get_agent_performance():
    """Get performance metrics for all agents"""
    
    # In a real implementation, this would pull from metrics/monitoring
    return {
        "trend_analyzer": {
            "total_executions": 0,
            "average_execution_time": 0.0,
            "success_rate": 100.0,
            "active_sessions": len(trend_analyzer.states)
        },
        "content_creator": {
            "total_executions": 0,
            "average_execution_time": 0.0,
            "success_rate": 100.0,
            "active_sessions": len(content_creator.states)
        },
        "compliance_agent": {
            "total_executions": 0,
            "average_execution_time": 0.0,
            "success_rate": 100.0,
            "active_sessions": len(compliance_agent.states)
        }
    }