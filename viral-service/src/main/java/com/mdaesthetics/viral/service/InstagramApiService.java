package com.mdaesthetics.viral.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdaesthetics.viral.model.SocialMediaPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class InstagramApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${social.instagram.graph-api-url:https://graph.instagram.com}")
    private String graphApiUrl;

    @Value("${social.instagram.api-version:v18.0}")
    private String apiVersion;

    public InstagramApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> getUserProfile(String accessToken) {
        try {
            String url = String.format("%s/%s/me?fields=id,username,account_type,media_count&access_token=%s",
                    graphApiUrl, apiVersion, accessToken);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", jsonNode.get("id").asText());
            result.put("username", jsonNode.get("username").asText());
            result.put("accountType", jsonNode.get("account_type").asText());
            result.put("mediaCount", jsonNode.get("media_count").asInt());
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "Failed to fetch user profile: " + e.getMessage());
            return errorResult;
        }
    }

    public Map<String, Object> createImagePost(String accessToken, MultipartFile imageFile, SocialMediaPost post) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Get user ID
            Map<String, Object> profileResult = getUserProfile(accessToken);
            if (!(Boolean) profileResult.get("success")) {
                return profileResult;
            }
            String userId = (String) profileResult.get("id");

            // Step 2: Upload image and create media container
            String createUrl = String.format("%s/%s/%s/media", graphApiUrl, apiVersion, userId);
            
            MultiValueMap<String, Object> createParams = new LinkedMultiValueMap<>();
            createParams.add("image_url", ""); // Would need to upload image first to get URL
            createParams.add("caption", post.getFormattedContent());
            createParams.add("access_token", accessToken);

            // For now, return guidance about image URL requirement
            result.put("success", false);
            result.put("error", "Instagram requires images to be uploaded via URL. Please upload image to a public URL first.");
            result.put("suggestion", "Use a service like Firebase Storage or AWS S3 to host the image, then provide the URL.");

            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to create Instagram post: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> createTextPost(String accessToken, SocialMediaPost post) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get user ID
            Map<String, Object> profileResult = getUserProfile(accessToken);
            if (!(Boolean) profileResult.get("success")) {
                return profileResult;
            }
            String userId = (String) profileResult.get("id");

            // Create media container for carousel/text post
            String createUrl = String.format("%s/%s/%s/media", graphApiUrl, apiVersion, userId);
            
            MultiValueMap<String, String> createParams = new LinkedMultiValueMap<>();
            createParams.add("media_type", "CAROUSEL");
            createParams.add("caption", post.getFormattedContent());
            createParams.add("access_token", accessToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> createEntity = new HttpEntity<>(createParams, headers);
            
            ResponseEntity<String> createResponse = restTemplate.postForEntity(createUrl, createEntity, String.class);
            JsonNode createJson = objectMapper.readTree(createResponse.getBody());
            
            if (!createJson.has("id")) {
                result.put("success", false);
                result.put("error", "Failed to create media container");
                return result;
            }

            String creationId = createJson.get("id").asText();

            // Publish the media
            String publishUrl = String.format("%s/%s/%s/media_publish", graphApiUrl, apiVersion, userId);
            
            MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
            publishParams.add("creation_id", creationId);
            publishParams.add("access_token", accessToken);

            HttpEntity<MultiValueMap<String, String>> publishEntity = new HttpEntity<>(publishParams, headers);
            
            ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishEntity, String.class);
            JsonNode publishJson = objectMapper.readTree(publishResponse.getBody());
            
            result.put("success", true);
            result.put("mediaId", publishJson.get("id").asText());
            result.put("message", "Post created successfully on Instagram");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Instagram text posts require media. Please include an image or use carousel format.");
            result.put("suggestion", "Create a simple branded image background for text content.");
        }

        return result;
    }

    public Map<String, Object> getPostAnalytics(String accessToken, String mediaId) {
        try {
            String url = String.format("%s/%s/%s?fields=id,caption,media_type,media_url,permalink,timestamp,like_count,comments_count&access_token=%s",
                    graphApiUrl, apiVersion, mediaId, accessToken);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("mediaId", jsonNode.get("id").asText());
            result.put("caption", jsonNode.has("caption") ? jsonNode.get("caption").asText() : "");
            result.put("mediaType", jsonNode.get("media_type").asText());
            result.put("permalink", jsonNode.get("permalink").asText());
            result.put("likes", jsonNode.has("like_count") ? jsonNode.get("like_count").asInt() : 0);
            result.put("comments", jsonNode.has("comments_count") ? jsonNode.get("comments_count").asInt() : 0);
            result.put("timestamp", jsonNode.get("timestamp").asText());
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "Failed to fetch post analytics: " + e.getMessage());
            return errorResult;
        }
    }

    public Map<String, Object> createStoryPost(String accessToken, MultipartFile mediaFile, SocialMediaPost post) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get user ID
            Map<String, Object> profileResult = getUserProfile(accessToken);
            if (!(Boolean) profileResult.get("success")) {
                return profileResult;
            }
            String userId = (String) profileResult.get("id");

            // Stories require media URL - return guidance
            result.put("success", false);
            result.put("error", "Instagram Stories require media to be uploaded via URL first");
            result.put("suggestion", "Upload media to Firebase Storage or AWS S3, then use the URL");
            result.put("storyEndpoint", String.format("%s/%s/%s/media", graphApiUrl, apiVersion, userId));

            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to create Instagram story: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> getLongLivedAccessToken(String shortLivedToken) {
        try {
            String url = String.format("%s/%s/oauth/access_token", graphApiUrl, apiVersion);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "ig_exchange_token");
            params.add("client_secret", ""); // Would need to be configured
            params.add("access_token", shortLivedToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("accessToken", jsonNode.get("access_token").asText());
            result.put("expiresIn", jsonNode.get("expires_in").asInt());
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "Failed to get long-lived token: " + e.getMessage());
            return errorResult;
        }
    }
}