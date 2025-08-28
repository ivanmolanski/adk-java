// OpenRouter API integration for real AI model calls (replacing Gemini)
/*
  NOTE: We now use OpenRouter API instead of Genkit/GoogleAI for better model access and control.
  This provides direct HTTP calls to OpenRouter's chat completions endpoint with the z-ai/glm-4.5-air:free model.
*/
import { z } from 'zod';
import { initializeApp, getApps } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import fetch from 'node-fetch';

// Initialize Firebase Admin if not already initialized
if (!getApps().length) {
  initializeApp();
}

// Dynamic logger - use console if firebase-functions not available
function getLogger() {
  try {
    const functionsLogger = require('firebase-functions/v2').logger;
    return functionsLogger;
  } catch (e) {
    // Fallback to console if firebase-functions not available
    return console;
  }
}

// OpenRouter API configuration
const OPENROUTER_BASE_URL = 'https://openrouter.ai/api/v1/chat/completions';
const DEFAULT_MODEL = 'z-ai/glm-4.5-air:free';
const SITE_URL = 'https://mdaesthetics.ca';
const SITE_NAME = 'MD Aesthetics Viral Forge';

// OpenRouter API types
interface OpenRouterMessage {
  role: string;
  content: string;
}

interface OpenRouterChoice {
  message: OpenRouterMessage;
  index: number;
  finish_reason: string;
}

interface OpenRouterResponse {
  choices: OpenRouterChoice[];
  id: string;
  model: string;
  object: string;
  usage?: {
    prompt_tokens: number;
    completion_tokens: number;
    total_tokens: number;
  };
}

// OpenRouter API client using native fetch
async function callOpenRouter(prompt: string, apiKey: string, temperature: number = 2.0): Promise<string> {
  const logger = getLogger();
  
  const requestBody = {
    model: DEFAULT_MODEL,
    messages: [{ role: 'user', content: prompt }],
    temperature: temperature,
    max_tokens: 4000
  };

  try {
    const response = await fetch(OPENROUTER_BASE_URL, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'HTTP-Referer': SITE_URL,
        'X-Title': SITE_NAME,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      const errorText = await response.text();
      logger.error('OpenRouter API error:', { status: response.status, body: errorText });
      throw new Error(`OpenRouter API error: ${response.status} - ${errorText}`);
    }

    const data = await response.json() as OpenRouterResponse;
    
    if (data.choices && data.choices.length > 0 && data.choices[0].message) {
      return data.choices[0].message.content || '';
    }
    
    logger.warn('OpenRouter returned no content in response');
    return '';
    
  } catch (error: any) {
    logger.error('OpenRouter API call failed:', error);
    throw error;
  }
}

async function generateJson(prompt: string, schemaHint: string, maxRetries = 2, apiKey?: string): Promise<any> {
  if (!apiKey) {
    throw new Error('OpenRouter API key is required');
  }
  
  const fullPrompt = `${prompt}\n\nRespond ONLY with valid minified JSON matching: ${schemaHint}`;
  const logger = getLogger();
  let lastErr: any;
  
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      logger.debug('OpenRouter generate attempt', { attempt, model: DEFAULT_MODEL });
      const text = await callOpenRouter(fullPrompt, apiKey, 2.0);
      
      const jsonStart = text.indexOf('{');
      const jsonEnd = text.lastIndexOf('}');
      if (jsonStart === -1 || jsonEnd === -1) throw new Error('No JSON braces in output');
      
      const candidate = text.substring(jsonStart, jsonEnd + 1).trim();
      return JSON.parse(candidate);
      
    } catch (e: any) {
      lastErr = e;
      logger.error('OpenRouter generation attempt failed', { attempt, error: e.message });
      if (attempt === maxRetries) break;
    }
  }
  
  return { error: 'generation_failed', details: String(lastErr), rawPrompt: prompt };
}

const firestore = getFirestore();

// Schemas
export const TrendInputSchema = z.object({
  post: z.object({
    platform: z.string(),
    profile: z.string(),
    caption: z.string().optional(),
    hashtags: z.array(z.string()).optional(),
    likes: z.number().optional(),
    comments: z.number().optional(),
    shares: z.number().optional(),
    views: z.number().optional(),
    engagementRate: z.number().optional()
  })
});

export const TrendOutputSchema = z.object({
  category: z.enum(['Process Demystified','Science Explained','Transformation','Myth Busting']).optional(),
  hook: z.string().optional(),
  cta: z.string().optional(),
  educationalPoint: z.string().optional(),
  summary: z.string().optional()
});

export async function analyzeTrend(post:any) {
  const logger = getLogger();
  const postId = post.postId || post.id || `${post.platform}_${post.profile}_${post.timestamp||''}`;
  const cacheRef = firestore.collection('trend_analysis').doc(postId);
  try {
    const cachedSnap = await cacheRef.get();
    if (cachedSnap.exists) {
      const data: any = cachedSnap.data();
      if (data) {
        // Optional TTL: 48h
        if (!data.cachedAt || Date.now() - new Date(data.cachedAt).getTime() < 48 * 3600 * 1000) {
          logger.debug('Trend analysis cache hit', { postId });
          return data.analysis;
        }
      }
    }
  } catch (e:any) {
    logger.error('Trend analysis cache read failed', { postId, error: e.message });
  }
  const schema = '{"category":"one of Process Demystified | Science Explained | Transformation | Myth Busting","hook":"string","cta":"string","educationalPoint":"string","summary":"1 sentence overview"}';
  const prompt = `You are a social media analyst for a physician-led clinical aesthetics brand. Classify and extract structured insights.\nPOST DATA (JSON):\n${JSON.stringify(post).slice(0,4000)}\nReturn JSON only.`;
  const analysis = await generateJson(prompt, schema);
  try {
    await cacheRef.set({ analysis, cachedAt: new Date().toISOString() }, { merge: true });
    logger.debug('Trend analysis cached', { postId });
  } catch (e:any) {
    logger.error('Trend analysis cache write failed', { postId, error: e.message });
  }
  return analysis;
}

export async function createContent(analysis:any, focusService: string) {
  const logger = getLogger();
  // Derive a deterministic key from analysis content + focusService
  const base = JSON.stringify(analysis).slice(0,2000); // truncated for stability
  const hash = Buffer.from(base).toString('base64').replace(/[^a-zA-Z0-9]/g,'').slice(0,32);
  const key = `${hash}_${focusService.replace(/\s+/g,'_')}`;
  const cacheRef = firestore.collection('content_drafts').doc(key);
  try {
    const snap = await cacheRef.get();
    if (snap.exists) {
      const data:any = snap.data();
      if (data && data.cachedAt && Date.now() - new Date(data.cachedAt).getTime() < 48*3600*1000) {
        logger.debug('Content draft cache hit', { key });
        return data.draft;
      }
    }
  } catch (e:any) {
    logger.error('Content draft cache read failed', { key, error: e.message });
  }
  const schema = '{"caption":"instagram+tik tok optimized caption <= 2100 chars","hashtags":"array of 8-18 lowercase hashtags without # symbol","cta":"clear consultation CTA","rawHook":"first 3 seconds hook text"}';
  const prompt = `You are Dr. Copeland's trusted clinical strategist. Using the analyzed viral pattern below, create a superior on-brand post pivoting to service: ${focusService}.\nBRAND GUARDRAILS: Clinical, authoritative, educational, results-focused. Replace any occurrence of Botox with Neuromodulator or Tox. Must include one educational mechanism explanation and avoid fluff.\nANALYSIS JSON: ${JSON.stringify(analysis).slice(0,4000)}\nReturn ONLY JSON.`;
  const result = await generateJson(prompt, schema);
  if (result.caption) {
    result.caption = result.caption.replace(/botox/ig,'Neuromodulator');
  }
  if (Array.isArray(result.hashtags)) {
    result.hashtags = result.hashtags.map((h:string)=> h.replace(/^[#]/,'')).filter(Boolean);
  }
  try {
    await cacheRef.set({ draft: result, cachedAt: new Date().toISOString(), focusService }, { merge: true });
    logger.debug('Content draft cached', { key });
  } catch (e:any) {
    logger.error('Content draft cache write failed', { key, error: e.message });
  }
  return result;
}

export async function classifyIntent(message: string, apiKey?: string) {
  const schema = '{"intent":"one of ORCHESTRATE|HELP|INSIGHT|UNKNOWN","confidence":0-1,"reason":"short rationale"}';
  const prompt = `Classify the user message intent for the MD Aesthetics command center.\nMessage: "${message}"\nRules: ORCHESTRATE if asking to run/scrape/analyze/generate. INSIGHT if asking for trends, summaries, metrics. HELP if asking how to use system. UNKNOWN otherwise.`;
  return await generateJson(prompt, schema, 2, apiKey);
}

export async function summarizeConversation(messages: Array<{role:string; content:string}>, apiKey?: string) {
  const last20 = messages.slice(-20);
  const schema = '{"summary":"concise state summary < 80 words","pendingActions":"array of short action strings"}';
  const prompt = `Summarize this MD Aesthetics AI command session focusing on goals, chosen services, unresolved requests. Provide pending actions array. Messages JSON: ${JSON.stringify(last20).slice(0,7000)}.`;
  return await generateJson(prompt, schema, 2, apiKey);
}

export async function storeChatSession(sessionId:string, data:any) {
  await firestore.collection('chat_sessions').doc(sessionId).set({ ...data, updatedAt: new Date().toISOString() }, { merge: true });
}

export async function loadChatSession(sessionId:string) {
  const snap = await firestore.collection('chat_sessions').doc(sessionId).get();
  return snap.exists ? snap.data() : null;
}
