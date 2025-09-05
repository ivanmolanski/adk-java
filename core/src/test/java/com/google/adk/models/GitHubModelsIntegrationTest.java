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

import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration tests for {@link GitHubModels}.
 * 
 * <p>These tests require a valid GITHUB_TOKEN environment variable to run.
 * If the token is not available, tests will be skipped.
 */
@RunWith(JUnit4.class)
public final class GitHubModelsIntegrationTest {

  private static final String GITHUB_TOKEN_ENV = "GITHUB_TOKEN";
  private static final String DEFAULT_MODEL = "openai/gpt-4o";

  @Test
  public void generateContent_withRealToken_returnsResponse() {
    String githubToken = System.getenv(GITHUB_TOKEN_ENV);
    Assume.assumeTrue("GITHUB_TOKEN environment variable is required for integration tests", 
                      githubToken != null && !githubToken.isEmpty());
    
    GitHubModels model = GitHubModels.builder()
        .modelName(DEFAULT_MODEL)
        .token(githubToken)
        .build();
    
    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(Part.builder().text("Say 'Hello from GitHub Models!'").build()))
        .build();
    
    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .build();
    
    Flowable<LlmResponse> responseFlowable = model.generateContent(request, false);
    
    // Test that we can get a response (this will make an actual API call if token is valid)
    responseFlowable.test()
        .assertNoErrors()
        .assertValueCount(1)
        .assertComplete();
  }

  @Test
  public void generateContent_withSystemInstruction_returnsResponse() {
    String githubToken = System.getenv(GITHUB_TOKEN_ENV);
    Assume.assumeTrue("GITHUB_TOKEN environment variable is required for integration tests", 
                      githubToken != null && !githubToken.isEmpty());
    
    GitHubModels model = GitHubModels.builder()
        .modelName(DEFAULT_MODEL)
        .token(githubToken)
        .build();
    
    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(Part.builder().text("What's 2+2?").build()))
        .build();
    
    Content systemInstruction = Content.builder()
        .role("system")
        .parts(ImmutableList.of(Part.builder().text("You are a helpful math tutor. Always explain your reasoning.").build()))
        .build();
    
    GenerateContentConfig config = GenerateContentConfig.builder()
        .systemInstruction(systemInstruction)
        .temperature(0.1f)
        .maxOutputTokens(50)
        .build();
    
    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .config(config)
        .build();
    
    Flowable<LlmResponse> responseFlowable = model.generateContent(request, false);
    
    // Test that we can get a response with system instruction
    responseFlowable.test()
        .assertNoErrors()
        .assertValueCount(1)
        .assertComplete();
  }

  @Test
  public void generateContent_withDifferentModel_returnsResponse() {
    String githubToken = System.getenv(GITHUB_TOKEN_ENV);
    Assume.assumeTrue("GITHUB_TOKEN environment variable is required for integration tests", 
                      githubToken != null && !githubToken.isEmpty());
    
    // Test with a different available model
    GitHubModels model = GitHubModels.builder()
        .modelName("meta-llama/llama-3.3-70b-instruct")
        .token(githubToken)
        .build();
    
    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(Part.builder().text("Tell me a short joke.").build()))
        .build();
    
    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .build();
    
    Flowable<LlmResponse> responseFlowable = model.generateContent(request, false);
    
    // Test that different models work
    responseFlowable.test()
        .assertNoErrors()
        .assertValueCount(1)
        .assertComplete();
  }

  @Test
  public void connection_withRealToken_worksCorrectly() {
    String githubToken = System.getenv(GITHUB_TOKEN_ENV);
    Assume.assumeTrue("GITHUB_TOKEN environment variable is required for integration tests", 
                      githubToken != null && !githubToken.isEmpty());
    
    GitHubModels model = GitHubModels.builder()
        .modelName(DEFAULT_MODEL)
        .token(githubToken)
        .build();
    
    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(Part.builder().text("Hello!").build()))
        .build();
    
    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .build();
    
    BaseLlmConnection connection = model.connect(request);
    
    try {
      // Test that we can receive responses through connection
      Flowable<LlmResponse> responseFlowable = connection.receive();
      responseFlowable.test()
          .assertNoErrors()
          .assertValueCount(1)
          .assertComplete();
    } finally {
      connection.close();
    }
  }
}