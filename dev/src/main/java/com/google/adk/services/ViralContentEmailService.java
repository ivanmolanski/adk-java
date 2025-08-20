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

package com.google.adk.services;

import com.google.adk.agents.social.InstagramScrapingTool;
import com.google.adk.agents.social.TikTokScrapingTool;
import com.google.adk.tools.ToolContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for sending daily viral content digest emails. Aggregates content from Instagram and
 * TikTok scraping tools.
 */
@Service
public class ViralContentEmailService {

  private static final Logger logger = LoggerFactory.getLogger(ViralContentEmailService.class);

  @Autowired private JavaMailSender mailSender;

  @Value("${app.email.recipient:info@mdaesthetics.ca}")
  private String recipientEmail;

  @Value("${app.email.from:noreply@mdaesthetics.ca}")
  private String fromEmail;

  private final InstagramScrapingTool instagramTool = new InstagramScrapingTool();
  private final TikTokScrapingTool tikTokTool = new TikTokScrapingTool();

  /** Scheduled method that runs daily at 8 AM to send viral content digest. */
  @Scheduled(cron = "0 0 8 * * *") // Every day at 8:00 AM
  public void sendDailyViralContentDigest() {
    logger.info("Starting daily viral content digest generation");

    try {
      CompletableFuture<String> instagramContentFuture = getInstagramContent();
      CompletableFuture<String> tikTokContentFuture = getTikTokContent();

      // Wait for both scraping operations to complete
      CompletableFuture.allOf(instagramContentFuture, tikTokContentFuture).join();

      String instagramContent = instagramContentFuture.get();
      String tikTokContent = tikTokContentFuture.get();

      String emailContent = generateEmailContent(instagramContent, tikTokContent);
      sendEmail(emailContent);

      logger.info("Daily viral content digest sent successfully");
    } catch (Exception e) {
      logger.error("Error sending daily viral content digest", e);
    }
  }

  private CompletableFuture<String> getInstagramContent() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("hashtag", "aesthetics");
            parameters.put("limit", 5);

            return instagramTool
                .execute(new ToolContext.Builder().build(), parameters)
                .blockingGet();
          } catch (Exception e) {
            logger.error("Error getting Instagram content", e);
            return "Error retrieving Instagram content: " + e.getMessage();
          }
        });
  }

  private CompletableFuture<String> getTikTokContent() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("keyword", "skincare");
            parameters.put("limit", 5);

            return tikTokTool.execute(new ToolContext.Builder().build(), parameters).blockingGet();
          } catch (Exception e) {
            logger.error("Error getting TikTok content", e);
            return "Error retrieving TikTok content: " + e.getMessage();
          }
        });
  }

  private String generateEmailContent(String instagramContent, String tikTokContent) {
    String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Daily Viral Aesthetics Content Digest</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background-color: #f8f9fa; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .section { margin-bottom: 30px; }
                    .section h2 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
                    .insights { background-color: #e8f5e8; padding: 15px; border-radius: 5px; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; }
                    pre { white-space: pre-wrap; background-color: #f4f4f4; padding: 15px; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🌟 Daily Viral Aesthetics Content Digest</h1>
                    <p><strong>MD Aesthetics - %s</strong></p>
                </div>

                <div class="content">
                    <div class="insights">
                        <h3>📈 Today's Key Insights for Revenue Growth</h3>
                        <ul>
                            <li><strong>Trending Topics:</strong> Focus on non-surgical treatments and before/after transformations</li>
                            <li><strong>Content Strategy:</strong> Educational content about procedures drives high engagement</li>
                            <li><strong>Hashtags:</strong> Use #aesthetics #botox #filler #skincare for maximum reach</li>
                            <li><strong>Best Practices:</strong> Short, informative videos perform better than long content</li>
                        </ul>
                    </div>

                    <div class="section">
                        <h2>📱 Instagram Viral Content</h2>
                        <pre>%s</pre>
                    </div>

                    <div class="section">
                        <h2>🎵 TikTok Trending Videos</h2>
                        <pre>%s</pre>
                    </div>

                    <div class="section">
                        <h2>💡 Action Items for MD Aesthetics</h2>
                        <ol>
                            <li><strong>Content Creation:</strong> Create similar content showcasing your treatments</li>
                            <li><strong>Hashtag Strategy:</strong> Use the trending hashtags identified above</li>
                            <li><strong>Engagement:</strong> Respond to comments and engage with trending posts</li>
                            <li><strong>Cross-Platform:</strong> Adapt successful content across Instagram and TikTok</li>
                            <li><strong>Schedule:</strong> Post during peak engagement hours (7-9 PM EST)</li>
                        </ol>
                    </div>
                </div>

                <div class="footer">
                    <p>This digest was automatically generated by MD Aesthetics AI System</p>
                    <p>Visit: <a href="https://mdaesthetics.ca">mdaesthetics.ca</a></p>
                </div>
            </body>
            </html>
            """,
        currentDate, instagramContent, tikTokContent);
  }

  private void sendEmail(String content) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setFrom(fromEmail);
    helper.setTo(recipientEmail);
    helper.setSubject(
        "🌟 Daily Viral Aesthetics Content Digest - "
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
    helper.setText(content, true); // HTML content

    mailSender.send(message);
    logger.info("Email sent successfully to: {}", recipientEmail);
  }

  /** Manual trigger for sending the digest (useful for testing) */
  public void sendManualDigest() {
    sendDailyViralContentDigest();
  }
}
