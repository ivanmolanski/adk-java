package com.mdaesthetics.viral.model;

import java.time.Instant;
import java.util.List;

/**
 * Output of TrendAnalyzer agent for a specific competitor post.
 */
public record TrendAnalysis(
    String id,              // Firestore doc ID
    String competitorPostId,
    String category,        // Process Demystified | Science Explained | Transformation | Myth Busting
    String hook,
    String callToAction,
    String educationalPoint,
    List<String> extractedHashtags,
    Double viralityScore,
    Double relevanceScore,
    String rawAgentJson,
    Instant analyzedAt
) {
    public TrendAnalysis withId(String newId) {
        return new TrendAnalysis(newId, competitorPostId, category, hook, callToAction, educationalPoint, extractedHashtags, viralityScore, relevanceScore, rawAgentJson, analyzedAt);
    }
}
