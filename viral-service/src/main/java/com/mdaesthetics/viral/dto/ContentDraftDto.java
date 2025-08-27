package com.mdaesthetics.viral.dto;

import java.time.Instant;
import java.util.List;

/** Projection of ContentDraft for API */
public record ContentDraftDto(
        String id,
        String trendAnalysisId,
        String focusService,
        String hook,
        String body,
        List<String> hashtags,
        String callToAction,
        boolean complianceChecked,
        boolean compliancePassed,
        String complianceNotes,
        Instant createdAt
) {}
