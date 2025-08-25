/**
 * Daily scheduled function to scrape competitor Instagram posts using Apify
 * Runs at 8:00 AM EST daily
 */
export declare const dailyViralScraper: import("firebase-functions/v2/scheduler").ScheduleFunction;
/**
 * Triggered when new viral research data is added to Firestore
 * Starts the agent analysis pipeline
 */
export declare const processViralPost: import("firebase-functions/core").CloudFunction<import("firebase-functions/v2/firestore").FirestoreEvent<import("firebase-functions/v2/firestore").QueryDocumentSnapshot | undefined, {
    docId: string;
}>>;
/**
 * Health check endpoint for monitoring
 */
export declare const healthCheck: import("firebase-functions/v2/scheduler").ScheduleFunction;
//# sourceMappingURL=index.d.ts.map