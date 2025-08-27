package com.mdaesthetics.viral.service;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.api.core.ApiService;
import com.google.api.core.ApiService.Listener;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.mdaesthetics.viral.model.CompetitorPost;
import com.mdaesthetics.viral.model.TrendAnalysis;
import com.mdaesthetics.viral.model.ContentDraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Optional pull-based subscriber for environments where push endpoints are not configured.
 * Listens to viral-post-created topic subscription and runs the workflow pipeline.
 */
@Service
@ConditionalOnProperty(name = "viral.pubsub.pull.enabled", havingValue = "true", matchIfMissing = false)
public class PubSubSubscriberService {
    private static final Logger log = LoggerFactory.getLogger(PubSubSubscriberService.class);

    @Value("${GCP_PROJECT:contentforge-ai-ygy25}")
    private String projectId;

    @Value("${VIRAL_POST_SUBSCRIPTION:viral-post-created-sub}")
    private String subscriptionId;

    private final ViralWorkflowService workflowService;
    private final FirestoreAccessService firestoreService;
    private final ObjectMapper mapper = new ObjectMapper();
    private Subscriber subscriber;

    public PubSubSubscriberService(ViralWorkflowService workflowService, FirestoreAccessService firestoreService) {
        this.workflowService = workflowService;
        this.firestoreService = firestoreService;
    }

    @PostConstruct
    public void start() {
        try {
            ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, subscriptionId);
            subscriber = Subscriber.newBuilder(subName, this::handleMessage).build();
            subscriber.addListener(new Listener() {
                @Override public void failed(ApiService.State from, Throwable failure) {
                    log.error("Pub/Sub subscriber failure", failure);
                }
            }, java.util.concurrent.Executors.newSingleThreadExecutor());
            subscriber.startAsync().awaitRunning();
            log.info("PubSubSubscriberService started for subscription {}", subName);
        } catch (Exception e) {
            log.warn("Failed to start pull subscriber (service will rely on push endpoints instead): {}", e.getMessage());
        }
    }

    private void handleMessage(PubsubMessage message, AckReplyConsumer consumer) {
        long start = System.currentTimeMillis();
        try {
            String data = message.getData().toString(StandardCharsets.UTF_8);
            Map<String,Object> json = mapper.readValue(data, Map.class);
            String postId = (String) json.get("postId");
            if (postId == null) {
                log.warn("Message missing postId, acking.");
                consumer.ack();
                return;
            }
            Optional<CompetitorPost> postOpt = firestoreService.getCompetitorPost(postId);
            if (postOpt.isEmpty()) {
                log.warn("Post {} not found in Firestore, acking.", postId);
                consumer.ack();
                return;
            }
            var result = workflowService.executePipeline(postOpt.get());
            long latency = System.currentTimeMillis() - start;
            if (result.containsKey("error")) {
                log.error("Pull workflow failed postId={} error={} latencyMs={}", postId, result.get("error"), latency);
            } else {
                log.info("Pull workflow success postId={} analysisId={} draftId={} qaPassed={} latencyMs={}", postId, result.get("trendAnalysisId"), result.get("contentDraftId"), result.get("qaPassed"), latency);
            }
            consumer.ack();
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            consumer.nack();
        }
    }

    @PreDestroy
    public void stop() {
        if (subscriber != null) {
            try {
                subscriber.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
                log.info("PubSubSubscriberService stopped");
            } catch (Exception e) {
                log.warn("Error stopping subscriber: {}", e.getMessage());
            }
        }
    }
}
