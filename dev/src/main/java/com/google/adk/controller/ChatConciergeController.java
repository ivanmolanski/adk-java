package com.google.adk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.adk.model.TrendAnalysisResult;
import com.google.adk.model.ContentDraft;
import java.util.List;

@RestController
@RequestMapping("/api/concierge")
public class ChatConciergeController {
    @GetMapping("/trends")
    public List<TrendAnalysisResult> getTrends() {
        // TODO: Fetch and return latest trends from Firestore
        return List.of();
    }

    @GetMapping("/content-draft")
    public List<ContentDraft> getContentDrafts() {
        // TODO: Fetch and return latest content drafts from Firestore
        return List.of();
    }
}
