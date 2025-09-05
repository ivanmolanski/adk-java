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

package com.google.adk.samples;

import com.google.adk.models.GitHubModels;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Sample application demonstrating GitHub Models usage with ADK Java.
 * 
 * <p>This example shows how to use GitHub's free inference API to power AI agents
 * without requiring paid API keys.
 * 
 * <p>To run this sample:
 * 1. Set GITHUB_TOKEN environment variable with your GitHub PAT or use in GitHub Actions
 * 2. Run: mvn exec:java -Dexec.mainClass="com.google.adk.samples.GitHubModelsSample"
 */
public class GitHubModelsSample {

  public static void main(String[] args) {
    // Check for GitHub token
    String githubToken = System.getenv("GITHUB_TOKEN");
    if (githubToken == null || githubToken.isEmpty()) {
      System.err.println("Error: GITHUB_TOKEN environment variable is required.");
      System.err.println("Please set it to your GitHub Personal Access Token with 'models:read' permission.");
      System.err.println("Example: export GITHUB_TOKEN=your_github_pat_here");
      System.exit(1);
    }

    System.out.println("GitHub Models ADK Java Sample");
    System.out.println("==============================");
    
    try {
      // Test different models available on GitHub Models
      testModel("openai/gpt-4o", githubToken);
      testModel("openai/gpt-4o-mini", githubToken);
      testConversation(githubToken);
      testWithSystemInstruction(githubToken);
      
    } catch (Exception e) {
      System.err.println("Error running sample: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Test a specific model with a simple prompt.
   */
  private static void testModel(String modelName, String githubToken) {
    System.out.println("\n--- Testing " + modelName + " ---");
    
    GitHubModels model = GitHubModels.builder()
        .modelName(modelName)
        .token(githubToken)
        .build();

    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(
            Part.builder()
                .text("Explain what GitHub Models is in one sentence.")
                .build()))
        .build();

    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .build();

    try {
      Flowable<LlmResponse> response = model.generateContent(request, false);
      
      response.blockingSubscribe(
          llmResponse -> {
            llmResponse.content().ifPresent(content -> {
              content.parts().ifPresent(parts -> {
                parts.forEach(part -> {
                  part.text().ifPresent(text -> {
                    System.out.println("Response: " + text);
                  });
                });
              });
            });
          },
          error -> {
            System.err.println("Error with " + modelName + ": " + error.getMessage());
          }
      );
    } catch (Exception e) {
      System.err.println("Failed to test " + modelName + ": " + e.getMessage());
    }
  }

  /**
   * Test a multi-turn conversation.
   */
  private static void testConversation(String githubToken) {
    System.out.println("\n--- Testing Conversation ---");
    
    GitHubModels model = GitHubModels.builder()
        .modelName("openai/gpt-4o")
        .token(githubToken)
        .build();

    // First message
    Content userMessage1 = Content.builder()
        .role("user")
        .parts(ImmutableList.of(
            Part.builder()
                .text("What's the capital of France?")
                .build()))
        .build();

    // Second message (simulating a follow-up)
    Content userMessage2 = Content.builder()
        .role("user")
        .parts(ImmutableList.of(
            Part.builder()
                .text("What's its population?")
                .build()))
        .build();

    // Simulate assistant response to first question
    Content assistantResponse = Content.builder()
        .role("assistant")
        .parts(ImmutableList.of(
            Part.builder()
                .text("The capital of France is Paris.")
                .build()))
        .build();

    LlmRequest conversationRequest = LlmRequest.builder()
        .contents(ImmutableList.of(userMessage1, assistantResponse, userMessage2))
        .build();

    try {
      Flowable<LlmResponse> response = model.generateContent(conversationRequest, false);
      
      response.blockingSubscribe(
          llmResponse -> {
            llmResponse.content().ifPresent(content -> {
              content.parts().ifPresent(parts -> {
                parts.forEach(part -> {
                  part.text().ifPresent(text -> {
                    System.out.println("Conversation Response: " + text);
                  });
                });
              });
            });
          },
          error -> {
            System.err.println("Conversation error: " + error.getMessage());
          }
      );
    } catch (Exception e) {
      System.err.println("Failed conversation test: " + e.getMessage());
    }
  }

  /**
   * Test with system instruction and generation parameters.
   */
  private static void testWithSystemInstruction(String githubToken) {
    System.out.println("\n--- Testing with System Instruction ---");
    
    GitHubModels model = GitHubModels.builder()
        .modelName("openai/gpt-4o")
        .token(githubToken)
        .build();

    Content userContent = Content.builder()
        .role("user")
        .parts(ImmutableList.of(
            Part.builder()
                .text("Explain recursion")
                .build()))
        .build();

    Content systemInstruction = Content.builder()
        .role("system")
        .parts(ImmutableList.of(
            Part.builder()
                .text("You are a patient computer science tutor. Explain concepts clearly with simple examples.")
                .build()))
        .build();

    GenerateContentConfig config = GenerateContentConfig.builder()
        .systemInstruction(systemInstruction)
        .temperature(0.3f)
        .maxOutputTokens(150)
        .build();

    LlmRequest request = LlmRequest.builder()
        .contents(ImmutableList.of(userContent))
        .config(config)
        .build();

    try {
      Flowable<LlmResponse> response = model.generateContent(request, false);
      
      response.blockingSubscribe(
          llmResponse -> {
            llmResponse.content().ifPresent(content -> {
              content.parts().ifPresent(parts -> {
                parts.forEach(part -> {
                  part.text().ifPresent(text -> {
                    System.out.println("Tutor Response: " + text);
                  });
                });
              });
            });
          },
          error -> {
            System.err.println("System instruction error: " + error.getMessage());
          }
      );
    } catch (Exception e) {
      System.err.println("Failed system instruction test: " + e.getMessage());
    }
  }
}