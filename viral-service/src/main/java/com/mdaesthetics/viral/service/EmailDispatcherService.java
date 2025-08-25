package com.mdaesthetics.viral.service;

import com.mdaesthetics.viral.model.ContentDraft;
import com.mdaesthetics.viral.model.TrendAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formats and dispatches HTML email digests. Actual Gmail API integration is TODO pending credentials.
 */
@Service
public class EmailDispatcherService {
    private static final Logger log = LoggerFactory.getLogger(EmailDispatcherService.class);

    @Value("${viral.email.recipients:}")
    private String recipients; // comma separated

    public String buildHtml(List<TrendAnalysis> analyses, List<ContentDraft> drafts) {
        String trends = analyses.stream().limit(5).map(this::trendRow).collect(Collectors.joining());
        String draftHtml = drafts.stream().limit(5).map(this::draftSection).collect(Collectors.joining("<hr style='margin:24px 0;border:none;border-top:1px solid #eee;'/>"));
        return "<html><body style='font-family:Arial,sans-serif;'>"+
            "<h2 style='color:#222;margin-bottom:4px;'>MDAesthetics Daily Viral Intelligence</h2>"+
            "<p style='margin-top:0;font-size:13px;color:#555;'>"+ DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now()) +"</p>"+
            "<h3 style='color:#444;'>Top Trend Analyses</h3>"+
            "<table width='100%' cellpadding='6' style='border-collapse:collapse;font-size:13px;'>"+
            "<thead><tr style='background:#f7f7f7;'><th align='left'>Category</th><th align='left'>Hook</th><th align='left'>Virality</th><th align='left'>Relevance</th></tr></thead>"+
            "<tbody>"+ trends +"</tbody></table>"+
            "<h3 style='color:#444;margin-top:32px;'>Generated Drafts</h3>"+ draftHtml +
            "<p style='font-size:12px;color:#777;margin-top:40px;'>Automated report • Internal use only</p>"+
            "</body></html>";
    }

    private String trendRow(TrendAnalysis t) {
        return String.format("<tr><td>%s</td><td>%s</td><td>%.2f</td><td>%.2f</td></tr>",
            escape(t.category()), escape(limit(t.hook(),80)), t.viralityScore()==null?0.0:t.viralityScore(), t.relevanceScore()==null?0.0:t.relevanceScore());
    }
    private String draftSection(ContentDraft d) {
        return "<div><h4 style='margin:4px 0;'>"+ escape(limit(d.hook(),90)) +"</h4>"+
            "<p style='white-space:pre-wrap;line-height:1.4;'>"+ escape(d.body()) +"</p>"+
            "<p style='color:#555;font-size:12px;margin:8px 0;'>CTA: "+ escape(d.callToAction()) +"</p>"+
            "<p style='color:#777;font-size:11px;'>Hashtags: "+ escape(String.join(" ", d.hashtags()==null?List.of():d.hashtags())) +"</p>"+
            (d.compliancePassed()?"<span style='color:green;font-size:11px;'>Compliance Passed</span>":"<span style='color:#c00;font-size:11px;'>Compliance Issues</span>") +
            "</div>";
    }

    public void sendDigest(String subject, String html, boolean simulate) {
        if (recipients==null || recipients.isBlank()) {
            log.warn("[email] No recipients configured; skipping send");
            return;
        }
        // TODO: Integrate Gmail API using service account credentials. For now, simulate/log.
        if (simulate) {
            log.info("[email] SIMULATION subject='{}' recipients='{}' size={} bytes", subject, recipients, html.length());
        } else {
            log.info("[email] (placeholder) Would send to {} subject='{}'", recipients, subject);
        }
    }

    private String escape(String s){ if(s==null) return ""; return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
    private String limit(String s,int max){ if(s==null) return ""; return s.length()>max? s.substring(0,max-1)+"…": s; }
}
