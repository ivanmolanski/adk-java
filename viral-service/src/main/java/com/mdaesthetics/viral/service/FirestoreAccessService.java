package com.mdaesthetics.viral.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.mdaesthetics.viral.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Thin Firestore abstraction; stores records as maps, converting Instant to epoch millis.
 */
@Service
public class FirestoreAccessService {
    private static final Logger log = LoggerFactory.getLogger(FirestoreAccessService.class);

    private Firestore db() { return FirestoreClient.getFirestore(); }

    // ---- CompetitorPost ----
    public CompetitorPost saveCompetitorPost(CompetitorPost post) {
        try {
            Map<String,Object> data = new HashMap<>();
            data.put("platform", post.platform());
            data.put("profile", post.profile());
            data.put("postUrl", post.postUrl());
            data.put("caption", post.caption());
            data.put("hashtags", post.hashtags());
            data.put("likes", post.likes());
            data.put("comments", post.comments());
            data.put("shares", post.shares());
            data.put("views", post.views());
            data.put("engagementRate", post.engagementRate());
            data.put("evs", post.evs());
            data.put("postedAt", post.postedAt()==null?null:post.postedAt().toEpochMilli());
            data.put("scrapedAt", post.scrapedAt()==null?null:post.scrapedAt().toEpochMilli());
            DocumentReference ref = db().collection(FirestoreCollections.COMPETITOR_POSTS).document();
            ref.set(data);
            return post.withId(ref.getId());
        } catch (Exception e) {
            log.error("Failed to save CompetitorPost", e);
            return post; // id null signals failure
        }
    }

    public Optional<CompetitorPost> getCompetitorPost(String id) {
        try {
            DocumentSnapshot snap = db().collection(FirestoreCollections.COMPETITOR_POSTS).document(id).get().get();
            if (!snap.exists()) return Optional.empty();
            return Optional.of(mapCompetitorPost(snap));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Fetch competitorPost failed id={} msg={}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public List<CompetitorPost> listRecentCompetitorPosts(int limit) {
        try {
            ApiFuture<QuerySnapshot> fut = db().collection(FirestoreCollections.COMPETITOR_POSTS)
                .orderBy("scrapedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
            return fut.get().getDocuments().stream().map(this::mapCompetitorPost).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("listRecentCompetitorPosts error {}", e.getMessage(), e);
            return List.of();
        }
    }

    private CompetitorPost mapCompetitorPost(DocumentSnapshot snap) {
        return new CompetitorPost(
            snap.getId(),
            snap.getString("platform"),
            snap.getString("profile"),
            snap.getString("postUrl"),
            snap.getString("caption"),
            (List<String>) snap.get("hashtags"),
            getLong(snap, "likes"),
            getLong(snap, "comments"),
            getLong(snap, "shares"),
            getLong(snap, "views"),
            getDouble(snap, "engagementRate"),
            getDouble(snap, "evs"),
            toInstant(snap.getLong("postedAt")),
            toInstant(snap.getLong("scrapedAt"))
        );
    }

    // ---- TrendAnalysis ----
    public TrendAnalysis saveTrendAnalysis(TrendAnalysis ta) {
        try {
            Map<String,Object> data = new HashMap<>();
            data.put("competitorPostId", ta.competitorPostId());
            data.put("category", ta.category());
            data.put("hook", ta.hook());
            data.put("callToAction", ta.callToAction());
            data.put("educationalPoint", ta.educationalPoint());
            data.put("extractedHashtags", ta.extractedHashtags());
            data.put("viralityScore", ta.viralityScore());
            data.put("relevanceScore", ta.relevanceScore());
            data.put("rawAgentJson", ta.rawAgentJson());
            data.put("analyzedAt", ta.analyzedAt()==null?null:ta.analyzedAt().toEpochMilli());
            DocumentReference ref = db().collection(FirestoreCollections.TREND_ANALYSES).document();
            ref.set(data);
            return ta.withId(ref.getId());
        } catch (Exception e) {
            log.error("Failed to save TrendAnalysis", e);
            return ta; // null id signals failure
        }
    }

    public List<TrendAnalysis> listRecentTrendAnalyses(int limit) {
        try {
            ApiFuture<QuerySnapshot> fut = db().collection(FirestoreCollections.TREND_ANALYSES)
                .orderBy("analyzedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
            return fut.get().getDocuments().stream().map(this::mapTrendAnalysis).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("listRecentTrendAnalyses error {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * List trend analyses with cursor-based pagination.
     * @param limit Maximum number of items to return (1-50)
     * @param cursor ISO timestamp string for pagination (optional)
     * @return List of trend analyses
     */
    public PaginatedResult<TrendAnalysis> listTrendAnalysesWithCursor(int limit, String cursor) {
        try {
            Query query = db().collection(FirestoreCollections.TREND_ANALYSES)
                .orderBy("analyzedAt", Query.Direction.DESCENDING);

            if (cursor != null && !cursor.isEmpty()) {
                try {
                    Instant cursorInstant = Instant.parse(cursor);
                    query = query.startAfter(cursorInstant.toEpochMilli());
                } catch (Exception e) {
                    log.warn("Invalid cursor format: {}, ignoring", cursor);
                }
            }

            ApiFuture<QuerySnapshot> fut = query.limit(Math.min(limit + 1, 51)).get();
            List<QueryDocumentSnapshot> docs = fut.get().getDocuments();

            boolean hasMore = docs.size() > limit;
            List<TrendAnalysis> items = docs.stream()
                .limit(limit)
                .map(this::mapTrendAnalysis)
                .collect(Collectors.toList());

            String nextCursor = null;
            if (hasMore && !items.isEmpty()) {
                TrendAnalysis last = items.get(items.size() - 1);
                nextCursor = last.analyzedAt() != null ? last.analyzedAt().toString() : null;
            }

            return new PaginatedResult<>(items, nextCursor, hasMore);
        } catch (Exception e) {
            log.error("listTrendAnalysesWithCursor error {}", e.getMessage(), e);
            return new PaginatedResult<>(List.of(), null, false);
        }
    }

    /** Direct get by TrendAnalysis document id. */
    public Optional<TrendAnalysis> getTrendAnalysis(String id) {
        try {
            DocumentSnapshot snap = db().collection(FirestoreCollections.TREND_ANALYSES).document(id).get().get();
            if(!snap.exists()) return Optional.empty();
            return Optional.of(mapTrendAnalysis(snap));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Fetch trendAnalysis failed id={} msg={}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /** Direct indexed lookup of a TrendAnalysis by competitorPostId (most recent if multiple). */
    public Optional<TrendAnalysis> findLatestTrendAnalysisForCompetitorPost(String competitorPostId) {
        try {
            Query q = db().collection(FirestoreCollections.TREND_ANALYSES)
                .whereEqualTo("competitorPostId", competitorPostId)
                .orderBy("analyzedAt", Query.Direction.DESCENDING)
                .limit(1);
            List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
            if (docs.isEmpty()) return Optional.empty();
            return Optional.of(mapTrendAnalysis(docs.get(0)));
        } catch (Exception e) {
            log.error("findLatestTrendAnalysisForCompetitorPost error postId={} msg={}", competitorPostId, e.getMessage());
            return Optional.empty();
        }
    }

    private TrendAnalysis mapTrendAnalysis(DocumentSnapshot snap) {
        return new TrendAnalysis(
            snap.getId(),
            snap.getString("competitorPostId"),
            snap.getString("category"),
            snap.getString("hook"),
            snap.getString("callToAction"),
            snap.getString("educationalPoint"),
            castStringList(snap.get("extractedHashtags")),
            getDouble(snap, "viralityScore"),
            getDouble(snap, "relevanceScore"),
            snap.getString("rawAgentJson"),
            toInstant(snap.getLong("analyzedAt"))
        );
    }

    // ---- ContentDraft ----
    public ContentDraft saveContentDraft(ContentDraft draft) {
        try {
            Map<String,Object> data = new HashMap<>();
            data.put("trendAnalysisId", draft.trendAnalysisId());
            data.put("focusService", draft.focusService());
            data.put("hook", draft.hook());
            data.put("body", draft.body());
            data.put("hashtags", draft.hashtags());
            data.put("callToAction", draft.callToAction());
            data.put("complianceChecked", draft.complianceChecked());
            data.put("compliancePassed", draft.compliancePassed());
            data.put("complianceNotes", draft.complianceNotes());
            data.put("createdAt", draft.createdAt()==null?null:draft.createdAt().toEpochMilli());
            DocumentReference ref = db().collection(FirestoreCollections.CONTENT_DRAFTS).document();
            ref.set(data);
            return draft.withId(ref.getId());
        } catch (Exception e) {
            log.error("Failed to save ContentDraft", e);
            return draft;
        }
    }

    public List<ContentDraft> listRecentContentDrafts(int limit) {
        try {
            ApiFuture<QuerySnapshot> fut = db().collection(FirestoreCollections.CONTENT_DRAFTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
            return fut.get().getDocuments().stream().map(this::mapContentDraft).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("listRecentContentDrafts error {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * List content drafts with cursor-based pagination.
     * @param limit Maximum number of items to return (1-50)
     * @param cursor ISO timestamp string for pagination (optional)
     * @return Paginated result of content drafts
     */
    public PaginatedResult<ContentDraft> listContentDraftsWithCursor(int limit, String cursor) {
        try {
            Query query = db().collection(FirestoreCollections.CONTENT_DRAFTS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

            if (cursor != null && !cursor.isEmpty()) {
                try {
                    Instant cursorInstant = Instant.parse(cursor);
                    query = query.startAfter(cursorInstant.toEpochMilli());
                } catch (Exception e) {
                    log.warn("Invalid cursor format: {}, ignoring", cursor);
                }
            }

            ApiFuture<QuerySnapshot> fut = query.limit(Math.min(limit + 1, 51)).get();
            List<QueryDocumentSnapshot> docs = fut.get().getDocuments();

            boolean hasMore = docs.size() > limit;
            List<ContentDraft> items = docs.stream()
                .limit(limit)
                .map(this::mapContentDraft)
                .collect(Collectors.toList());

            String nextCursor = null;
            if (hasMore && !items.isEmpty()) {
                ContentDraft last = items.get(items.size() - 1);
                nextCursor = last.createdAt() != null ? last.createdAt().toString() : null;
            }

            return new PaginatedResult<>(items, nextCursor, hasMore);
        } catch (Exception e) {
            log.error("listContentDraftsWithCursor error {}", e.getMessage(), e);
            return new PaginatedResult<>(List.of(), null, false);
        }
    }

    /** Direct get by ContentDraft document id. */
    public Optional<ContentDraft> getContentDraft(String id) {
        try {
            DocumentSnapshot snap = db().collection(FirestoreCollections.CONTENT_DRAFTS).document(id).get().get();
            if(!snap.exists()) return Optional.empty();
            return Optional.of(mapContentDraft(snap));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Fetch contentDraft failed id={} msg={}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /** Direct indexed lookup of latest ContentDraft by trendAnalysisId. */
    public Optional<ContentDraft> findLatestContentDraftForTrendAnalysis(String trendAnalysisId) {
        try {
            Query q = db().collection(FirestoreCollections.CONTENT_DRAFTS)
                .whereEqualTo("trendAnalysisId", trendAnalysisId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1);
            List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
            if (docs.isEmpty()) return Optional.empty();
            return Optional.of(mapContentDraft(docs.get(0)));
        } catch (Exception e) {
            log.error("findLatestContentDraftForTrendAnalysis error analysisId={} msg={}", trendAnalysisId, e.getMessage());
            return Optional.empty();
        }
    }

    private ContentDraft mapContentDraft(DocumentSnapshot snap) {
        return new ContentDraft(
            snap.getId(),
            snap.getString("trendAnalysisId"),
            snap.getString("focusService"),
            snap.getString("hook"),
            snap.getString("body"),
            (List<String>) snap.get("hashtags"),
            snap.getString("callToAction"),
            getBool(snap, "complianceChecked"),
            getBool(snap, "compliancePassed"),
            snap.getString("complianceNotes"),
            toInstant(snap.getLong("createdAt"))
        );
    }

    // ---- DailyBrief (skeleton) ----
    public DailyBrief saveDailyBrief(DailyBrief brief) {
        try {
            Map<String,Object> data = new HashMap<>();
            data.put("date", brief.date()==null?null:brief.date().toEpochMilli());
            data.put("topTrendAnalysisIds", brief.topTrendAnalysisIds());
            data.put("topContentDraftIds", brief.topContentDraftIds());
            data.put("htmlBody", brief.htmlBody());
            data.put("sent", brief.sent());
            data.put("sentAt", brief.sentAt()==null?null:brief.sentAt().toEpochMilli());
            DocumentReference ref = db().collection(FirestoreCollections.DAILY_BRIEFS).document();
            ref.set(data);
            return brief.withId(ref.getId());
        } catch (Exception e) {
            log.error("Failed to save DailyBrief", e);
            return brief;
        }
    }

    public List<DailyBrief> listRecentDailyBriefs(int limit) {
        try {
            ApiFuture<QuerySnapshot> fut = db().collection(FirestoreCollections.DAILY_BRIEFS)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
            return fut.get().getDocuments().stream().map(this::mapDailyBrief).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("listRecentDailyBriefs error {}", e.getMessage(), e);
            return List.of();
        }
    }

    private DailyBrief mapDailyBrief(DocumentSnapshot snap) {
        return new DailyBrief(
            snap.getId(),
            toInstant(snap.getLong("date")),
            castStringList(snap.get("topTrendAnalysisIds")),
            castStringList(snap.get("topContentDraftIds")),
            snap.getString("htmlBody"),
            getBool(snap, "sent"),
            toInstant(snap.getLong("sentAt"))
        );
    }

    // ---- Helpers ----
    private Instant toInstant(Long v) { return v==null?null: Instant.ofEpochMilli(v); }
    private Long getLong(DocumentSnapshot s, String f){ Object o = s.get(f); return o instanceof Number ? ((Number)o).longValue() : null; }
    private Double getDouble(DocumentSnapshot s, String f){ Object o = s.get(f); return o instanceof Number ? ((Number)o).doubleValue() : null; }
    private boolean getBool(DocumentSnapshot s, String f){ Object o = s.get(f); return o instanceof Boolean ? (Boolean)o : false; }
    private List<String> castStringList(Object o){
        if(o==null) return List.of();
        if(o instanceof List<?> l){
            List<String> out = new ArrayList<>();
            for(Object e : l){ if(e!=null) out.add(String.valueOf(e)); }
            return out;
        }
        return List.of();
    }
}
