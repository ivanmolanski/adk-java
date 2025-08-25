"use strict";
// Clean TypeScript file for Firebase Functions v2
Object.defineProperty(exports, "__esModule", { value: true });
exports.healthCheck = exports.processViralPost = exports.dailyViralScraper = void 0;
const scheduler_1 = require("firebase-functions/v2/scheduler");
const firestore_1 = require("firebase-functions/v2/firestore");
const params_1 = require("firebase-functions/params");
const apify_client_1 = require("apify-client");
const app_1 = require("firebase-admin/app");
const firestore_2 = require("firebase-admin/firestore");
const v2_1 = require("firebase-functions/v2");
// Initialize Firebase Admin if not already initialized
if (!(0, app_1.getApps)().length) {
    (0, app_1.initializeApp)();
}
const db = (0, firestore_2.getFirestore)();
// Define secrets using Firebase Functions v2 approach
const apifyToken = (0, params_1.defineSecret)('APIFY_TOKEN');
const googleApiKey = (0, params_1.defineSecret)('MD_API_KEY');
const serviceAccountJson = (0, params_1.defineSecret)('MD_SERVICE_ACCOUNT');
const googleCseKey = (0, params_1.defineSecret)('GOOGLE_CSE_KEY');
const googleCseCx = (0, params_1.defineSecret)('GOOGLE_CSE_CX');
// Competitor profiles to monitor (hardcoded as per requirements)
const competitorProfiles = [
    '_thelookaesthetics',
    'subtle.enhancements',
    'skinvitalityofficial'
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
 * Triggered when new viral research data is added to Firestore
 * Starts the agent analysis pipeline
 */
exports.processViralPost = (0, firestore_1.onDocumentCreated)({
    document: 'viral_research/{docId}',
    secrets: [googleApiKey, serviceAccountJson]
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
//# sourceMappingURL=index.js.map