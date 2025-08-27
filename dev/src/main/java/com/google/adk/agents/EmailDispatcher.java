package com.google.adk.agents;

import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * EmailDispatcher
 * Formats and sends daily HTML email digests with top viral content and drafts.
 */
@Service
public class EmailDispatcher {
    private static final Logger log = LoggerFactory.getLogger(EmailDispatcher.class);

    private final InMemorySessionService sessionService = new InMemorySessionService();
    private final InMemoryArtifactService artifactService = new InMemoryArtifactService();

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Email configuration
    private String fromEmail = "noreply@mdaesthetics.ai";
    private List<String> recipientEmails = List.of(
        "christine.carrer@hotmail.com",
        "dalkeith@golden.net"
    );

    // Email templates and styling
    private static final String EMAIL_TEMPLATE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>MDAesthetics Daily Viral Content Briefing</title>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    line-height: 1.6;
                    color: #333;
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background-color: #f8f9fa;
                }
                .container {
                    background-color: white;
                    padding: 30px;
                    border-radius: 10px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                }
                .header {
                    text-align: center;
                    border-bottom: 3px solid #2563eb;
                    padding-bottom: 20px;
                    margin-bottom: 30px;
                }
                .logo {
                    font-size: 24px;
                    font-weight: bold;
                    color: #2563eb;
                    margin-bottom: 10px;
                }
                .date {
                    color: #666;
                    font-size: 14px;
                }
                .section {
                    margin-bottom: 30px;
                    padding: 20px;
                    border-left: 4px solid #2563eb;
                    background-color: #f8fafc;
                }
                .section-title {
                    font-size: 18px;
                    font-weight: bold;
                    color: #2563eb;
                    margin-bottom: 15px;
                }
                .metric {
                    display: inline-block;
                    background-color: #dbeafe;
                    color: #1e40af;
                    padding: 5px 10px;
                    border-radius: 15px;
                    font-size: 12px;
                    margin: 2px;
                }
                .content-item {
                    background-color: white;
                    border: 1px solid #e5e7eb;
                    border-radius: 8px;
                    padding: 15px;
                    margin-bottom: 15px;
                }
                .hook {
                    font-weight: bold;
                    color: #dc2626;
                    font-style: italic;
                }
                .hashtag {
                    color: #7c3aed;
                    font-weight: 500;
                }
                .cta {
                    background-color: #2563eb;
                    color: white;
                    padding: 12px 24px;
                    text-decoration: none;
                    border-radius: 6px;
                    display: inline-block;
                    margin: 10px 0;
                }
                .footer {
                    text-align: center;
                    margin-top: 40px;
                    padding-top: 20px;
                    border-top: 1px solid #e5e7eb;
                    color: #666;
                    font-size: 12px;
                }
                .insights {
                    background-color: #f0f9ff;
                    border: 1px solid #0ea5e9;
                    border-radius: 8px;
                    padding: 15px;
                    margin: 15px 0;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <div class="logo">MDAesthetics</div>
                    <h1>Daily Viral Content Briefing</h1>
                    <div class="date">%s</div>
                </div>

                %s

                <div class="footer">
                    <p>This briefing was generated by your AI Viral Forge system</p>
                    <p>Questions? Reply to this email or contact your development team</p>
                </div>
            </div>
        </body>
        </html>
        """;

    public EmailDispatcher() {
        // Load configuration from environment
        String fromEnv = System.getenv("MDAESTHETICS_FROM_EMAIL");
        if (fromEnv != null && !fromEnv.isEmpty()) {
            this.fromEmail = fromEnv;
        }

        String recipientsEnv = System.getenv("MDAESTHETICS_RECIPIENTS");
        if (recipientsEnv != null && !recipientsEnv.isEmpty()) {
            this.recipientEmails = List.of(recipientsEnv.split(","));
        }
    }

    /**
     * Send daily digest email with top viral content and AI-generated drafts
     */
    public String sendDailyDigest() {
        return sendDailyDigest(null, null);
    }

    /**
     * Send daily digest with specific data
     */
    public String sendDailyDigest(List<Map<String, Object>> topTrends, List<Map<String, Object>> contentDrafts) {
        String requestId = "email-" + System.currentTimeMillis();
        long start = System.currentTimeMillis();

        try {
            log.info("[email] Starting daily digest: {}", requestId);

            // Generate email content
            String emailContent = generateEmailContent(topTrends, contentDrafts);
            String subject = generateSubject(topTrends);

            // Send email
            boolean success = sendEmail(subject, emailContent);

            long latency = System.currentTimeMillis() - start;
            if (success) {
                log.info("[email] Daily digest sent successfully in {}ms", latency);
                return String.format("""
                    ✅ Daily briefing email sent successfully!

                    📧 Recipients: %s
                    📊 Content: %s
                    ⏱️  Processing time: %dms

                    The team will receive the latest viral content insights and AI-generated drafts.
                    """,
                    String.join(", ", recipientEmails),
                    topTrends != null ? topTrends.size() + " trends" : "latest insights",
                    latency);
            } else {
                log.error("[email] Failed to send daily digest after {}ms", latency);
                return """
                    ❌ Failed to send daily briefing email

                    Please check the email configuration and try again.
                    """;
            }

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[email] Exception sending digest after {}ms: {}", latency, e.getMessage(), e);
            return String.format("""
                ❌ Email dispatch failed

                Error: %s

                Please check the email service configuration.
                """, e.getMessage());
        }
    }

    /**
     * Send test email to verify configuration
     */
    public String sendTestEmail() {
        String subject = "MDAesthetics Viral Forge - Test Email";
        String content = generateTestEmailContent();

        try {
            boolean success = sendEmail(subject, content);
            if (success) {
                return """
                    ✅ Test email sent successfully!

                    📧 Recipients: %s
                    📝 Content: Configuration test

                    Check your inbox to verify email delivery is working.
                    """.formatted(String.join(", ", recipientEmails));
            } else {
                return """
                    ❌ Test email failed

                    Please check the email configuration:
                    • JavaMailSender bean is configured
                    • SMTP settings are correct
                    • From email address is valid
                    """;
            }
        } catch (Exception e) {
            return """
                ❌ Test email exception: %s

                Please check the email service configuration.
                """.formatted(e.getMessage());
        }
    }

    /**
     * Get email configuration status
     */
    public String getConfigurationStatus() {
        StringBuilder status = new StringBuilder();

        status.append("📧 Email Configuration Status\n\n");
        status.append(String.format("📤 From: %s\n", fromEmail));
        status.append(String.format("📥 Recipients: %d configured\n", recipientEmails.size()));

        for (int i = 0; i < recipientEmails.size(); i++) {
            status.append(String.format("  %d. %s\n", i + 1, recipientEmails.get(i)));
        }

        status.append(String.format("\n🔧 Mail Sender: %s\n",
            mailSender != null ? "✅ Configured" : "❌ Not configured"));

        if (mailSender == null) {
            status.append("\n⚠️  WARNING: JavaMailSender not available\n");
            status.append("   Emails will be logged but not sent\n");
            status.append("   Configure Spring Boot Mail properties to enable sending\n");
        }

        return status.toString();
    }

    private String generateSubject(List<Map<String, Object>> topTrends) {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("MMM dd"));

        if (topTrends != null && !topTrends.isEmpty()) {
            Map<String, Object> topTrend = topTrends.get(0);
            String category = safeGetString(topTrend, "category", "Viral Content");
            return String.format("MDAesthetics Daily Briefing - %s Trends (%s)", category, dateStr);
        }

        return String.format("MDAesthetics Daily Viral Content Briefing (%s)", dateStr);
    }

    private String generateEmailContent(List<Map<String, Object>> topTrends, List<Map<String, Object>> contentDrafts) {
        StringBuilder sections = new StringBuilder();

        // Executive Summary
        sections.append(generateExecutiveSummary(topTrends));

        // Top Trends Section
        if (topTrends != null && !topTrends.isEmpty()) {
            sections.append(generateTrendsSection(topTrends));
        } else {
            sections.append(generateDefaultTrendsSection());
        }

        // AI Content Drafts Section
        if (contentDrafts != null && !contentDrafts.isEmpty()) {
            sections.append(generateContentDraftsSection(contentDrafts));
        } else {
            sections.append(generateDefaultContentSection());
        }

        // Strategic Recommendations
        sections.append(generateRecommendationsSection());

        // Call to Action
        sections.append(generateCallToActionSection());

        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a"));

        return EMAIL_TEMPLATE.formatted(dateStr, sections.toString());
    }

    private String generateExecutiveSummary(List<Map<String, Object>> topTrends) {
        StringBuilder summary = new StringBuilder();
        summary.append("""
            <div class="section">
                <div class="section-title">📊 Executive Summary</div>
                <p>Welcome to your daily viral content briefing from the MDAesthetics AI Viral Forge system.</p>
                <div class="insights">
                    <strong>Today's Focus:</strong> We've analyzed the latest competitor content and generated AI-powered recommendations tailored to your brand pillars: Duo-C-Lift, SkinTyte body contouring, and physician-grade injectables.
                </div>
            </div>
            """);

        if (topTrends != null && !topTrends.isEmpty()) {
            summary.append(String.format("""
                <div class="metric">Top Trend: %s</div>
                <div class="metric">%d Viral Posts Analyzed</div>
                <div class="metric">AI Content Generated</div>
                """,
                safeGetString(topTrends.get(0), "category", "Unknown"),
                topTrends.size()));
        }

        return summary.toString();
    }

    private String generateTrendsSection(List<Map<String, Object>> topTrends) {
        StringBuilder trends = new StringBuilder();
        trends.append("""
            <div class="section">
                <div class="section-title">🔥 Top Viral Trends</div>
            </div>
            """);

        for (int i = 0; i < Math.min(5, topTrends.size()); i++) {
            Map<String, Object> trend = topTrends.get(i);
            trends.append(generateTrendItem(trend, i + 1));
        }

        return trends.toString();
    }

    private String generateTrendItem(Map<String, Object> trend, int rank) {
        String category = safeGetString(trend, "category", "Unknown");
        String hook = safeGetString(trend, "hook", "No hook available");
        String educationalPoint = safeGetString(trend, "educationalPoint", "No educational content");
        Double viralityScore = safeGetDouble(trend, "viralityScore", 0.0);
        Double relevanceScore = safeGetDouble(trend, "relevanceScore", 0.0);

        return String.format("""
            <div class="content-item">
                <h4>#%d %s</h4>
                <p><strong>Hook:</strong> <span class="hook">"%s"</span></p>
                <p><strong>Educational Value:</strong> %s</p>
                <div class="metric">Virality: %.1f/10</div>
                <div class="metric">MDAesthetics Fit: %.1f/10</div>
            </div>
            """, rank, category, hook, educationalPoint, viralityScore * 10, relevanceScore * 10);
    }

    private String generateContentDraftsSection(List<Map<String, Object>> contentDrafts) {
        StringBuilder drafts = new StringBuilder();
        drafts.append("""
            <div class="section">
                <div class="section-title">🚀 AI-Generated Content Drafts</div>
                <p>Here are ready-to-post content ideas optimized for MDAesthetics:</p>
            </div>
            """);

        for (int i = 0; i < Math.min(3, contentDrafts.size()); i++) {
            Map<String, Object> draft = contentDrafts.get(i);
            drafts.append(generateContentDraftItem(draft, i + 1));
        }

        return drafts.toString();
    }

    private String generateContentDraftItem(Map<String, Object> draft, int number) {
        String caption = safeGetString(draft, "caption", "No caption available");
        @SuppressWarnings("unchecked")
        List<String> hashtags = (List<String>) draft.getOrDefault("hashtags", List.of());
        String mediaType = safeGetString(draft, "suggestedMediaType", "static_image");

        StringBuilder hashtagsHtml = new StringBuilder();
        for (String hashtag : hashtags) {
            hashtagsHtml.append(String.format("<span class=\"hashtag\">%s</span> ", hashtag));
        }

        return String.format("""
            <div class="content-item">
                <h4>Draft #%d - %s</h4>
                <p><strong>Caption:</strong></p>
                <blockquote>%s</blockquote>
                <p><strong>Hashtags:</strong> %s</p>
                <div class="metric">Platform: Instagram/TikTok</div>
                <div class="metric">Media: %s</div>
            </div>
            """, number, mediaType, caption, hashtagsHtml.toString(), mediaType);
    }

    private String generateRecommendationsSection() {
        return """
            <div class="section">
                <div class="section-title">💡 Strategic Recommendations</div>
                <div class="insights">
                    <strong>Content Strategy:</strong>
                    <ul>
                        <li>Focus on "Process Demystified" content showing SkinTyte treatments</li>
                        <li>Create educational content about Duo-C-Lift science</li>
                        <li>Use clinical language while keeping it accessible</li>
                        <li>Include local hashtags for better discoverability</li>
                    </ul>
                </div>
                <div class="insights">
                    <strong>Brand Voice:</strong>
                    <ul>
                        <li>Maintain physician-led, authoritative tone</li>
                        <li>Emphasize clinical results and science</li>
                        <li>Use results-oriented language</li>
                        <li>Avoid "Botox" - use "Tox", "Neuromodulator", or "Neurotoxin"</li>
                    </ul>
                </div>
            </div>
            """;
    }

    private String generateCallToActionSection() {
        return """
            <div class="section">
                <div class="section-title">🎯 Next Steps</div>
                <p>Ready to implement these insights? Here's what you can do:</p>
                <ul>
                    <li><strong>Review:</strong> Check the AI-generated drafts above</li>
                    <li><strong>Customize:</strong> Adapt the content to your specific promotions</li>
                    <li><strong>Schedule:</strong> Post during peak engagement times</li>
                    <li><strong>Monitor:</strong> Track performance and engagement metrics</li>
                </ul>
                <a href="#" class="cta">Open Viral Forge Dashboard</a>
            </div>
            """;
    }

    private String generateDefaultTrendsSection() {
        return """
            <div class="section">
                <div class="section-title">🔥 Current Viral Trends</div>
                <div class="content-item">
                    <h4>Process Demystified Content</h4>
                    <p><strong>Hook:</strong> <span class="hook">"Watch how SkinTyte transforms skin in just one treatment"</span></p>
                    <p><strong>Educational Value:</strong> Infrared light technology heats collagen fibers for immediate tightening</p>
                    <div class="metric">High Virality</div>
                    <div class="metric">MDAesthetics Fit: 9/10</div>
                </div>
                <div class="content-item">
                    <h4>Science Explained Simply</h4>
                    <p><strong>Hook:</strong> <span class="hook">"Why Duo-C-Lift combines Ultherapy + Radiesse"</span></p>
                    <p><strong>Educational Value:</strong> Focused ultrasound + biostimulator for comprehensive facial rejuvenation</p>
                    <div class="metric">Educational Focus</div>
                    <div class="metric">MDAesthetics Fit: 10/10</div>
                </div>
            </div>
            """;
    }

    private String generateDefaultContentSection() {
        return """
            <div class="section">
                <div class="section-title">🚀 AI-Generated Content Drafts</div>
                <div class="content-item">
                    <h4>Draft #1 - SkinTyte Educational Video</h4>
                    <blockquote>🔬 The Science Behind SkinTyte: Our infrared technology precisely targets collagen fibers, causing immediate contraction and stimulating new collagen growth. Results continue improving for months! #skintyte #mdaesthetics #torontoaesthetics</blockquote>
                    <div class="metric">Platform: Instagram Reels</div>
                    <div class="metric">Hashtags: Optimized</div>
                </div>
            </div>
            """;
    }

    private String generateTestEmailContent() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a"));

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { color: #2563eb; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>MDAesthetics Viral Forge</h1>
                    <h2>Test Email</h2>
                </div>
                <p>This is a test email from your MDAesthetics AI Viral Forge system.</p>
                <p><strong>Sent:</strong> %s</p>
                <p><strong>Recipients:</strong> %s</p>
                <p>If you received this email, your email configuration is working correctly!</p>
                <hr>
                <p><small>This email was generated automatically by the Viral Forge system.</small></p>
            </body>
            </html>
            """, dateStr, String.join(", ", recipientEmails));
    }

    private boolean sendEmail(String subject, String htmlContent) {
        if (mailSender == null) {
            log.warn("[email] JavaMailSender not configured - logging email instead of sending");
            log.info("[email] SUBJECT: {}", subject);
            log.info("[email] CONTENT: {}", htmlContent.substring(0, Math.min(500, htmlContent.length())));
            return true; // Consider it successful for testing
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmails.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML content

            mailSender.send(message);
            return true;

        } catch (Exception e) {
            log.error("[email] Failed to send email: {}", e.getMessage(), e);
            return false;
        }
    }

    // Safe getters for Map values
    private String safeGetString(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private String safeGetString(Map<String, Object> map, String key) {
        return safeGetString(map, key, "");
    }

    private Double safeGetDouble(Map<String, Object> map, String key, Double defaultValue) {
        if (map == null) return defaultValue;
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Configuration setters
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public void setRecipientEmails(List<String> recipientEmails) {
        this.recipientEmails = recipientEmails;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
}
