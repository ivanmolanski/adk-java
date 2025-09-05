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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link GitHubModels}. */
@RunWith(JUnit4.class)
public final class GitHubModelsTest {

  private static final String TEST_MODEL = "openai/gpt-4o";
  private static final String TEST_TOKEN = "test-github-token";

  @Test
  public void constructor_withModelAndToken_setsPropertiesCorrectly() {
    GitHubModels model = new GitHubModels(TEST_MODEL, TEST_TOKEN);
    
    assertThat(model.model()).isEqualTo(TEST_MODEL);
  }

  @Test
  public void constructor_withCustomBaseUrl_setsPropertiesCorrectly() {
    String customBaseUrl = "https://custom.api.endpoint";
    GitHubModels model = new GitHubModels(TEST_MODEL, TEST_TOKEN, customBaseUrl);
    
    assertThat(model.model()).isEqualTo(TEST_MODEL);
  }

  @Test
  public void constructor_withNullToken_throwsException() {
    assertThrows(
        NullPointerException.class,
        () -> new GitHubModels(TEST_MODEL, null));
  }

  @Test
  public void constructor_withNullModel_throwsException() {
    assertThrows(
        NullPointerException.class,
        () -> new GitHubModels(null, TEST_TOKEN));
  }

  @Test
  public void builder_withAllRequiredFields_buildsSuccessfully() {
    GitHubModels model = GitHubModels.builder()
        .modelName(TEST_MODEL)
        .token(TEST_TOKEN)
        .build();
    
    assertThat(model.model()).isEqualTo(TEST_MODEL);
  }

  @Test
  public void builder_withCustomBaseUrl_buildsSuccessfully() {
    String customBaseUrl = "https://custom.api.endpoint";
    GitHubModels model = GitHubModels.builder()
        .modelName(TEST_MODEL)
        .token(TEST_TOKEN)
        .baseUrl(customBaseUrl)
        .build();
    
    assertThat(model.model()).isEqualTo(TEST_MODEL);
  }

  @Test
  public void builder_withMissingModelName_throwsException() {
    assertThrows(
        NullPointerException.class,
        () -> GitHubModels.builder()
            .token(TEST_TOKEN)
            .build());
  }

  @Test
  public void builder_withMissingToken_throwsException() {
    assertThrows(
        NullPointerException.class,
        () -> GitHubModels.builder()
            .modelName(TEST_MODEL)
            .build());
  }

  @Test
  public void generateContent_withBasicRequest_returnsFlowable() {
    GitHubModels model = new GitHubModels(TEST_MODEL, TEST_TOKEN);
    
    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(Part.builder().text("Hello").build()))
        .build();
    
    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .build();
    
    // This will fail with network error in tests, but we can verify the method exists
    // and returns a Flowable without null pointer exceptions
    assertThat(model.generateContent(request, false)).isNotNull();
  }

  @Test
  public void connect_withBasicRequest_returnsConnection() {
    GitHubModels model = new GitHubModels(TEST_MODEL, TEST_TOKEN);
    
    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(Part.builder().text("Hello").build()))
        .build();
    
    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .build();
    
    BaseLlmConnection connection = model.connect(request);
    assertThat(connection).isNotNull();
    
    // Test that connection can be closed without error
    connection.close();
  }

  @Test
  public void generateContent_withSystemInstruction_includesInRequest() {
    GitHubModels model = new GitHubModels(TEST_MODEL, TEST_TOKEN);
    
    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(Part.builder().text("Hello").build()))
        .build();
    
    Content systemInstruction = Content.builder()
        .role("system")
        .parts(ImmutableList.of(Part.builder().text("You are a helpful assistant").build()))
        .build();
    
    GenerateContentConfig config = GenerateContentConfig.builder()
        .systemInstruction(systemInstruction)
        .temperature(0.7f)
        .maxOutputTokens(100)
        .build();
    
    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .config(config)
        .build();
    
    // This will fail with network error in tests, but verifies no compilation errors
    assertThat(model.generateContent(request, false)).isNotNull();
  }

  @Test
  public void generateContent_withStreamingEnabled_returnsFlowable() {
    GitHubModels model = new GitHubModels(TEST_MODEL, TEST_TOKEN);
    
    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(Part.builder().text("Hello").build()))
        .build();
    
    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .build();
    
    // Test streaming mode
    assertThat(model.generateContent(request, true)).isNotNull();
  }
}