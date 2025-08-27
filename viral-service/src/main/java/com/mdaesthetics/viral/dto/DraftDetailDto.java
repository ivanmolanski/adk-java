package com.mdaesthetics.viral.dto;

import java.time.Instant;
import java.util.List;

/** Aggregated view: ContentDraft + TrendAnalysis summary */
public record DraftDetailDto(
        String id,
        String focusService,
        String hook,
        String body,
        List<String> hashtags,
        String callToAction,
        boolean compliancePassed,
        Instant createdAt,
        // trend summary
        String trendAnalysisId,
        String trendCategory,
        String trendHook,
        Double trendViralityScore,
        Double trendRelevanceScore
) {}
