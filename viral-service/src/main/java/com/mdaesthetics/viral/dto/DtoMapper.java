package com.mdaesthetics.viral.dto;

import com.mdaesthetics.viral.model.ContentDraft;
import com.mdaesthetics.viral.model.TrendAnalysis;

public class DtoMapper {
    public static TrendAnalysisDto toDto(TrendAnalysis t){
        return new TrendAnalysisDto(t.id(), t.competitorPostId(), t.category(), t.hook(), t.callToAction(), t.educationalPoint(), t.extractedHashtags(), t.viralityScore(), t.relevanceScore(), t.analyzedAt());
    }
    public static ContentDraftDto toDto(ContentDraft d){
        return new ContentDraftDto(d.id(), d.trendAnalysisId(), d.focusService(), d.hook(), d.body(), d.hashtags(), d.callToAction(), d.complianceChecked(), d.compliancePassed(), d.complianceNotes(), d.createdAt());
    }
}
