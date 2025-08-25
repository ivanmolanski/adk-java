package com.mdaesthetics.viral.model;

import java.time.Instant;
import java.util.List;

/**
 * Generated draft content tailored for MDAesthetics brand.
 */
public record ContentDraft(
    String id,
    String trendAnalysisId,
    String focusService,           // e.g., Duo-C-Lift, SkinTyte, etc.
    String hook,
    String body,
    List<String> hashtags,
    String callToAction,
    boolean complianceChecked,
    boolean compliancePassed,
    String complianceNotes,
    Instant createdAt
) {
    public ContentDraft withId(String newId) {
        return new ContentDraft(newId, trendAnalysisId, focusService, hook, body, hashtags, callToAction, complianceChecked, compliancePassed, complianceNotes, createdAt);
    }
}
