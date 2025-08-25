package com.google.adk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.adk.model.BriefingDigest;

@RestController
@RequestMapping("/api/briefing")
public class BriefingController {
    @GetMapping("/daily")
    public BriefingDigest getDailyBriefing() {
        // TODO: Fetch and return daily briefing digest
        return new BriefingDigest();
    }
}
