package com.mdaesthetics.viral.controller;

import com.mdaesthetics.viral.model.SocialMediaPost;
import com.mdaesthetics.viral.service.InstagramApiService;
import com.mdaesthetics.viral.service.TikTokApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = "*")
public class SocialMediaController {

    @Autowired
    private TikTokApiService tiktokService;

    @Autowired
    private InstagramApiService instagramService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSocialMediaStatus(
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> tiktokStatus = new HashMap<>();
        Map<String, Object> instagramStatus = new HashMap<>();
        
        try {
            // Try to get OAuth2 clients from session/security context
            // This is a simplified approach - in production you'd check actual OAuth2 tokens
            tiktokStatus.put("connected", false);
            tiktokStatus.put("profile", null);
            
            instagramStatus.put("connected", false);
            instagramStatus.put("profile", null);
            
        } catch (Exception e) {
            // Default to not connected
            tiktokStatus.put("connected", false);
            instagramStatus.put("connected", false);
        }
        
        response.put("tiktok", tiktokStatus);
        response.put("instagram", instagramStatus);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/disconnect/{platform}")
    public ResponseEntity<Map<String, String>> disconnect(@PathVariable String platform) {
        // TODO: Implement actual OAuth2 token revocation
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Disconnected from " + platform);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/post/{platform}")
    public ResponseEntity<Map<String, Object>> publishPost(
            HttpServletRequest request,
            @PathVariable String platform,
            @RequestParam String content,
            @RequestParam(required = false) String hashtags,
            @RequestParam(required = false) MultipartFile[] media) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // For now, simulate posting functionality
            // TODO: Implement actual OAuth2 client retrieval and API posting
            
            response.put("success", true);
            response.put("platform", platform);
            response.put("message", "Post published successfully to " + platform);
            response.put("result", Map.of(
                "postId", "mock_" + platform + "_" + System.currentTimeMillis(),
                "content", content,
                "hashtags", hashtags != null ? hashtags : "",
                "mediaCount", media != null ? media.length : 0
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to post to " + platform + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/auth/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(
            @RegisteredOAuth2AuthorizedClient("tiktok") OAuth2AuthorizedClient tiktokClient,
            @RegisteredOAuth2AuthorizedClient("instagram") OAuth2AuthorizedClient instagramClient) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> authStatus = new HashMap<>();
        
        authStatus.put("tiktok", tiktokClient != null);
        authStatus.put("instagram", instagramClient != null);
        
        if (tiktokClient != null) {
            Map<String, Object> tiktokProfile = tiktokService.getUserProfile(
                tiktokClient.getAccessToken().getTokenValue());
            authStatus.put("tiktokProfile", tiktokProfile);
        }
        
        if (instagramClient != null) {
            Map<String, Object> instagramProfile = instagramService.getUserProfile(
                instagramClient.getAccessToken().getTokenValue());
            authStatus.put("instagramProfile", instagramProfile);
        }
        
        response.put("success", true);
        response.put("authStatus", authStatus);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tiktok/post/video")
    public ResponseEntity<Map<String, Object>> postToTikTokVideo(
            @RegisteredOAuth2AuthorizedClient("tiktok") OAuth2AuthorizedClient tiktokClient,
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam("content") String content,
            @RequestParam(value = "hashtags", required = false) String hashtagsStr,
            @RequestParam(value = "privacy", defaultValue = "public") String privacy) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (tiktokClient == null) {
            response.put("success", false);
            response.put("error", "TikTok authentication required");
            response.put("loginUrl", "/oauth2/authorization/tiktok");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            SocialMediaPost post = new SocialMediaPost("tiktok", content, 
                hashtagsStr != null ? java.util.Arrays.asList(hashtagsStr.split(",")) : null);
            post.setPrivacy(privacy);

            Map<String, Object> result = tiktokService.uploadVideo(
                tiktokClient.getAccessToken().getTokenValue(), videoFile, post);

            response.put("success", result.get("success"));
            response.put("platform", "tiktok");
            response.put("result", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to post to TikTok: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/instagram/post/image")
    public ResponseEntity<Map<String, Object>> postToInstagramImage(
            @RegisteredOAuth2AuthorizedClient("instagram") OAuth2AuthorizedClient instagramClient,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("content") String content,
            @RequestParam(value = "hashtags", required = false) String hashtagsStr) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (instagramClient == null) {
            response.put("success", false);
            response.put("error", "Instagram authentication required");
            response.put("loginUrl", "/oauth2/authorization/instagram");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            SocialMediaPost post = new SocialMediaPost("instagram", content, 
                hashtagsStr != null ? java.util.Arrays.asList(hashtagsStr.split(",")) : null);

            Map<String, Object> result = instagramService.createImagePost(
                instagramClient.getAccessToken().getTokenValue(), imageFile, post);

            response.put("success", result.get("success"));
            response.put("platform", "instagram");
            response.put("result", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to post to Instagram: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/instagram/post/text")
    public ResponseEntity<Map<String, Object>> postToInstagramText(
            @RegisteredOAuth2AuthorizedClient("instagram") OAuth2AuthorizedClient instagramClient,
            @RequestParam("content") String content,
            @RequestParam(value = "hashtags", required = false) String hashtagsStr) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (instagramClient == null) {
            response.put("success", false);
            response.put("error", "Instagram authentication required");
            response.put("loginUrl", "/oauth2/authorization/instagram");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            SocialMediaPost post = new SocialMediaPost("instagram", content, 
                hashtagsStr != null ? java.util.Arrays.asList(hashtagsStr.split(",")) : null);

            Map<String, Object> result = instagramService.createTextPost(
                instagramClient.getAccessToken().getTokenValue(), post);

            response.put("success", result.get("success"));
            response.put("platform", "instagram");
            response.put("result", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to post to Instagram: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/post/generated")
    public ResponseEntity<Map<String, Object>> postGeneratedContent(
            @RegisteredOAuth2AuthorizedClient("tiktok") OAuth2AuthorizedClient tiktokClient,
            @RegisteredOAuth2AuthorizedClient("instagram") OAuth2AuthorizedClient instagramClient,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String platform = (String) requestBody.get("platform");
            String content = (String) requestBody.get("content");
            @SuppressWarnings("unchecked")
            java.util.List<String> hashtags = (java.util.List<String>) requestBody.get("hashtags");
            
            SocialMediaPost post = new SocialMediaPost(platform, content, hashtags);
            
            Map<String, Object> result = new HashMap<>();
            
            if ("tiktok".equals(platform)) {
                if (tiktokClient == null) {
                    response.put("success", false);
                    response.put("error", "TikTok authentication required");
                    response.put("loginUrl", "/oauth2/authorization/tiktok");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
                
                result = tiktokService.createTextPost(
                    tiktokClient.getAccessToken().getTokenValue(), post);
                    
            } else if ("instagram".equals(platform)) {
                if (instagramClient == null) {
                    response.put("success", false);
                    response.put("error", "Instagram authentication required");
                    response.put("loginUrl", "/oauth2/authorization/instagram");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
                
                result = instagramService.createTextPost(
                    instagramClient.getAccessToken().getTokenValue(), post);
                    
            } else {
                response.put("success", false);
                response.put("error", "Unsupported platform: " + platform);
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", result.get("success"));
            response.put("platform", platform);
            response.put("result", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to post generated content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/analytics/{platform}/{postId}")
    public ResponseEntity<Map<String, Object>> getPostAnalytics(
            @RegisteredOAuth2AuthorizedClient("tiktok") OAuth2AuthorizedClient tiktokClient,
            @RegisteredOAuth2AuthorizedClient("instagram") OAuth2AuthorizedClient instagramClient,
            @PathVariable String platform,
            @PathVariable String postId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> analytics;
            
            if ("tiktok".equals(platform)) {
                if (tiktokClient == null) {
                    response.put("success", false);
                    response.put("error", "TikTok authentication required");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
                
                analytics = tiktokService.getPostAnalytics(
                    tiktokClient.getAccessToken().getTokenValue(), postId);
                    
            } else if ("instagram".equals(platform)) {
                if (instagramClient == null) {
                    response.put("success", false);
                    response.put("error", "Instagram authentication required");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
                
                analytics = instagramService.getPostAnalytics(
                    instagramClient.getAccessToken().getTokenValue(), postId);
                    
            } else {
                response.put("success", false);
                response.put("error", "Unsupported platform: " + platform);
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", analytics.get("success"));
            response.put("platform", platform);
            response.put("postId", postId);
            response.put("analytics", analytics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to fetch analytics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/platforms")
    public ResponseEntity<Map<String, Object>> getSupportedPlatforms() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> platforms = new HashMap<>();
        
        Map<String, Object> tiktok = new HashMap<>();
        tiktok.put("name", "TikTok");
        tiktok.put("loginUrl", "/oauth2/authorization/tiktok");
        tiktok.put("supportedMedia", new String[]{"video"});
        tiktok.put("maxDuration", "180"); // 3 minutes
        
        Map<String, Object> instagram = new HashMap<>();
        instagram.put("name", "Instagram");
        instagram.put("loginUrl", "/oauth2/authorization/instagram");
        instagram.put("supportedMedia", new String[]{"image", "video", "carousel"});
        instagram.put("maxDuration", "60"); // 1 minute for videos
        
        platforms.put("tiktok", tiktok);
        platforms.put("instagram", instagram);
        
        response.put("success", true);
        response.put("platforms", platforms);
        
        return ResponseEntity.ok(response);
    }
}