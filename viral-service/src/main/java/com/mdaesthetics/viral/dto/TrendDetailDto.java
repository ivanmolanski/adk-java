package com.mdaesthetics.viral.dto;

import java.time.Instant;
import java.util.List;

/** Aggregated view: TrendAnalysis + CompetitorPost summary + latest Draft */
public record TrendDetailDto(
        String id,
        String category,
        String hook,
        String callToAction,
        String educationalPoint,
        List<String> extractedHashtags,
        Double viralityScore,
        Double relevanceScore,
        Instant analyzedAt,
        // competitor post summary
        String platform,
        String profile,
        String postUrl,
        Long likes,
        Long comments,
        Long shares,
        Long views,
        Double engagementRate,
        Double evs,
        Instant postedAt,
        // draft summary (may be null)
        String draftId,
        String draftFocusService,
        String draftHook,
        String draftCallToAction,
        Boolean draftCompliancePassed,
        Instant draftCreatedAt
) {}
