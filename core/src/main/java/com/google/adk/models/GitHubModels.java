/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.adk.models;

import static com.google.common.base.StandardSystemProperty.JAVA_VERSION;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.genai.types.Blob;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.FinishReason;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the GitHub Models inference API for LLM access.
 *
 * <p>This class provides access to GitHub's free inference API which is OpenAI-compatible
 * and accessible to all GitHub users with a Personal Access Token or GITHUB_TOKEN.
 * 
 * <p>Supports models like "openai/gpt-4o", "deepseek/deepseek-r1", "meta-llama/llama-3.3-70b-instruct", etc.
 */
public class GitHubModels extends BaseLlm {

  private static final Logger logger = LoggerFactory.getLogger(GitHubModels.class);
  private static final String DEFAULT_BASE_URL = "https://models.github.ai/inference/chat/completions";
  private static final ImmutableMap<String, String> TRACKING_HEADERS;

  static {
    String frameworkLabel = "google-adk/" + Version.JAVA_ADK_VERSION;
    String languageLabel = "gl-java/" + JAVA_VERSION.value();
    String versionHeaderValue = String.format("%s %s", frameworkLabel, languageLabel);

    TRACKING_HEADERS =
        ImmutableMap.of(
            "x-goog-api-client", versionHeaderValue,
            "user-agent", versionHeaderValue);
  }

  private final String baseUrl;
  private final String token;
  private final ObjectMapper objectMapper;

  /**
   * Constructs a new GitHubModels instance.
   *
   * @param modelName The name of the model to use (e.g., "openai/gpt-4o").
   * @param token The GitHub Personal Access Token or GITHUB_TOKEN.
   */
  public GitHubModels(String modelName, String token) {
    this(modelName, token, DEFAULT_BASE_URL);
  }

  /**
   * Constructs a new GitHubModels instance with custom base URL.
   *
   * @param modelName The name of the model to use (e.g., "openai/gpt-4o").
   * @param token The GitHub Personal Access Token or GITHUB_TOKEN.
   * @param baseUrl The base URL for the GitHub Models API.
   */
  public GitHubModels(String modelName, String token, String baseUrl) {
    super(modelName);
    this.token = Objects.requireNonNull(token, "token cannot be null");
    this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Returns a new Builder instance for constructing GitHubModels objects.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link GitHubModels}. */
  public static class Builder {
    private String modelName;
    private String token;
    private String baseUrl = DEFAULT_BASE_URL;

    private Builder() {}

    /**
     * Sets the name of the model to use.
     *
     * @param modelName The model name (e.g., "openai/gpt-4o").
     * @return This builder.
     */
    @CanIgnoreReturnValue
    public Builder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    /**
     * Sets the GitHub token for authentication.
     *
     * @param token The GitHub Personal Access Token or GITHUB_TOKEN.
     * @return This builder.
     */
    @CanIgnoreReturnValue
    public Builder token(String token) {
      this.token = token;
      return this;
    }

    /**
     * Sets the base URL for the GitHub Models API.
     *
     * @param baseUrl The base URL.
     * @return This builder.
     */
    @CanIgnoreReturnValue
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /**
     * Builds the {@link GitHubModels} instance.
     *
     * @return A new {@link GitHubModels} instance.
     * @throws NullPointerException if modelName or token is null.
     */
    public GitHubModels build() {
      Objects.requireNonNull(modelName, "modelName must be set.");
      Objects.requireNonNull(token, "token must be set.");
      
      return new GitHubModels(modelName, token, baseUrl);
    }
  }

  @Override
  public Flowable<LlmResponse> generateContent(LlmRequest llmRequest, boolean stream) {
    logger.debug("Generating content with GitHub Models, model: {}, stream: {}", model(), stream);
    
    try {
      String requestBody = createOpenAIRequest(llmRequest, stream);
      String responseBody = makeHttpRequest(requestBody);
      LlmResponse response = parseOpenAIResponse(responseBody);
      
      return Flowable.just(response);
    } catch (Exception e) {
      logger.error("Error generating content with GitHub Models", e);
      return Flowable.error(e);
    }
  }

  @Override
  public BaseLlmConnection connect(LlmRequest llmRequest) {
    // For now, return a basic connection that delegates to generateContent
    return new GitHubModelsConnection(this, llmRequest);
  }

  /**
   * Creates an OpenAI-compatible request body from the LlmRequest.
   */
  private String createOpenAIRequest(LlmRequest llmRequest, boolean stream) throws Exception {
    Map<String, Object> request = new HashMap<>();
    
    // Use the model from the request if specified, otherwise use the default model
    String effectiveModel = llmRequest.model().orElse(model());
    request.put("model", effectiveModel);
    
    // Convert contents to OpenAI messages format
    List<Map<String, Object>> messages = convertContentsToMessages(llmRequest.contents());
    request.put("messages", messages);
    
    // Add system instruction if present
    llmRequest.getFirstSystemInstruction().ifPresent(instruction -> {
      Map<String, Object> systemMessage = new HashMap<>();
      systemMessage.put("role", "system");
      systemMessage.put("content", instruction);
      messages.add(0, systemMessage); // Add system message at the beginning
    });
    
    // Set streaming
    request.put("stream", stream);
    
    // Add generation config parameters if present
    llmRequest.config().ifPresent(config -> {
      config.maxOutputTokens().ifPresent(max -> request.put("max_tokens", max));
      config.temperature().ifPresent(temp -> request.put("temperature", temp));
      config.topP().ifPresent(topP -> request.put("top_p", topP));
      config.stopSequences().ifPresent(stops -> {
        if (!stops.isEmpty()) {
          request.put("stop", stops);
        }
      });
    });
    
    return objectMapper.writeValueAsString(request);
  }

  /**
   * Converts GenAI Content objects to OpenAI messages format.
   */
  private List<Map<String, Object>> convertContentsToMessages(List<Content> contents) {
    List<Map<String, Object>> messages = new ArrayList<>();
    
    for (Content content : contents) {
      Map<String, Object> message = new HashMap<>();
      
      // Determine role based on content (simplified logic)
      String role = "user"; // Default to user role
      if (content.role().isPresent()) {
        String contentRole = content.role().get();
        if ("model".equals(contentRole)) {
          role = "assistant";
        } else if ("system".equals(contentRole)) {
          role = "system";
        }
      }
      message.put("role", role);
      
      // Convert parts to content
      if (content.parts().isPresent()) {
        List<Part> parts = content.parts().get();
        if (parts.size() == 1 && parts.get(0).text().isPresent()) {
          // Simple text message
          message.put("content", parts.get(0).text().get());
        } else {
          // Complex content with multiple parts (handle text only for now)
          StringBuilder contentBuilder = new StringBuilder();
          for (Part part : parts) {
            part.text().ifPresent(contentBuilder::append);
          }
          message.put("content", contentBuilder.toString());
        }
      }
      
      messages.add(message);
    }
    
    return messages;
  }

  /**
   * Makes an HTTP request to the GitHub Models API.
   */
  private String makeHttpRequest(String requestBody) throws Exception {
    URL url = new URL(baseUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    
    // Set request method and headers
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    conn.setRequestProperty("Authorization", "Bearer " + token);
    
    // Add tracking headers
    TRACKING_HEADERS.forEach(conn::setRequestProperty);
    
    // Set timeouts
    conn.setConnectTimeout(15000);
    conn.setReadTimeout(30000);
    
    // Write request body
    try (OutputStream os = conn.getOutputStream()) {
      os.write(requestBody.getBytes(StandardCharsets.UTF_8));
      os.flush();
    }
    
    // Read response
    int responseCode = conn.getResponseCode();
    InputStream inputStream = responseCode >= 200 && responseCode < 300 
        ? conn.getInputStream() 
        : conn.getErrorStream();
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, bytesRead);
    }
    
    String responseBody = baos.toString(StandardCharsets.UTF_8);
    
    if (responseCode < 200 || responseCode >= 300) {
      logger.error("GitHub Models API error: {} {}", responseCode, responseBody);
      throw new RuntimeException("GitHub Models API error: " + responseCode + " " + responseBody);
    }
    
    return responseBody;
  }

  /**
   * Parses OpenAI-compatible response into LlmResponse.
   */
  private LlmResponse parseOpenAIResponse(String responseBody) throws Exception {
    @SuppressWarnings("unchecked")
    Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
    
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
    
    if (choices == null || choices.isEmpty()) {
      throw new RuntimeException("No choices in GitHub Models response");
    }
    
    Map<String, Object> firstChoice = choices.get(0);
    @SuppressWarnings("unchecked")
    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
    
    if (message == null) {
      throw new RuntimeException("No message in GitHub Models response choice");
    }
    
    String content = (String) message.get("content");
    String finishReason = (String) firstChoice.get("finish_reason");
    
    // Convert to GenAI format - use same pattern as Gemini.java
    Part textPart = Part.builder().text(content != null ? content : "").build();
    Content responseContent = Content.builder()
        .role("model")
        .parts(ImmutableList.of(textPart))
        .build();
    
    // Map finish reason
    FinishReason mappedFinishReason = mapFinishReason(finishReason);
    
    // Create candidate and response like in Gemini.java
    com.google.genai.types.Candidate candidate = com.google.genai.types.Candidate.builder()
        .content(responseContent)
        .finishReason(mappedFinishReason)
        .build();
    
    com.google.genai.types.GenerateContentResponse genaiResponse = 
        com.google.genai.types.GenerateContentResponse.builder()
            .candidates(ImmutableList.of(candidate))
            .build();
    
    return LlmResponse.create(genaiResponse);
  }

  /**
   * Maps OpenAI finish reason to GenAI FinishReason.
   */
  private FinishReason mapFinishReason(String openAIFinishReason) {
    if (openAIFinishReason == null) {
      return new FinishReason(FinishReason.Known.STOP);
    }
    
    return switch (openAIFinishReason) {
      case "stop" -> new FinishReason(FinishReason.Known.STOP);
      case "length" -> new FinishReason(FinishReason.Known.MAX_TOKENS);
      case "content_filter" -> new FinishReason(FinishReason.Known.SAFETY);
      case "tool_calls" -> new FinishReason(FinishReason.Known.STOP); // Treat tool calls as normal completion
      default -> new FinishReason(FinishReason.Known.OTHER);
    };
  }

  /**
   * Simple connection class for GitHub Models.
   */
  private static class GitHubModelsConnection implements BaseLlmConnection {
    private final GitHubModels gitHubModels;
    private final LlmRequest initialRequest;

    public GitHubModelsConnection(GitHubModels gitHubModels, LlmRequest initialRequest) {
      this.gitHubModels = gitHubModels;
      this.initialRequest = initialRequest;
    }

    @Override
    public Completable sendHistory(List<Content> history) {
      // For now, just return a completed completable
      return Completable.complete();
    }

    @Override
    public Completable sendContent(Content content) {
      // For now, just return a completed completable
      return Completable.complete();
    }

    @Override
    public Completable sendRealtime(Blob blob) {
      // GitHub Models doesn't support realtime, return error
      return Completable.error(new UnsupportedOperationException("Realtime not supported by GitHub Models"));
    }

    @Override
    public Flowable<LlmResponse> receive() {
      return gitHubModels.generateContent(initialRequest, false);
    }

    @Override
    public void close() {
      // No persistent connection to close for GitHub Models
    }

    @Override
    public void close(Throwable throwable) {
      // No persistent connection to close for GitHub Models
    }
  }
}