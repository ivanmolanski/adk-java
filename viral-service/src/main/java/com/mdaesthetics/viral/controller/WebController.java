package com.mdaesthetics.viral.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/virality";
    }

    @GetMapping("/virality")
    public String virality() {
        return "virality";
    }
    
    @GetMapping("/chat")
    public String chat() {
        return "redirect:/virality";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/virality";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("tiktokLoginUrl", "/oauth2/authorization/tiktok");
        model.addAttribute("instagramLoginUrl", "/oauth2/authorization/instagram");
        return "login";
    }

    @GetMapping("/posting")
    public String posting() {
        return "redirect:/virality";
    }
}