package com.google.adk.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.adk.model.AgentJob;

@RestController
@RequestMapping("/api/agent-job")
public class AgentJobController {
    @PostMapping("/trigger")
    public String triggerJob(@RequestBody AgentJob job) {
        // TODO: Trigger agent job (e.g., start scraping, analysis, etc.)
        return "Job triggered: " + job.getType();
    }
}
