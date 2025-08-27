package com.mdaesthetics.viral.dto;

import java.time.Instant;
import java.util.List;

/** Lightweight projection of TrendAnalysis for API responses */
public record TrendAnalysisDto(
        String id,
        String competitorPostId,
        String category,
        String hook,
        String callToAction,
        String educationalPoint,
        List<String> extractedHashtags,
        Double viralityScore,
        Double relevanceScore,
        Instant analyzedAt
) {}
