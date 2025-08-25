package com.google.adk.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.adk.model.EmailRequest;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    @PostMapping("/send")
    public String sendEmail(@RequestBody EmailRequest request) {
        // TODO: Send email using GmailService
        return "Email sent to: " + request.getTo();
    }
}
