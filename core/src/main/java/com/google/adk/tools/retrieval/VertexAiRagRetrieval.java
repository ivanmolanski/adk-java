package com.google.adk.tools.retrieval;

import com.google.adk.agents.InvocationContext;
import com.google.adk.models.LlmRequest;
import com.google.adk.tools.ToolContext;
import com.google.cloud.aiplatform.v1.RagContexts;
import com.google.cloud.aiplatform.v1.RagQuery;
import com.google.cloud.aiplatform.v1.RetrieveContextsRequest;
import com.google.cloud.aiplatform.v1.RetrieveContextsRequest.VertexRagStore.RagResource;
import com.google.cloud.aiplatform.v1.RetrieveContextsResponse;
import com.google.cloud.aiplatform.v1.VertexRagServiceClient;
import com.google.common.collect.ImmutableList;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VertexAiRagRetrieval extends BaseRetrievalTool {
  private final VertexRagServiceClient vertexRagServiceClient;
  private final String parent;
  private final List<RagResource> ragResources;
  private final Double vectorDistanceThreshold;

  public VertexAiRagRetrieval(
      String name,
      String description,
      VertexRagServiceClient vertexRagServiceClient,
      String parent,
      List<RagResource> ragResources,
      Double vectorDistanceThreshold) {
    super(name, description);
    this.vertexRagServiceClient = vertexRagServiceClient;
    this.parent = parent;
    this.ragResources = ragResources;
    this.vectorDistanceThreshold = vectorDistanceThreshold;
  }

  public Single<Map<String, Object>> runAsync(Map<String, Object> params, ToolContext toolContext) {
    String query = (String) params.get("query");
    RetrieveContextsRequest.Builder requestBuilder = RetrieveContextsRequest.newBuilder()
        .setParent(parent)
        .setQuery(RagQuery.newBuilder().setText(query));
    RetrieveContextsRequest.VertexRagStore.Builder ragStoreBuilder =
        RetrieveContextsRequest.VertexRagStore.newBuilder()
            .addAllRagResources(ragResources);
    if (vectorDistanceThreshold != null) {
      ragStoreBuilder.setVectorDistanceThreshold(vectorDistanceThreshold);
    }
    requestBuilder.setVertexRagStore(ragStoreBuilder);
    RetrieveContextsRequest request = requestBuilder.build();
    return Single.fromCallable(() -> vertexRagServiceClient.retrieveContexts(request))
        .map(response -> {
          RagContexts contexts = response.getContexts();
          if (contexts.getContextsCount() > 0) {
            List<String> contextTexts = contexts.getContextsList().stream()
                .map(RagContexts.Context::getText)
                .collect(Collectors.toList());
            return Map.of("response", contextTexts);
          } else {
            return Map.of("response", "No matching result found with the config: resources: " + ragResources);
          }
        });
  }

  @Override
  public io.reactivex.rxjava3.core.Completable processLlmRequest(LlmRequest.Builder llmRequestBuilder, ToolContext toolContext) {
    // Minimal stub for test compatibility
    return io.reactivex.rxjava3.core.Completable.complete();
  }
}
