package com.google.adk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.google.adk.model.ChatMessage;
import com.google.adk.model.ViralOutput;
import com.google.adk.agents.AIManager;

@Controller
public class ViralOutputWebSocketController {
    @Autowired
    private AIManager aiManager;

    @MessageMapping("/viral")
    @SendTo("/topic/viral-output")
    public ViralOutput handleViralRequest(ChatMessage message) {
        // TODO: Use AIManager to orchestrate agent calls and return real-time viral output
        ViralOutput output = new ViralOutput();
        output.setContent(aiManager.handleChat(message.getContent()));
        output.setAgent("AIManager");
        output.setTimestamp(java.time.Instant.now().toString());
        return output;
    }
}
