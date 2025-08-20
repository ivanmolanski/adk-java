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

package com.google.adk.web.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.social.InstagramScrapingTool;
import com.google.adk.agents.social.TikTokScrapingTool;
import com.google.adk.services.ViralContentEmailService;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

  @Autowired private Firestore firestore;

  @Autowired private ViralContentEmailService emailService;

  private final LlmAgent aestheticsAgent;

  public ChatController() {
    // Initialize the aesthetics-focused agent with Gemini 2.5 Flash
    this.aestheticsAgent =
        LlmAgent.builder()
            .name("md_aesthetics_assistant")
            .description(
                "AI assistant specialized in medical aesthetics, social media content strategy, and business growth for MD Aesthetics")
            .model("gemini-2.0-flash")
            .instruction(
                """
                You are an AI assistant for MD Aesthetics (mdaesthetics.ca), specializing in:
                1. Medical aesthetics procedures and treatments
                2. Social media content strategy and viral trends analysis
                3. Business growth and revenue optimization
                4. Instagram and TikTok content recommendations
                5. Patient education and treatment explanations

                Your goal is to help increase revenue and engagement by providing insights on:
                - Trending aesthetics content on social media
                - Treatment recommendations based on viral trends
                - Content creation strategies
                - Patient engagement tactics

                Always maintain a professional, helpful, and knowledgeable tone while focusing on
                business growth opportunities for MD Aesthetics.
                """)
            .tools(new InstagramScrapingTool(), new TikTokScrapingTool())
            .build();
  }

  @PostMapping("/message")
  public CompletableFuture<ResponseEntity<ChatResponse>> sendMessage(
      @RequestBody ChatRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (String) auth.getPrincipal();

            // Store user message in Firestore
            storeMessage(userId, request.sessionId, request.message, "user");

            // Process message with the aesthetics agent
            String response = processMessageWithAgent(request.message);

            // Store agent response in Firestore
            storeMessage(userId, request.sessionId, response, "assistant");

            return ResponseEntity.ok(new ChatResponse(response, request.sessionId));

          } catch (Exception e) {
            logger.error("Error processing chat message", e);
            return ResponseEntity.internalServerError()
                .body(
                    new ChatResponse(
                        "Sorry, I encountered an error processing your message.",
                        request.sessionId));
          }
        });
  }

  @PostMapping("/scrape-instagram")
  public CompletableFuture<ResponseEntity<ScrapeResponse>> scrapeInstagram(
      @RequestBody ScrapeRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            InstagramScrapingTool tool = new InstagramScrapingTool();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("hashtag", request.hashtag != null ? request.hashtag : "aesthetics");
            parameters.put("limit", request.limit != null ? request.limit : 10);

            String result =
                tool.runAsync(parameters, null)
                    .map(response -> (String) response.getOrDefault("result", "No results"))
                    .blockingGet();

            return ResponseEntity.ok(new ScrapeResponse(result, "Instagram scraping completed"));

          } catch (Exception e) {
            logger.error("Error scraping Instagram", e);
            return ResponseEntity.internalServerError()
                .body(new ScrapeResponse("Error scraping Instagram: " + e.getMessage(), "error"));
          }
        });
  }

  @PostMapping("/scrape-tiktok")
  public CompletableFuture<ResponseEntity<ScrapeResponse>> scrapeTikTok(
      @RequestBody ScrapeRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            TikTokScrapingTool tool = new TikTokScrapingTool();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("keyword", request.hashtag != null ? request.hashtag : "skincare");
            parameters.put("limit", request.limit != null ? request.limit : 10);

            String result =
                tool.runAsync(parameters, null)
                    .map(response -> (String) response.getOrDefault("result", "No results"))
                    .blockingGet();

            return ResponseEntity.ok(new ScrapeResponse(result, "TikTok scraping completed"));

          } catch (Exception e) {
            logger.error("Error scraping TikTok", e);
            return ResponseEntity.internalServerError()
                .body(new ScrapeResponse("Error scraping TikTok: " + e.getMessage(), "error"));
          }
        });
  }

  @PostMapping("/send-digest")
  public ResponseEntity<Map<String, String>> sendViralDigest() {
    try {
      emailService.sendManualDigest();
      Map<String, String> response = new HashMap<>();
      response.put("status", "success");
      response.put("message", "Viral content digest sent successfully");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Error sending viral digest", e);
      Map<String, String> response = new HashMap<>();
      response.put("status", "error");
      response.put("message", "Error sending digest: " + e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  @GetMapping("/sessions/{sessionId}/messages")
  public CompletableFuture<ResponseEntity<Object>> getSessionMessages(
      @PathVariable String sessionId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (String) auth.getPrincipal();

            // Retrieve messages from Firestore
            CollectionReference messagesRef =
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("sessions")
                    .document(sessionId)
                    .collection("messages");

            return ResponseEntity.ok(messagesRef.orderBy("timestamp").get());

          } catch (Exception e) {
            logger.error("Error retrieving session messages", e);
            return ResponseEntity.internalServerError().body("Error retrieving messages");
          }
        });
  }

  private String processMessageWithAgent(String message) {
    try {
      // This is a simplified implementation
      // In a real scenario, you would use the full ADK agent processing pipeline

      if (message.toLowerCase().contains("instagram") || message.toLowerCase().contains("insta")) {
        return "I can help you analyze Instagram content for aesthetics trends. Would you like me to scrape current viral aesthetics posts?";
      } else if (message.toLowerCase().contains("tiktok")
          || message.toLowerCase().contains("viral")) {
        return "I can analyze TikTok trends for you. Let me look at the latest viral aesthetics content that could inspire your MD Aesthetics social media strategy.";
      } else if (message.toLowerCase().contains("botox")
          || message.toLowerCase().contains("filler")) {
        return "Botox and filler treatments are very popular on social media right now. I can analyze trending content and suggest content strategies that could drive more bookings for MD Aesthetics.";
      } else {
        return "I'm your MD Aesthetics AI assistant. I can help with viral content analysis, treatment trend insights, and social media strategies to grow your business. What would you like to explore?";
      }
    } catch (Exception e) {
      logger.error("Error processing message with agent", e);
      return "I apologize, but I encountered an error processing your request. Please try again.";
    }
  }

  private void storeMessage(String userId, String sessionId, String message, String role) {
    try {
      Map<String, Object> messageData = new HashMap<>();
      messageData.put("message", message);
      messageData.put("role", role);
      messageData.put("timestamp", Instant.now().toEpochMilli());

      DocumentReference messageRef =
          firestore
              .collection("users")
              .document(userId)
              .collection("sessions")
              .document(sessionId)
              .collection("messages")
              .document(UUID.randomUUID().toString());

      messageRef.set(messageData);

    } catch (Exception e) {
      logger.error("Error storing message in Firestore", e);
    }
  }

  // Request/Response DTOs
  public static class ChatRequest {
    @JsonProperty("message")
    public String message;

    @JsonProperty("sessionId")
    public String sessionId;
  }

  public static class ChatResponse {
    @JsonProperty("response")
    public String response;

    @JsonProperty("sessionId")
    public String sessionId;

    public ChatResponse(String response, String sessionId) {
      this.response = response;
      this.sessionId = sessionId;
    }
  }

  public static class ScrapeRequest {
    @JsonProperty("hashtag")
    public String hashtag;

    @JsonProperty("limit")
    public Integer limit;
  }

  public static class ScrapeResponse {
    @JsonProperty("data")
    public String data;

    @JsonProperty("status")
    public String status;

    public ScrapeResponse(String data, String status) {
      this.data = data;
      this.status = status;
    }
  }
}
