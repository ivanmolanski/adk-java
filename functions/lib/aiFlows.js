"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TrendOutputSchema = exports.TrendInputSchema = void 0;
exports.analyzeTrend = analyzeTrend;
exports.createContent = createContent;
exports.classifyIntent = classifyIntent;
exports.summarizeConversation = summarizeConversation;
exports.storeChatSession = storeChatSession;
exports.loadChatSession = loadChatSession;
// Genkit AI integration for real Gemini model calls (safety disabled per project directive)
const genkit_1 = require("genkit");
const googleai_1 = require("@genkit-ai/googleai");
const zod_1 = require("zod");
const v2_1 = require("firebase-functions/v2");
const firestore_1 = require("@google-cloud/firestore");
// Defer AI initialization until first use (Firebase secrets not available at build time)
let ai = null;
function getAI(apiKey) {
    if (!ai) {
        const key = apiKey || process.env.GEMINI_API_KEY;
        if (!key) {
            throw new Error('GEMINI_API_KEY environment variable is required for Gemini model access.');
        }
        ai = (0, genkit_1.genkit)({
            plugins: [(0, googleai_1.googleAI)({ apiKey: key })],
        });
    }
    return ai;
}
// Single enforced model per directive (NO FALLBACK ALLOWED)
const MODEL = 'gemini-2.5-flash';
// Unified safety disable list across common categories.
const DISABLE_SAFETY = [
    'HARM_CATEGORY_HATE_SPEECH',
    'HARM_CATEGORY_HARASSMENT',
    'HARM_CATEGORY_SEXUAL',
    'HARM_CATEGORY_DANGEROUS_CONTENT'
].map(c => ({ category: c, threshold: 'BLOCK_NONE' }));
async function generateJson(prompt, schemaHint, maxRetries = 2, apiKey) {
    const fullPrompt = `${prompt}\n\nRespond ONLY with valid minified JSON matching: ${schemaHint}`;
    let lastErr;
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
        try {
            v2_1.logger.debug('AI generate attempt', { attempt, model: MODEL });
            const aiInstance = getAI(apiKey);
            const resp = await aiInstance.generate({
                model: MODEL,
                prompt: fullPrompt,
                config: { safetySettings: DISABLE_SAFETY }
            });
            const text = resp.text ? resp.text() : resp.response?.text?.() || resp.outputText || '';
            const jsonStart = text.indexOf('{');
            const jsonEnd = text.lastIndexOf('}');
            if (jsonStart === -1 || jsonEnd === -1)
                throw new Error('No JSON braces in output');
            const candidate = text.substring(jsonStart, jsonEnd + 1).trim();
            return JSON.parse(candidate);
        }
        catch (e) {
            lastErr = e;
            v2_1.logger.error('AI generation attempt failed', { attempt, error: e.message });
            if (attempt === maxRetries)
                break;
        }
    }
    return { error: 'generation_failed', details: String(lastErr), rawPrompt: prompt };
}
const firestore = new firestore_1.Firestore();
// Schemas
exports.TrendInputSchema = zod_1.z.object({
    post: zod_1.z.object({
        platform: zod_1.z.string(),
        profile: zod_1.z.string(),
        caption: zod_1.z.string().optional(),
        hashtags: zod_1.z.array(zod_1.z.string()).optional(),
        likes: zod_1.z.number().optional(),
        comments: zod_1.z.number().optional(),
        shares: zod_1.z.number().optional(),
        views: zod_1.z.number().optional(),
        engagementRate: zod_1.z.number().optional()
    })
});
exports.TrendOutputSchema = zod_1.z.object({
    category: zod_1.z.enum(['Process Demystified', 'Science Explained', 'Transformation', 'Myth Busting']).optional(),
    hook: zod_1.z.string().optional(),
    cta: zod_1.z.string().optional(),
    educationalPoint: zod_1.z.string().optional(),
    summary: zod_1.z.string().optional()
});
async function analyzeTrend(post) {
    const postId = post.postId || post.id || `${post.platform}_${post.profile}_${post.timestamp || ''}`;
    const cacheRef = firestore.collection('trend_analysis').doc(postId);
    try {
        const cachedSnap = await cacheRef.get();
        if (cachedSnap.exists) {
            const data = cachedSnap.data();
            if (data) {
                // Optional TTL: 48h
                if (!data.cachedAt || Date.now() - new Date(data.cachedAt).getTime() < 48 * 3600 * 1000) {
                    v2_1.logger.debug('Trend analysis cache hit', { postId });
                    return data.analysis;
                }
            }
        }
    }
    catch (e) {
        v2_1.logger.error('Trend analysis cache read failed', { postId, error: e.message });
    }
    const schema = '{"category":"one of Process Demystified | Science Explained | Transformation | Myth Busting","hook":"string","cta":"string","educationalPoint":"string","summary":"1 sentence overview"}';
    const prompt = `You are a social media analyst for a physician-led clinical aesthetics brand. Classify and extract structured insights.\nPOST DATA (JSON):\n${JSON.stringify(post).slice(0, 4000)}\nReturn JSON only.`;
    const analysis = await generateJson(prompt, schema);
    try {
        await cacheRef.set({ analysis, cachedAt: new Date().toISOString() }, { merge: true });
        v2_1.logger.debug('Trend analysis cached', { postId });
    }
    catch (e) {
        v2_1.logger.error('Trend analysis cache write failed', { postId, error: e.message });
    }
    return analysis;
}
async function createContent(analysis, focusService) {
    // Derive a deterministic key from analysis content + focusService
    const base = JSON.stringify(analysis).slice(0, 2000); // truncated for stability
    const hash = Buffer.from(base).toString('base64').replace(/[^a-zA-Z0-9]/g, '').slice(0, 32);
    const key = `${hash}_${focusService.replace(/\s+/g, '_')}`;
    const cacheRef = firestore.collection('content_drafts').doc(key);
    try {
        const snap = await cacheRef.get();
        if (snap.exists) {
            const data = snap.data();
            if (data && data.cachedAt && Date.now() - new Date(data.cachedAt).getTime() < 48 * 3600 * 1000) {
                v2_1.logger.debug('Content draft cache hit', { key });
                return data.draft;
            }
        }
    }
    catch (e) {
        v2_1.logger.error('Content draft cache read failed', { key, error: e.message });
    }
    const schema = '{"caption":"instagram+tik tok optimized caption <= 2100 chars","hashtags":"array of 8-18 lowercase hashtags without # symbol","cta":"clear consultation CTA","rawHook":"first 3 seconds hook text"}';
    const prompt = `You are Dr. Copeland's trusted clinical strategist. Using the analyzed viral pattern below, create a superior on-brand post pivoting to service: ${focusService}.\nBRAND GUARDRAILS: Clinical, authoritative, educational, results-focused. Replace any occurrence of Botox with Neuromodulator or Tox. Must include one educational mechanism explanation and avoid fluff.\nANALYSIS JSON: ${JSON.stringify(analysis).slice(0, 4000)}\nReturn ONLY JSON.`;
    const result = await generateJson(prompt, schema);
    if (result.caption) {
        result.caption = result.caption.replace(/botox/ig, 'Neuromodulator');
    }
    if (Array.isArray(result.hashtags)) {
        result.hashtags = result.hashtags.map((h) => h.replace(/^[#]/, '')).filter(Boolean);
    }
    try {
        await cacheRef.set({ draft: result, cachedAt: new Date().toISOString(), focusService }, { merge: true });
        v2_1.logger.debug('Content draft cached', { key });
    }
    catch (e) {
        v2_1.logger.error('Content draft cache write failed', { key, error: e.message });
    }
    return result;
}
async function classifyIntent(message, apiKey) {
    const schema = '{"intent":"one of ORCHESTRATE|HELP|INSIGHT|UNKNOWN","confidence":0-1,"reason":"short rationale"}';
    const prompt = `Classify the user message intent for the MD Aesthetics command center.\nMessage: "${message}"\nRules: ORCHESTRATE if asking to run/scrape/analyze/generate. INSIGHT if asking for trends, summaries, metrics. HELP if asking how to use system. UNKNOWN otherwise.`;
    return await generateJson(prompt, schema, 2, apiKey);
}
async function summarizeConversation(messages, apiKey) {
    const last20 = messages.slice(-20);
    const schema = '{"summary":"concise state summary < 80 words","pendingActions":"array of short action strings"}';
    const prompt = `Summarize this MD Aesthetics AI command session focusing on goals, chosen services, unresolved requests. Provide pending actions array. Messages JSON: ${JSON.stringify(last20).slice(0, 7000)}.`;
    return await generateJson(prompt, schema, 2, apiKey);
}
async function storeChatSession(sessionId, data) {
    await firestore.collection('chat_sessions').doc(sessionId).set({ ...data, updatedAt: new Date().toISOString() }, { merge: true });
}
async function loadChatSession(sessionId) {
    const snap = await firestore.collection('chat_sessions').doc(sessionId).get();
    return snap.exists ? snap.data() : null;
}
//# sourceMappingURL=aiFlows.js.map