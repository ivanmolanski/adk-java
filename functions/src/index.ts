// Clean TypeScript file for Firebase Functions v2

import { onSchedule } from 'firebase-functions/v2/scheduler';
import { onDocumentCreated } from 'firebase-functions/v2/firestore';
import { onRequest } from 'firebase-functions/v2/https';
import { defineSecret } from 'firebase-functions/params';
import { ApifyClient } from 'apify-client';
import { initializeApp, getApps } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { logger } from 'firebase-functions/v2';
import { PubSub } from '@google-cloud/pubsub';
import { generateDailyDigest } from './sendDailyDigest';
import { classifyIntent, summarizeConversation, loadChatSession, storeChatSession } from './aiFlows';

// Initialize Firebase Admin if not already initialized
if (!getApps().length) {
  initializeApp();
}

const db = getFirestore();
const pubsub = new PubSub();

// Pub/Sub topic names (can be overridden by env vars later if desired)
const TOPIC_VIRAL_POST_CREATED = process.env.PUBSUB_TOPIC_VIRAL_POST_CREATED || 'viral-post-created';
const TOPIC_ORCHESTRATION_COMPLETED = process.env.PUBSUB_TOPIC_ORCHESTRATION_COMPLETED || 'orchestration-completed';

// Define secrets using Firebase Functions v2 approach
const apifyToken = defineSecret('APIFY_TOKEN');
const googleApiKey = defineSecret('MD_API_KEY');
const serviceAccountJson = defineSecret('MD_SERVICE_ACCOUNT');
const googleCseKey = defineSecret('GOOGLE_CSE_KEY');
const googleCseCx = defineSecret('GOOGLE_CSE_CX');
const geminiApiKey = defineSecret('GEMINI_API_KEY');

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
  'duoclift','skintyte','radiesse','torontoaesthetics','whitbymedspa'
];

/**
 * Daily scheduled function to scrape competitor Instagram posts using Apify
 * Runs at 8:00 AM EST daily
 */
export const dailyViralScraper = onSchedule({
  schedule: '0 8 * * *',
  timeZone: 'America/Toronto',
  memory: '1GiB',
  timeoutSeconds: 540,
  secrets: [apifyToken, googleApiKey]
}, async (event) => {
  logger.info('Starting daily viral scraper job', { timestamp: new Date().toISOString() });
  
  try {
    // Initialize Apify client with secret token
    const apifyClient = new ApifyClient({
      token: apifyToken.value()
    });

    const allPosts: any[] = [];
    
    for (const profile of competitorProfiles) {
      logger.info(`Scraping profile: ${profile}`);
      
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
        
        logger.info(`Successfully scraped ${items.length} posts from ${profile}`);
        
        // Process and enhance each post
        for (const item of items) {
          const enhancedPost = {
            platform: 'instagram',
            profile: profile,
            postId: (item as any).id || '',
            postURL: (item as any).url || '',
            caption: (item as any).caption || '',
            hashtags: extractHashtags((item as any).caption || ''),
            likes: (item as any).likesCount || 0,
            comments: (item as any).commentsCount || 0,
            shares: 0, // Instagram doesn't provide shares in public API
            views: (item as any).videoViewCount || 0,
            mediaType: (item as any).type || 'photo',
            displayUrl: (item as any).displayUrl || '',
            ownerFullName: (item as any).ownerFullName || '',
            ownerUsername: (item as any).ownerUsername || profile,
            timestamp: (item as any).timestamp ? new Date((item as any).timestamp).toISOString() : new Date().toISOString(),
            engagementRate: calculateEngagementRate((item as any).likesCount || 0, (item as any).commentsCount || 0, (item as any).ownerFollowersCount || 1),
            scrapedAt: new Date().toISOString(),
            source: 'apify'
          };
          
          allPosts.push(enhancedPost);
        }
        
      } catch (profileError) {
        logger.error(`Failed to scrape profile ${profile}:`, profileError);
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
    
    logger.info(`Daily scraper completed successfully. Processed ${allPosts.length} posts total.`);
    
  } catch (error) {
    logger.error('Daily viral scraper failed:', error);
    throw error;
  }
});

/**
 * Scheduled TikTok scraper leveraging Apify clockworks/tiktok-scraper actor.
 * Runs daily at 8:10 AM after Instagram scrape.
 */
export const dailyTiktokScraper = onSchedule({
  schedule: '10 8 * * *',
  timeZone: 'America/Toronto',
  memory: '1GiB',
  timeoutSeconds: 540,
  secrets: [apifyToken]
}, async () => {
  logger.info('Starting daily TikTok scraper job');
  try {
    const apifyClient = new ApifyClient({ token: apifyToken.value() });
    const input:any = {
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
    logger.info(`TikTok scraper returned ${items.length} items`);
    const batch = db.batch();
    const collectionRef = db.collection('viral_research');
    const dateKey = new Date().toISOString().split('T')[0];
    items.forEach((item:any, idx:number) => {
      // Normalize fields based on typical TikTok dataset structure (hearts, comments, shares, plays)
      const post = {
        platform: 'tiktok',
        profile: item.authorUsername || item.authorName || 'unknown',
        postId: item.id || item.videoId || `${dateKey}_${idx}`,
        postURL: item.url || item.shareUrl || '',
        caption: item.text || item.caption || '',
        hashtags: (item.hashtags || []).map((h:any)=> `#${h.name || h}`),
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
            return (( (item.diggCount||0) + (item.commentCount||0) + (item.shareCount||0) ) / followers) * 100;
        })(),
        scrapedAt: new Date().toISOString(),
        source: 'apify-tiktok'
      };
      const docRef = collectionRef.doc(`${dateKey}_tiktok_${post.postId}`);
      batch.set(docRef, post, { merge: true });
    });
    await batch.commit();
    logger.info('Daily TikTok scraper completed.');
  } catch (e:any) {
    logger.error('Daily TikTok scraper failed', e);
    throw e;
  }
});

/**
 * Triggered when new viral research data is added to Firestore
 * Starts the agent analysis pipeline
 */
export const processViralPost = onDocumentCreated({
  document: 'viral_research/{docId}',
  secrets: [googleApiKey, serviceAccountJson, geminiApiKey]
}, async (event) => {
  const snapshot = event.data;
  if (!snapshot) {
    logger.warn('No data associated with the event');
    return;
  }

  const postData = snapshot.data();
  logger.info(`Processing new viral post: ${postData.postId}`, { 
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
      }});
    } catch (e:any) {
      logger.error('Failed publishing viral-post-created event', e);
    }
    
    logger.info(`Analysis request created for post ${postData.postId}`);
    
  } catch (error) {
    logger.error(`Failed to process viral post ${postData.postId}:`, error);
    throw error;
  }
});

/**
 * Extract hashtags from caption text
 */
function extractHashtags(caption: string): string[] {
  const hashtagRegex = /#[a-zA-Z0-9_]+/g;
  return caption.match(hashtagRegex) || [];
}

/**
 * Calculate engagement rate as (likes + comments) / followers * 100
 */
function calculateEngagementRate(likes: number, comments: number, followers: number): number {
  if (followers <= 0) return 0;
  return ((likes + comments) / followers) * 100;
}

/**
 * Health check endpoint for monitoring
 */
export const healthCheck = onSchedule({
  schedule: '*/5 * * * *', // Every 5 minutes
  timeZone: 'America/Toronto'
}, async (event) => {
  logger.info('Health check - all systems operational');
});

/**
 * Unified orchestration scheduled job (after scraping) to run CSE augmentation and enrichment pipeline.
 * Runs daily at 8:30 AM EST (after dailyViralScraper 8:00 AM) to allow scrape completion.
 */
export const dailyUnifiedOrchestration = onSchedule({
  schedule: '30 8 * * *',
  timeZone: 'America/Toronto',
  memory: '512MiB',
  timeoutSeconds: 300,
  secrets: [googleCseKey, googleCseCx]
}, async () => {
  const { runOrchestration } = await import('./AgentOrchestrator.js');
  logger.info('Starting unified orchestration run');
  // Fetch today's scraped posts to feed pipeline
  const dateKey = new Date().toISOString().split('T')[0];
  const snap = await db.collection('viral_research').where('scrapedAt', '>=', dateKey).limit(200).get();
  const posts:any[] = [];
  snap.forEach(doc => posts.push({ id: doc.id, ...doc.data() }));
  const result = await runOrchestration({ posts });
  logger.info('Unified orchestration completed', result);
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
    }});
  } catch (e:any) {
    logger.error('Failed publishing orchestration-completed event', e);
  }
});

/**
 * HTTP endpoint to trigger orchestration on-demand. Optional query param limit.
 */
export const runOrchestrationHttp = onRequest({ secrets: [googleCseKey, googleCseCx] }, async (req, res) => {
  try {
    const { runOrchestration } = await import('./AgentOrchestrator.js');
    const limit = Math.min(parseInt(req.query.limit as string) || 50, 500);
    const snap = await db.collection('viral_research').orderBy('scrapedAt','desc').limit(limit).get();
    const posts:any[] = [];
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
      }});
    } catch (e:any) {
      logger.error('Failed publishing orchestration-completed (http) event', e);
    }
    res.status(200).json(result);
  } catch (e:any) {
    logger.error('On-demand orchestration failed', e);
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
export const manualOrchestrate = onRequest({ secrets: [googleCseKey, googleCseCx] }, async (req, res) => {
  try {
    const { runOrchestration } = await import('./AgentOrchestrator.js');
    const limit = Math.min(parseInt(req.query.limit as string) || 50, 500);
    const queryTerm = (req.query.queryTerm as string) || '';
    const disableCse = /^true$/i.test((req.query.disableCse as string) || '');

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

    const snap = await db.collection('viral_research').orderBy('scrapedAt','desc').limit(limit).get();
    const posts:any[] = [];
    snap.forEach(doc => posts.push({ id: doc.id, ...doc.data() }));
    const result = await runOrchestration({ posts });

    // Restore env modifications
    if (queryTerm) process.env.GOOGLE_CSE_TERMS = originalTerms;
    if (disableCse) process.env.ENABLE_GLOBAL_CSE = originalEnable;

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
      }});
    } catch (e:any) {
      logger.error('Failed publishing orchestration-completed (manual) event', e);
    }

    res.status(200).json(result);
  } catch (e:any) {
    logger.error('Manual orchestration failed', e);
    res.status(500).json({ error: e.message });
  }
});

/**
 * Scheduled Daily Digest at 12:00 PM America/Toronto
 */
export const dailyBrief = onSchedule({
  schedule: '0 12 * * *',
  timeZone: 'America/Toronto',
  memory: '512MiB',
  timeoutSeconds: 180
}, async () => {
  try {
    await generateDailyDigest();
  } catch (e:any) {
    logger.error('dailyBrief generation failed', e);
    throw e;
  }
});

/**
 * Chat endpoint with persistent session memory & intent classification.
 * POST body: { sessionId?: string, message: string }
 */
export const chatCommand = onRequest({ secrets: [geminiApiKey] }, async (req, res) => {
  if (req.method !== 'POST') { res.status(405).json({ error: 'POST only' }); return; }
  const { sessionId: providedId, message } = req.body || {};
  if (!message) { res.status(400).json({ error: 'message required' }); return; }
  const sessionId = providedId || `sess_${Date.now().toString(36)}`;
  const existing = await loadChatSession(sessionId) || { messages: [], summaries: [] };
  existing.messages.push({ role: 'user', content: message, at: new Date().toISOString() });
  // Classify intent
  let intentRaw: string = '';
  try { intentRaw = await classifyIntent(message, geminiApiKey.value()); } catch (e:any) { logger.error('Intent classification failed', e); }
  let intent = 'UNKNOWN';
  try { intent = JSON.parse(intentRaw).intent || 'UNKNOWN'; } catch {}
  // Optionally trigger orchestration
  let orchestrationResult: any = null;
  if (intent === 'ORCHESTRATE') {
    try {
      const { runOrchestration } = await import('./AgentOrchestrator.js');
      const snap = await db.collection('viral_research').orderBy('scrapedAt','desc').limit(40).get();
      const posts:any[] = []; snap.forEach(doc => posts.push({ id: doc.id, ...doc.data() }));
      orchestrationResult = await runOrchestration({ posts });
    } catch (e:any) {
      logger.error('Chat-triggered orchestration failed', e); }
  }
  // Summarize every 12 messages
  if (existing.messages.length % 12 === 0) {
    try {
      const summary = await summarizeConversation(existing.messages.slice(-50), geminiApiKey.value());
      existing.summaries.push({ summary, at: new Date().toISOString() });
    } catch (e:any) { logger.error('Conversation summarization failed', e); }
  }
  existing.messages.push({ role: 'system', content: `Intent: ${intent}`, at: new Date().toISOString() });
  await storeChatSession(sessionId, existing);
  res.status(200).json({ sessionId, intent, orchestrationResult });
});
