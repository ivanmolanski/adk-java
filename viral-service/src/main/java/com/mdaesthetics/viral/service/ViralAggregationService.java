package com.mdaesthetics.viral.service;

import com.mdaesthetics.viral.dto.DraftDetailDto;
import com.mdaesthetics.viral.dto.TrendDetailDto;
import com.mdaesthetics.viral.model.CompetitorPost;
import com.mdaesthetics.viral.model.ContentDraft;
import com.mdaesthetics.viral.model.TrendAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Aggregates multi-entity views for detail endpoints.
 */
@Service
public class ViralAggregationService {
    private static final Logger log = LoggerFactory.getLogger(ViralAggregationService.class);

    private final FirestoreAccessService firestore;

    public ViralAggregationService(FirestoreAccessService firestore) {
        this.firestore = firestore;
    }

    /**
     * Build TrendDetailDto from a TrendAnalysis id, including its CompetitorPost summary and latest ContentDraft.
     */
    public Optional<TrendDetailDto> buildTrendDetail(String trendAnalysisId) {
        // Direct lookup via FirestoreAccessService (O(1) document fetch).
        TrendAnalysis trend = firestore.getTrendAnalysis(trendAnalysisId).orElse(null);
        if (trend == null) return Optional.empty();

        CompetitorPost post = firestore.getCompetitorPost(trend.competitorPostId()).orElse(null);
        ContentDraft latestDraft = firestore.findLatestContentDraftForTrendAnalysis(trend.id()).orElse(null);

        TrendDetailDto dto = new TrendDetailDto(
                trend.id(),
                trend.category(),
                trend.hook(),
                trend.callToAction(),
                trend.educationalPoint(),
                trend.extractedHashtags(),
                trend.viralityScore(),
                trend.relevanceScore(),
                trend.analyzedAt(),
                post == null ? null : post.platform(),
                post == null ? null : post.profile(),
                post == null ? null : post.postUrl(),
                post == null ? null : post.likes(),
                post == null ? null : post.comments(),
                post == null ? null : post.shares(),
                post == null ? null : post.views(),
                post == null ? null : post.engagementRate(),
                post == null ? null : post.evs(),
                post == null ? null : post.postedAt(),
                latestDraft == null ? null : latestDraft.id(),
                latestDraft == null ? null : latestDraft.focusService(),
                latestDraft == null ? null : latestDraft.hook(),
                latestDraft == null ? null : latestDraft.callToAction(),
                latestDraft == null ? null : latestDraft.compliancePassed(),
                latestDraft == null ? null : latestDraft.createdAt()
        );
        return Optional.of(dto);
    }

    /**
     * Build DraftDetailDto from a ContentDraft id including TrendAnalysis summary.
     */
    public Optional<DraftDetailDto> buildDraftDetail(String draftId) {
        // Direct lookup of draft.
        ContentDraft draft = firestore.getContentDraft(draftId).orElse(null);
        if (draft == null) return Optional.empty();
        TrendAnalysis trend = firestore.getTrendAnalysis(draft.trendAnalysisId()).orElse(null);
        DraftDetailDto dto = new DraftDetailDto(
                draft.id(),
                draft.focusService(),
                draft.hook(),
                draft.body(),
                draft.hashtags(),
                draft.callToAction(),
                draft.compliancePassed(),
                draft.createdAt(),
                draft.trendAnalysisId(),
                trend == null ? null : trend.category(),
                trend == null ? null : trend.hook(),
                trend == null ? null : trend.viralityScore(),
                trend == null ? null : trend.relevanceScore()
        );
        return Optional.of(dto);
    }

    /** Sample fallback TrendDetail if Firestore unavailable */
    public TrendDetailDto sampleTrendDetail() {
        return new TrendDetailDto(
                "trend_sample_1",
                "Science Explained",
                "How BBL light penetrates 7 skin layers",
                "Book your consultation",
                "BBL uses selective broadband light to target pigment & vascular components",
                java.util.List.of("#bblscience", "#torontoaesthetics", "#mdaesthetics"),
                0.82,
                0.91,
                Instant.now().minusSeconds(3600),
                "instagram",
                "@skinvitality",
                "https://instagram.com/p/sample123",
                1200L, 85L, 30L, 9500L,
                3.1,
                0.87,
                Instant.now().minusSeconds(7200),
                "draft_sample_1",
                "SkinTyte",
                "Infrared tightening without downtime",
                "DM to see if you're a candidate",
                true,
                Instant.now().minusSeconds(1800)
        );
    }

    /** Sample fallback DraftDetail */
    public DraftDetailDto sampleDraftDetail() {
        return new DraftDetailDto(
                "draft_sample_1",
                "SkinTyte",
                "Infrared tightening without downtime",
                "We pair SkinTyte with physician-guided collagen stimulation to firm & smooth without needles.",
                java.util.List.of("#skintyte", "#firmandsmooth", "#mdaesthetics"),
                "Book a consult to assess laxity stage & customize your protocol.",
                true,
                Instant.now().minusSeconds(1200),
                "trend_sample_1",
                "Science Explained",
                "How BBL light penetrates 7 skin layers",
                0.82,
                0.91
        );
    }
}
