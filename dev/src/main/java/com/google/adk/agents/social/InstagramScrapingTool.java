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

package com.google.adk.agents.social;

import com.google.adk.tools.BaseTool;
import com.google.adk.tools.ToolContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for scraping Instagram content related to aesthetics and beauty trends. Focuses on finding
 * viral content that can be analyzed for MD Aesthetics business insights.
 */
public class InstagramScrapingTool extends BaseTool {

  private static final Logger logger = LoggerFactory.getLogger(InstagramScrapingTool.class);

  public InstagramScrapingTool() {
    super(
        "instagram_scraper",
        "Scrapes Instagram for viral aesthetics and beauty content, analyzing engagement metrics and trending hashtags");
  }

  // Instagram hashtags related to aesthetics
  private static final List<String> AESTHETICS_HASHTAGS =
      ImmutableList.of(
          "aesthetics",
          "botox",
          "filler",
          "skincare",
          "antiaging",
          "medicalaesthetics",
          "beautytreatments",
          "cosmeticsurgery",
          "skinglow",
          "beautytrends",
          "injectables",
          "nonsurgical");

  // Pattern for detecting engagement metrics
  private static final Pattern ENGAGEMENT_PATTERN =
      Pattern.compile("(\\d+[kmb]?)[\\s]*(likes|comments|views)", Pattern.CASE_INSENSITIVE);

  @Override
  public Optional<FunctionDeclaration> declaration() {
    return Optional.of(
        FunctionDeclaration.builder()
            .name(name())
            .description(description())
            .parameters(
                Schema.fromJson(
                    """
                {
                  "type": "object",
                  "properties": {
                    "hashtag": {
                      "type": "string",
                      "description": "Specific hashtag to search for (optional, defaults to aesthetics-related tags)"
                    },
                    "limit": {
                      "type": "integer",
                      "description": "Maximum number of posts to scrape (default: 20)"
                    }
                  }
                }
                """))
            .build());
  }

  @Override
  public Single<Map<String, Object>> runAsync(
      Map<String, Object> parameters, ToolContext toolContext) {
    String hashtag = (String) parameters.getOrDefault("hashtag", "aesthetics");
    int limit = ((Number) parameters.getOrDefault("limit", 20)).intValue();

    logger.info("Scraping Instagram for hashtag: {} with limit: {}", hashtag, limit);

    return Single.fromCallable(
        () -> {
          try {
            List<InstagramPost> posts = scrapeInstagramHashtag(hashtag, limit);
            String results = formatResults(posts);
            return ImmutableMap.of("result", results);
          } catch (Exception e) {
            logger.error("Error scraping Instagram", e);
            return ImmutableMap.of("error", "Error scraping Instagram: " + e.getMessage());
          }
        });
  }

  private List<InstagramPost> scrapeInstagramHashtag(String hashtag, int limit) throws Exception {
    // Note: This is a simplified implementation. In production, you would need to:
    // 1. Use Instagram Basic Display API or Instagram Graph API
    // 2. Handle authentication properly
    // 3. Respect rate limits
    // 4. Use proper web scraping techniques that comply with Instagram's terms

    List<InstagramPost> posts = new ArrayList<>();

    // For demo purposes, we'll simulate finding viral aesthetics content
    // In a real implementation, this would make actual API calls
    posts.addAll(getSimulatedViralContent(hashtag, limit));

    return posts;
  }

  private List<InstagramPost> getSimulatedViralContent(String hashtag, int limit) {
    List<InstagramPost> posts = new ArrayList<>();

    // Simulated viral content for demonstration
    posts.add(
        new InstagramPost(
            "aesthetic_clinic_downtown",
            "Before & After: Non-surgical face lift with dermal fillers ✨ #aesthetics #botox #filler",
            "https://example.com/post1",
            15000,
            342,
            ImmutableList.of(
                "#aesthetics", "#botox", "#filler", "#beforeandafter", "#nonsurgical")));

    posts.add(
        new InstagramPost(
            "beauty_trends_2025",
            "Glass skin trend 2025: The secret to perfect skin glow #skincare #glassskin #antiaging",
            "https://example.com/post2",
            28500,
            567,
            ImmutableList.of(
                "#skincare", "#glassskin", "#antiaging", "#beautytrends", "#skinglow")));

    posts.add(
        new InstagramPost(
            "medical_aesthetics_pro",
            "Lip filler transformation 💋 Natural-looking results #lipfiller #aesthetics #beautytreatments",
            "https://example.com/post3",
            12300,
            289,
            ImmutableList.of("#lipfiller", "#aesthetics", "#beautytreatments", "#transformation")));

    return posts.subList(0, Math.min(limit, posts.size()));
  }

  private String formatResults(List<InstagramPost> posts) {
    StringBuilder result = new StringBuilder();
    result.append("Instagram Viral Aesthetics Content Analysis:\n\n");

    for (int i = 0; i < posts.size(); i++) {
      InstagramPost post = posts.get(i);
      result
          .append(String.format("%d. @%s\n", i + 1, post.username))
          .append(String.format("   Caption: %s\n", post.caption))
          .append(
              String.format("   Engagement: %,d likes, %,d comments\n", post.likes, post.comments))
          .append(String.format("   Hashtags: %s\n", String.join(" ", post.hashtags)))
          .append(String.format("   URL: %s\n\n", post.url));
    }

    // Add trending hashtags analysis
    result.append("Trending Hashtags in Aesthetics:\n");
    AESTHETICS_HASHTAGS.forEach(tag -> result.append("#").append(tag).append(" "));

    return result.toString();
  }

  private static class InstagramPost {
    final String username;
    final String caption;
    final String url;
    final int likes;
    final int comments;
    final List<String> hashtags;

    InstagramPost(
        String username,
        String caption,
        String url,
        int likes,
        int comments,
        List<String> hashtags) {
      this.username = username;
      this.caption = caption;
      this.url = url;
      this.likes = likes;
      this.comments = comments;
      this.hashtags = hashtags;
    }
  }
}
