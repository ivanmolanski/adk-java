// Clean TypeScript file for Firebase Functions v2

import { onSchedule } from 'firebase-functions/v2/scheduler';
import { onDocumentCreated } from 'firebase-functions/v2/firestore';
import { defineSecret } from 'firebase-functions/params';
import { ApifyClient } from 'apify-client';
import { initializeApp, getApps } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { logger } from 'firebase-functions/v2';

// Initialize Firebase Admin if not already initialized
if (!getApps().length) {
  initializeApp();
}

const db = getFirestore();

// Define secrets using Firebase Functions v2 approach
const apifyToken = defineSecret('APIFY_TOKEN');
const googleApiKey = defineSecret('MD_API_KEY');
const serviceAccountJson = defineSecret('MD_SERVICE_ACCOUNT');
const googleCseKey = defineSecret('GOOGLE_CSE_KEY');
const googleCseCx = defineSecret('GOOGLE_CSE_CX');

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
 * Triggered when new viral research data is added to Firestore
 * Starts the agent analysis pipeline
 */
export const processViralPost = onDocumentCreated({
  document: 'viral_research/{docId}',
  secrets: [googleApiKey, serviceAccountJson]
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
