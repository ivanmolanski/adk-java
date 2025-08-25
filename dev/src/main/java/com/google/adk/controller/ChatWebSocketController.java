package com.google.adk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.google.adk.agents.AIManager;

@Controller
public class ChatWebSocketController {
    @Autowired
    private AIManager aiManager;

    @MessageMapping("/chat")
    @SendTo("/topic/viral-output")
    public String handleChatMessage(String message) {
        // Delegate to AIManager, which will orchestrate agent calls and return viral output
        return aiManager.handleChat(message);
    }
}
