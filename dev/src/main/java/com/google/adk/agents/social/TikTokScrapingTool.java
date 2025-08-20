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
import com.google.genai.types.Type;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for scraping TikTok content related to aesthetics and beauty trends. Focuses on finding
 * viral videos that can inspire content for MD Aesthetics.
 */
public class TikTokScrapingTool extends BaseTool {

  private static final Logger logger = LoggerFactory.getLogger(TikTokScrapingTool.class);

  // TikTok hashtags and keywords related to aesthetics
  private static final List<String> AESTHETICS_KEYWORDS =
      ImmutableList.of(
          "skincare",
          "botox",
          "filler",
          "antiaging",
          "beautytrends",
          "glowup",
          "aesthetics",
          "beautytips",
          "medicalspa",
          "facials",
          "nonsurgical",
          "beautytreatment",
          "skincareroutine",
          "glowingskin");

  @Override
  public String name() {
    return "tiktok_scraper";
  }

  @Override
  public String description() {
    return "Scrapes TikTok for viral aesthetics and beauty content, analyzing trending videos and hashtags";
  }

  @Override
  public FunctionDeclaration getDeclaration() {
    return FunctionDeclaration.builder()
        .name(name())
        .description(description())
        .parameters(
            Schema.builder()
                .type(Type.OBJECT)
                .properties(
                    ImmutableMap.of(
                        "keyword",
                            Schema.builder()
                                .type(Type.STRING)
                                .description(
                                    "Specific keyword to search for (optional, defaults to aesthetics-related keywords)")
                                .build(),
                        "limit",
                            Schema.builder()
                                .type(Type.INTEGER)
                                .description("Maximum number of videos to analyze (default: 15)")
                                .build()))
                .build())
        .build();
  }

  @Override
  public Single<String> execute(ToolContext toolContext, Map<String, Object> parameters) {
    String keyword = (String) parameters.getOrDefault("keyword", "skincare");
    int limit = ((Number) parameters.getOrDefault("limit", 15)).intValue();

    logger.info("Scraping TikTok for keyword: {} with limit: {}", keyword, limit);

    return Single.fromCallable(
        () -> {
          try {
            List<TikTokVideo> videos = scrapeTikTokKeyword(keyword, limit);
            return formatResults(videos);
          } catch (Exception e) {
            logger.error("Error scraping TikTok", e);
            return "Error scraping TikTok: " + e.getMessage();
          }
        });
  }

  private List<TikTokVideo> scrapeTikTokKeyword(String keyword, int limit) throws Exception {
    // Note: This is a simplified implementation. In production, you would need to:
    // 1. Use TikTok API for Developers or TikTok Research API
    // 2. Handle authentication and rate limits
    // 3. Comply with TikTok's terms of service
    // 4. Use official API endpoints

    List<TikTokVideo> videos = new ArrayList<>();

    // For demo purposes, we'll simulate finding viral aesthetics content
    videos.addAll(getSimulatedViralVideos(keyword, limit));

    return videos;
  }

  private List<TikTokVideo> getSimulatedViralVideos(String keyword, int limit) {
    List<TikTokVideo> videos = new ArrayList<>();

    // Simulated viral TikTok content for demonstration
    videos.add(
        new TikTokVideo(
            "@aesthetics_queen",
            "5-minute morning skincare routine for glowing skin ✨ #skincare #glowingskin #beautytrends",
            "https://tiktok.com/@aesthetics_queen/video/1",
            2500000, // 2.5M views
            125000, // 125K likes
            8500, // 8.5K comments
            45000, // 45K shares
            ImmutableList.of("#skincare", "#glowingskin", "#beautytrends", "#morningroutine")));

    videos.add(
        new TikTokVideo(
            "@botox_before_after",
            "Botox transformation - 2 weeks results! #botox #transformation #aesthetics",
            "https://tiktok.com/@botox_before_after/video/2",
            1800000, // 1.8M views
            98000, // 98K likes
            12000, // 12K comments
            25000, // 25K shares
            ImmutableList.of("#botox", "#transformation", "#aesthetics", "#beforeandafter")));

    videos.add(
        new TikTokVideo(
            "@filler_facts",
            "Lip filler do's and don'ts from a medical professional 💋 #lipfiller #aesthetics #medicalspa",
            "https://tiktok.com/@filler_facts/video/3",
            3200000, // 3.2M views
            185000, // 185K likes
            15000, // 15K comments
            62000, // 62K shares
            ImmutableList.of("#lipfiller", "#aesthetics", "#medicalspa", "#beautytips")));

    videos.add(
        new TikTokVideo(
            "@glowup_journey",
            "Non-surgical facelift results after 6 months #nonsurgical #antiaging #glowup",
            "https://tiktok.com/@glowup_journey/video/4",
            1500000, // 1.5M views
            89000, // 89K likes
            6800, // 6.8K comments
            28000, // 28K shares
            ImmutableList.of("#nonsurgical", "#antiaging", "#glowup", "#skincare")));

    videos.add(
        new TikTokVideo(
            "@beauty_treatments_pro",
            "HydraFacial vs Regular Facial - which is better? #hydrafacial #facials #skincare",
            "https://tiktok.com/@beauty_treatments_pro/video/5",
            950000, // 950K views
            67000, // 67K likes
            4200, // 4.2K comments
            18000, // 18K shares
            ImmutableList.of("#hydrafacial", "#facials", "#skincare", "#beautytreatment")));

    return videos.subList(0, Math.min(limit, videos.size()));
  }

  private String formatResults(List<TikTokVideo> videos) {
    StringBuilder result = new StringBuilder();
    result.append("TikTok Viral Aesthetics Content Analysis:\n\n");

    for (int i = 0; i < videos.size(); i++) {
      TikTokVideo video = videos.get(i);
      result
          .append(String.format("%d. %s\n", i + 1, video.username))
          .append(String.format("   Caption: %s\n", video.caption))
          .append(
              String.format(
                  "   Views: %,d | Likes: %,d | Comments: %,d | Shares: %,d\n",
                  video.views, video.likes, video.comments, video.shares))
          .append(String.format("   Engagement Rate: %.2f%%\n", calculateEngagementRate(video)))
          .append(String.format("   Hashtags: %s\n", String.join(" ", video.hashtags)))
          .append(String.format("   URL: %s\n\n", video.url));
    }

    // Add trending hashtags analysis
    result.append("Trending Keywords in Aesthetics TikTok:\n");
    AESTHETICS_KEYWORDS.forEach(keyword -> result.append("#").append(keyword).append(" "));
    result.append("\n\n");

    // Add content strategy insights
    result.append("Content Strategy Insights for MD Aesthetics:\n");
    result.append("1. Before/After transformations perform exceptionally well\n");
    result.append("2. Educational content about procedures drives high engagement\n");
    result.append("3. Short, informative videos about skincare routines are trending\n");
    result.append("4. Professional advice from medical practitioners builds trust\n");
    result.append("5. Non-surgical options are increasingly popular topics\n");

    return result.toString();
  }

  private double calculateEngagementRate(TikTokVideo video) {
    double totalEngagement = video.likes + video.comments + video.shares;
    return (totalEngagement / video.views) * 100;
  }

  private static class TikTokVideo {
    final String username;
    final String caption;
    final String url;
    final int views;
    final int likes;
    final int comments;
    final int shares;
    final List<String> hashtags;

    TikTokVideo(
        String username,
        String caption,
        String url,
        int views,
        int likes,
        int comments,
        int shares,
        List<String> hashtags) {
      this.username = username;
      this.caption = caption;
      this.url = url;
      this.views = views;
      this.likes = likes;
      this.comments = comments;
      this.shares = shares;
      this.hashtags = hashtags;
    }
  }
}
