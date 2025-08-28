import * as functions from 'firebase-functions';
/**
 * Daily scheduled function to scrape competitor Instagram posts using Apify
 * Runs at 8:00 AM EST daily
 */
export declare const dailyViralScraper: functions.scheduler.ScheduleFunction;
/**
 * Scheduled TikTok scraper leveraging Apify clockworks/tiktok-scraper actor.
 * Runs daily at 8:10 AM after Instagram scrape.
 */
export declare const dailyTiktokScraper: functions.scheduler.ScheduleFunction;
/**
 * Triggered when new viral research data is added to Firestore
 * Starts the agent analysis pipeline
 */
export declare const processViralPost: functions.CloudFunction<functions.firestore.FirestoreEvent<functions.firestore.QueryDocumentSnapshot | undefined, {
    docId: string;
}>>;
/**
 * Health check endpoint for monitoring
 */
export declare const healthCheck: functions.scheduler.ScheduleFunction;
/**
 * Unified orchestration scheduled job (after scraping) to run CSE augmentation and enrichment pipeline.
 * Runs daily at 8:30 AM EST (after dailyViralScraper 8:00 AM) to allow scrape completion.
 */
export declare const dailyUnifiedOrchestration: functions.scheduler.ScheduleFunction;
/**
 * HTTP endpoint to trigger orchestration on-demand. Optional query param limit.
 */
export declare const runOrchestrationHttp: functions.https.HttpsFunction;
/**
 * manualOrchestrate - Advanced command-center trigger used by AI Chat GUI.
 * Query params:
 *   limit (number) - number of recent posts to include (default 50)
 *   queryTerm (string) - optional ad-hoc strategic term to temporarily append to GOOGLE_CSE_TERMS for this run only (e.g. "BBL therapy today")
 *   disableCse=true|false - optionally bypass global CSE augmentation
 */
export declare const manualOrchestrate: functions.https.HttpsFunction;
/**
 * Scheduled Daily Digest at 12:00 PM America/Toronto
 */
export declare const dailyBrief: functions.scheduler.ScheduleFunction;
/**
 * Chat endpoint with persistent session memory & intent classification.
 * POST body: { sessionId?: string, message: string }
 */
export declare const chatCommand: functions.https.HttpsFunction;
//# sourceMappingURL=index.d.ts.map