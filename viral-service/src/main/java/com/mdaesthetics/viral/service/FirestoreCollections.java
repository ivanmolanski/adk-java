package com.mdaesthetics.viral.service;

/**
 * Central definition of Firestore collection names to keep Node Functions and Java service aligned.
 * If a name changes here it must also change in the Firebase Functions layer.
 */
public final class FirestoreCollections {
    private FirestoreCollections() {}

    public static final String COMPETITOR_POSTS = "competitorPosts"; // raw scraped competitor content
    public static final String TREND_ANALYSES = "trendAnalyses";     // structured LLM analysis results
    public static final String CONTENT_DRAFTS = "contentDrafts";     // generated MDA content drafts
    public static final String DAILY_BRIEFS = "dailyBriefs";         // assembled daily email briefs
}
