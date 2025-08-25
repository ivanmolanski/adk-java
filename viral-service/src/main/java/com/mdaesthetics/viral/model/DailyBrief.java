package com.mdaesthetics.viral.model;

import java.time.Instant;
import java.util.List;

/**
 * Aggregated daily email digest content prior to dispatch.
 */
public record DailyBrief(
    String id,
    Instant date,
    List<String> topTrendAnalysisIds,
    List<String> topContentDraftIds,
    String htmlBody,
    boolean sent,
    Instant sentAt
) {
    public DailyBrief withId(String newId) {
        return new DailyBrief(newId, date, topTrendAnalysisIds, topContentDraftIds, htmlBody, sent, sentAt);
    }
}
