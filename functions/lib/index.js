"use strict";
// Clean TypeScript file for Firebase Functions v2
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.chatCommand = exports.dailyBrief = exports.manualOrchestrate = exports.runOrchestrationHttp = exports.dailyUnifiedOrchestration = exports.healthCheck = exports.processViralPost = exports.dailyTiktokScraper = exports.dailyViralScraper = void 0;
const scheduler_1 = require("firebase-functions/v2/scheduler");
const firestore_1 = require("firebase-functions/v2/firestore");
const https_1 = require("firebase-functions/v2/https");
const params_1 = require("firebase-functions/params");
const apify_client_1 = require("apify-client");
const app_1 = require("firebase-admin/app");
const firestore_2 = require("firebase-admin/firestore");
const v2_1 = require("firebase-functions/v2");
const pubsub_1 = require("@google-cloud/pubsub");
const sendDailyDigest_1 = require("./sendDailyDigest");
const aiFlows_1 = require("./aiFlows");
// Initialize Firebase Admin if not already initialized
if (!(0, app_1.getApps)().length) {
    (0, app_1.initializeApp)();
}
const db = (0, firestore_2.getFirestore)();
const pubsub = new pubsub_1.PubSub();
// Pub/Sub topic names (can be overridden by env vars later if desired)
const TOPIC_VIRAL_POST_CREATED = process.env.PUBSUB_TOPIC_VIRAL_POST_CREATED || 'viral-post-created';
const TOPIC_ORCHESTRATION_COMPLETED = process.env.PUBSUB_TOPIC_ORCHESTRATION_COMPLETED || 'orchestration-completed';
// Define secrets using Firebase Functions v2 approach
const apifyToken = (0, params_1.defineSecret)('APIFY_TOKEN');
const googleApiKey = (0, params_1.defineSecret)('MD_API_KEY');
const serviceAccountJson = (0, params_1.defineSecret)('MD_SERVICE_ACCOUNT');
const googleCseKey = (0, params_1.defineSecret)('GOOGLE_CSE_KEY');
const googleCseCx = (0, params_1.defineSecret)('GOOGLE_CSE_CX');
const geminiApiKey = (0, params_1.defineSecret)('GEMINI_API_KEY');
// Competitor profiles to monitor (hardcoded as per requirements)
const competitorProfiles = [
    '_thelookaesthetics',
    'subtle.enhancements',
    'skinvitalityofficial'
];
// TikTok profiles & hashtags initial seeds
const tiktokProfiles = [
    'mdaesthetics.ca',
    'skinvitalityofficial'
];
const tiktokHashtags = [
    'duoclift', 'skintyte', 'radiesse', 'torontoaesthetics', 'whitbymedspa'
];
/**
 * Daily scheduled function to scrape competitor Instagram posts using Apify
 * Runs at 8:00 AM EST daily
 */
exports.dailyViralScraper = (0, scheduler_1.onSchedule)({
    schedule: '0 8 * * *',
    timeZone: 'America/Toronto',
    memory: '1GiB',
    timeoutSeconds: 540,
    secrets: [apifyToken, googleApiKey]
}, async (event) => {
    v2_1.logger.info('Starting daily viral scraper job', { timestamp: new Date().toISOString() });
    try {
        // Initialize Apify client with secret token
        const apifyClient = new apify_client_1.ApifyClient({
            token: apifyToken.value()
        });
        const allPosts = [];
        for (const profile of competitorProfiles) {
            v2_1.logger.info(`Scraping profile: ${profile}`);
            try {
                // Configure Apify Instagram Scraper run
                const runInput = {
                    directUrls: [`https://www.instagram.com/${profile}/`],
                    resultsType: 'posts',
                    resultsLimit: 50,
                    addParentData: false
                };
                // Start the scraper
                const run = await apifyClient.actor('apify/instagram-scraper').call(runInput);
                // Get results
                const { items } = await apifyClient.dataset(run.defaultDatasetId).listItems();
                v2_1.logger.info(`Successfully scraped ${items.length} posts from ${profile}`);
                // Process and enhance each post
                for (const item of items) {
                    const enhancedPost = {
                        platform: 'instagram',
                        profile: profile,
                        postId: item.id || '',
                        postURL: item.url || '',
                        caption: item.caption || '',
                        hashtags: extractHashtags(item.caption || ''),
                        likes: item.likesCount || 0,
                        comments: item.commentsCount || 0,
                        shares: 0, // Instagram doesn't provide shares in public API
                        views: item.videoViewCount || 0,
                        mediaType: item.type || 'photo',
                        displayUrl: item.displayUrl || '',
                        ownerFullName: item.ownerFullName || '',
                        ownerUsername: item.ownerUsername || profile,
                        timestamp: item.timestamp ? new Date(item.timestamp).toISOString() : new Date().toISOString(),
                        engagementRate: calculateEngagementRate(item.likesCount || 0, item.commentsCount || 0, item.ownerFollowersCount || 1),
                        scrapedAt: new Date().toISOString(),
                        source: 'apify'
                    };
                    allPosts.push(enhancedPost);
                }
            }
            catch (profileError) {
                v2_1.logger.error(`Failed to scrape profile ${profile}:`, profileError);
                continue; // Continue with next profile
            }
        }
        // Save all posts to Firestore
        const batch = db.batch();
        const collectionRef = db.collection('viral_research');
        const dateKey = new Date().toISOString().split('T')[0];
        allPosts.forEach((post, index) => {
            const docRef = collectionRef.doc(`${dateKey}_${post.profile}_${index}`);
            batch.set(docRef, post);
        });
        await batch.commit();
        v2_1.logger.info(`Daily scraper completed successfully. Processed ${allPosts.length} posts total.`);
    }
    catch (error) {
        v2_1.logger.error('Daily viral scraper failed:', error);
        throw error;
    }
});
/**
 * Scheduled TikTok scraper leveraging Apify clockworks/tiktok-scraper actor.
 * Runs daily at 8:10 AM after Instagram scrape.
 */
exports.dailyTiktokScraper = (0, scheduler_1.onSchedule)({
    schedule: '10 8 * * *',
    timeZone: 'America/Toronto',
    memory: '1GiB',
    timeoutSeconds: 540,
    secrets: [apifyToken]
}, async () => {
    v2_1.logger.info('Starting daily TikTok scraper job');
    try {
        const apifyClient = new apify_client_1.ApifyClient({ token: apifyToken.value() });
        const input = {
            profiles: tiktokProfiles,
            hashtags: tiktokHashtags,
            resultsPerPage: 50,
            profileScrapeSections: ['videos'],
            excludePinnedPosts: true,
            shouldDownloadVideos: false,
            shouldDownloadCovers: false
        };
        const run = await apifyClient.actor('clockworks/tiktok-scraper').call(input);
        const { items } = await apifyClient.dataset(run.defaultDatasetId).listItems();
        v2_1.logger.info(`TikTok scraper returned ${items.length} items`);
        const batch = db.batch();
        const collectionRef = db.collection('viral_research');
        const dateKey = new Date().toISOString().split('T')[0];
        items.forEach((item, idx) => {
            // Normalize fields based on typical TikTok dataset structure (hearts, comments, shares, plays)
            const post = {
                platform: 'tiktok',
                profile: item.authorUsername || item.authorName || 'unknown',
                postId: item.id || item.videoId || `${dateKey}_${idx}`,
                postURL: item.url || item.shareUrl || '',
                caption: item.text || item.caption || '',
                hashtags: (item.hashtags || []).map((h) => `#${h.name || h}`),
                likes: item.diggCount || item.hearts || 0,
                comments: item.commentCount || 0,
                shares: item.shareCount || 0,
                views: item.playCount || item.plays || 0,
                mediaType: 'video',
                thumbnail: item.cover || item.covers?.origin || null,
                musicTitle: item.music?.title || null,
                musicAuthor: item.music?.authorName || null,
                timestamp: item.createTime ? new Date(item.createTime * 1000).toISOString() : new Date().toISOString(),
                engagementRate: (() => {
                    const followers = item.authorFollowers || item.authorStats?.followerCount || 1;
                    return (((item.diggCount || 0) + (item.commentCount || 0) + (item.shareCount || 0)) / followers) * 100;
                })(),
                scrapedAt: new Date().toISOString(),
                source: 'apify-tiktok'
            };
            const docRef = collectionRef.doc(`${dateKey}_tiktok_${post.postId}`);
            batch.set(docRef, post, { merge: true });
        });
        await batch.commit();
        v2_1.logger.info('Daily TikTok scraper completed.');
    }
    catch (e) {
        v2_1.logger.error('Daily TikTok scraper failed', e);
        throw e;
    }
});
/**
 * Triggered when new viral research data is added to Firestore
 * Starts the agent analysis pipeline
 */
exports.processViralPost = (0, firestore_1.onDocumentCreated)({
    document: 'viral_research/{docId}',
    secrets: [googleApiKey, serviceAccountJson, geminiApiKey]
}, async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
        v2_1.logger.warn('No data associated with the event');
        return;
    }
    const postData = snapshot.data();
    v2_1.logger.info(`Processing new viral post: ${postData.postId}`, {
        profile: postData.profile,
        platform: postData.platform
    });
    try {
        // Here we would trigger the Java ADK agents via HTTP call
        // For now, we'll log the event and store analysis request
        const analysisRequest = {
            postId: postData.postId,
            profile: postData.profile,
            platform: postData.platform,
            status: 'pending_analysis',
            createdAt: new Date().toISOString(),
            originalPost: postData
        };
        await db.collection('analysis_requests').add(analysisRequest);
        // Publish event to Pub/Sub for downstream Java ADK consumption
        try {
            await pubsub.topic(TOPIC_VIRAL_POST_CREATED).publishMessage({ json: {
                    version: 1,
                    postId: postData.postId,
                    platform: postData.platform,
                    profile: postData.profile,
                    docPath: snapshot.ref.path,
                    createdAt: new Date().toISOString()
                } });
        }
        catch (e) {
            v2_1.logger.error('Failed publishing viral-post-created event', e);
        }
        v2_1.logger.info(`Analysis request created for post ${postData.postId}`);
    }
    catch (error) {
        v2_1.logger.error(`Failed to process viral post ${postData.postId}:`, error);
        throw error;
    }
});
/**
 * Extract hashtags from caption text
 */
function extractHashtags(caption) {
    const hashtagRegex = /#[a-zA-Z0-9_]+/g;
    return caption.match(hashtagRegex) || [];
}
/**
 * Calculate engagement rate as (likes + comments) / followers * 100
 */
function calculateEngagementRate(likes, comments, followers) {
    if (followers <= 0)
        return 0;
    return ((likes + comments) / followers) * 100;
}
/**
 * Health check endpoint for monitoring
 */
exports.healthCheck = (0, scheduler_1.onSchedule)({
    schedule: '*/5 * * * *', // Every 5 minutes
    timeZone: 'America/Toronto'
}, async (event) => {
    v2_1.logger.info('Health check - all systems operational');
});
/**
 * Unified orchestration scheduled job (after scraping) to run CSE augmentation and enrichment pipeline.
 * Runs daily at 8:30 AM EST (after dailyViralScraper 8:00 AM) to allow scrape completion.
 */
exports.dailyUnifiedOrchestration = (0, scheduler_1.onSchedule)({
    schedule: '30 8 * * *',
    timeZone: 'America/Toronto',
    memory: '512MiB',
    timeoutSeconds: 300,
    secrets: [googleCseKey, googleCseCx]
}, async () => {
    const { runOrchestration } = await Promise.resolve().then(() => __importStar(require('./AgentOrchestrator.js')));
    v2_1.logger.info('Starting unified orchestration run');
    // Fetch today's scraped posts to feed pipeline
    const dateKey = new Date().toISOString().split('T')[0];
    const snap = await db.collection('viral_research').where('scrapedAt', '>=', dateKey).limit(200).get();
    const posts = [];
    snap.forEach(doc => posts.push({ id: doc.id, ...doc.data() }));
    const result = await runOrchestration({ posts });
    v2_1.logger.info('Unified orchestration completed', result);
    await db.collection('orchestration_runs').add({
        type: 'scheduled',
        startedAt: result.started,
        durationMs: result.durationMs,
        status: result.status,
        metrics: result,
        createdAt: new Date().toISOString()
    });
    // Publish orchestration completion event
    try {
        await pubsub.topic(TOPIC_ORCHESTRATION_COMPLETED).publishMessage({ json: {
                version: 1,
                runType: 'scheduled',
                startedAt: result.started,
                durationMs: result.durationMs,
                status: result.status,
                platformCounts: result.platformCounts || {},
                enrichedPosts: result.enrichedPosts,
                cse: result.cse,
                emittedAt: new Date().toISOString()
            } });
    }
    catch (e) {
        v2_1.logger.error('Failed publishing orchestration-completed event', e);
    }
});
/**
 * HTTP endpoint to trigger orchestration on-demand. Optional query param limit.
 */
exports.runOrchestrationHttp = (0, https_1.onRequest)({ secrets: [googleCseKey, googleCseCx] }, async (req, res) => {
    try {
        const { runOrchestration } = await Promise.resolve().then(() => __importStar(require('./AgentOrchestrator.js')));
        const limit = Math.min(parseInt(req.query.limit) || 50, 500);
        const snap = await db.collection('viral_research').orderBy('scrapedAt', 'desc').limit(limit).get();
        const posts = [];
        snap.forEach(doc => posts.push({ id: doc.id, ...doc.data() }));
        const result = await runOrchestration({ posts });
        await db.collection('orchestration_runs').add({
            type: 'http',
            startedAt: result.started,
            durationMs: result.durationMs,
            status: result.status,
            metrics: result,
            createdAt: new Date().toISOString()
        });
        try {
            await pubsub.topic(TOPIC_ORCHESTRATION_COMPLETED).publishMessage({ json: {
                    version: 1,
                    runType: 'http',
                    startedAt: result.started,
                    durationMs: result.durationMs,
                    status: result.status,
                    platformCounts: result.platformCounts || {},
                    enrichedPosts: result.enrichedPosts,
                    cse: result.cse,
                    emittedAt: new Date().toISOString()
                } });
        }
        catch (e) {
            v2_1.logger.error('Failed publishing orchestration-completed (http) event', e);
        }
        res.status(200).json(result);
    }
    catch (e) {
        v2_1.logger.error('On-demand orchestration failed', e);
        res.status(500).json({ error: e.message });
    }
});
/**
 * manualOrchestrate - Advanced command-center trigger used by AI Chat GUI.
 * Query params:
 *   limit (number) - number of recent posts to include (default 50)
 *   queryTerm (string) - optional ad-hoc strategic term to temporarily append to GOOGLE_CSE_TERMS for this run only (e.g. "BBL therapy today")
 *   disableCse=true|false - optionally bypass global CSE augmentation
 */
exports.manualOrchestrate = (0, https_1.onRequest)({ secrets: [googleCseKey, googleCseCx] }, async (req, res) => {
    try {
        const { runOrchestration } = await Promise.resolve().then(() => __importStar(require('./AgentOrchestrator.js')));
        const limit = Math.min(parseInt(req.query.limit) || 50, 500);
        const queryTerm = req.query.queryTerm || '';
        const disableCse = /^true$/i.test(req.query.disableCse || '');
        // Temporary override for this invocation only
        const originalTerms = process.env.GOOGLE_CSE_TERMS;
        const originalEnable = process.env.ENABLE_GLOBAL_CSE;
        if (queryTerm) {
            const merged = originalTerms ? `${originalTerms},${queryTerm}` : queryTerm;
            process.env.GOOGLE_CSE_TERMS = merged;
        }
        if (disableCse) {
            process.env.ENABLE_GLOBAL_CSE = 'false';
        }
        const snap = await db.collection('viral_research').orderBy('scrapedAt', 'desc').limit(limit).get();
        const posts = [];
        snap.forEach(doc => posts.push({ id: doc.id, ...doc.data() }));
        const result = await runOrchestration({ posts });
        // Restore env modifications
        if (queryTerm)
            process.env.GOOGLE_CSE_TERMS = originalTerms;
        if (disableCse)
            process.env.ENABLE_GLOBAL_CSE = originalEnable;
        await db.collection('orchestration_runs').add({
            type: 'manual',
            startedAt: result.started,
            durationMs: result.durationMs,
            status: result.status,
            adHocQuery: queryTerm || null,
            cseDisabled: disableCse,
            metrics: result,
            createdAt: new Date().toISOString()
        });
        try {
            await pubsub.topic(TOPIC_ORCHESTRATION_COMPLETED).publishMessage({ json: {
                    version: 1,
                    runType: 'manual',
                    startedAt: result.started,
                    durationMs: result.durationMs,
                    status: result.status,
                    platformCounts: result.platformCounts || {},
                    enrichedPosts: result.enrichedPosts,
                    cse: result.cse,
                    adHocQuery: queryTerm || null,
                    cseDisabled: disableCse,
                    emittedAt: new Date().toISOString()
                } });
        }
        catch (e) {
            v2_1.logger.error('Failed publishing orchestration-completed (manual) event', e);
        }
        res.status(200).json(result);
    }
    catch (e) {
        v2_1.logger.error('Manual orchestration failed', e);
        res.status(500).json({ error: e.message });
    }
});
/**
 * Scheduled Daily Digest at 12:00 PM America/Toronto
 */
exports.dailyBrief = (0, scheduler_1.onSchedule)({
    schedule: '0 12 * * *',
    timeZone: 'America/Toronto',
    memory: '512MiB',
    timeoutSeconds: 180
}, async () => {
    try {
        await (0, sendDailyDigest_1.generateDailyDigest)();
    }
    catch (e) {
        v2_1.logger.error('dailyBrief generation failed', e);
        throw e;
    }
});
/**
 * Chat endpoint with persistent session memory & intent classification.
 * POST body: { sessionId?: string, message: string }
 */
exports.chatCommand = (0, https_1.onRequest)({ secrets: [geminiApiKey] }, async (req, res) => {
    if (req.method !== 'POST') {
        res.status(405).json({ error: 'POST only' });
        return;
    }
    const { sessionId: providedId, message } = req.body || {};
    if (!message) {
        res.status(400).json({ error: 'message required' });
        return;
    }
    const sessionId = providedId || `sess_${Date.now().toString(36)}`;
    const existing = await (0, aiFlows_1.loadChatSession)(sessionId) || { messages: [], summaries: [] };
    existing.messages.push({ role: 'user', content: message, at: new Date().toISOString() });
    // Classify intent
    let intentRaw = '';
    try {
        intentRaw = await (0, aiFlows_1.classifyIntent)(message, geminiApiKey.value());
    }
    catch (e) {
        v2_1.logger.error('Intent classification failed', e);
    }
    let intent = 'UNKNOWN';
    try {
        intent = JSON.parse(intentRaw).intent || 'UNKNOWN';
    }
    catch { }
    // Optionally trigger orchestration
    let orchestrationResult = null;
    if (intent === 'ORCHESTRATE') {
        try {
            const { runOrchestration } = await Promise.resolve().then(() => __importStar(require('./AgentOrchestrator.js')));
            const snap = await db.collection('viral_research').orderBy('scrapedAt', 'desc').limit(40).get();
            const posts = [];
            snap.forEach(doc => posts.push({ id: doc.id, ...doc.data() }));
            orchestrationResult = await runOrchestration({ posts });
        }
        catch (e) {
            v2_1.logger.error('Chat-triggered orchestration failed', e);
        }
    }
    // Summarize every 12 messages
    if (existing.messages.length % 12 === 0) {
        try {
            const summary = await (0, aiFlows_1.summarizeConversation)(existing.messages.slice(-50), geminiApiKey.value());
            existing.summaries.push({ summary, at: new Date().toISOString() });
        }
        catch (e) {
            v2_1.logger.error('Conversation summarization failed', e);
        }
    }
    existing.messages.push({ role: 'system', content: `Intent: ${intent}`, at: new Date().toISOString() });
    await (0, aiFlows_1.storeChatSession)(sessionId, existing);
    res.status(200).json({ sessionId, intent, orchestrationResult });
});
//# sourceMappingURL=index.js.map