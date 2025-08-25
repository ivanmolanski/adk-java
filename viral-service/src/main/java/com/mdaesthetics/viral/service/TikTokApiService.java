package com.mdaesthetics.viral.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class TikTokApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${social.tiktok.api-base-url:https://open.tiktokapis.com}")
    private String apiBaseUrl;

    public TikTokApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> getUserProfile(String accessToken) {
        try {
            String url = apiBaseUrl + "/v2/user/info/";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Map<String, Object> result = new HashMap<>();
            
            if (jsonNode.has("data") && jsonNode.get("data").has("user")) {
                JsonNode user = jsonNode.get("data").get("user");
                result.put("success", true);
                result.put("username", user.get("username").asText());
                result.put("displayName", user.get("display_name").asText());
                result.put("followerCount", user.get("follower_count").asLong());
                result.put("verified", user.get("is_verified").asBoolean());
            } else {
                result.put("success", false);
                result.put("error", "User profile not found");
            }
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "Failed to fetch user profile: " + e.getMessage());
            return errorResult;
        }
    }

    public Map<String, Object> uploadVideo(String accessToken, MultipartFile videoFile, SocialMediaPost post) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Initialize upload
            String initUrl = apiBaseUrl + "/v2/post/publish/video/init/";
            
            Map<String, Object> initRequest = new HashMap<>();
            Map<String, Object> postInfo = new HashMap<>();
            postInfo.put("title", post.getContent());
            postInfo.put("privacy_level", post.getPrivacy().toUpperCase());
            postInfo.put("disable_duet", false);
            postInfo.put("disable_comment", false);
            postInfo.put("disable_stitch", false);
            initRequest.put("post_info", postInfo);
            
            Map<String, Object> sourceInfo = new HashMap<>();
            sourceInfo.put("source", "FILE_UPLOAD");
            sourceInfo.put("video_size", videoFile.getSize());
            sourceInfo.put("chunk_size", Math.min(videoFile.getSize(), 10 * 1024 * 1024)); // 10MB chunks
            sourceInfo.put("total_chunk_count", 1);
            initRequest.put("source_info", sourceInfo);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String initBody = objectMapper.writeValueAsString(initRequest);
            HttpEntity<String> initEntity = new HttpEntity<>(initBody, headers);

            ResponseEntity<String> initResponse = restTemplate.exchange(
                initUrl, HttpMethod.POST, initEntity, String.class);

            JsonNode initJson = objectMapper.readTree(initResponse.getBody());
            
            if (!initJson.has("data")) {
                result.put("success", false);
                result.put("error", "Failed to initialize upload");
                return result;
            }

            String publishId = initJson.get("data").get("publish_id").asText();
            String uploadUrl = initJson.get("data").get("upload_url").asText();

            // Step 2: Upload video file
            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            uploadHeaders.set("Content-Range", "bytes 0-" + (videoFile.getSize() - 1) + "/" + videoFile.getSize());

            HttpEntity<byte[]> uploadEntity = new HttpEntity<>(videoFile.getBytes(), uploadHeaders);
            
            ResponseEntity<String> uploadResponse = restTemplate.exchange(
                uploadUrl, HttpMethod.PUT, uploadEntity, String.class);

            // Step 3: Publish video
            String publishUrl = apiBaseUrl + "/v2/post/publish/status/fetch/";
            
            Map<String, Object> publishRequest = new HashMap<>();
            publishRequest.put("publish_id", publishId);
            
            String publishBody = objectMapper.writeValueAsString(publishRequest);
            HttpEntity<String> publishEntity = new HttpEntity<>(publishBody, headers);

            // Poll for status (simplified - in production, implement proper polling)
            Thread.sleep(2000); // Wait 2 seconds
            
            ResponseEntity<String> statusResponse = restTemplate.exchange(
                publishUrl, HttpMethod.POST, publishEntity, String.class);

            JsonNode statusJson = objectMapper.readTree(statusResponse.getBody());
            
            result.put("success", true);
            result.put("publishId", publishId);
            result.put("status", statusJson.get("data").get("status").asText());
            result.put("message", "Video uploaded successfully to TikTok");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to upload video: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> createTextPost(String accessToken, SocialMediaPost post) {
        // TikTok doesn't support text-only posts, return guidance
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "TikTok requires video content. Text-only posts are not supported.");
        result.put("suggestion", "Please provide a video file or use Instagram for text posts.");
        return result;
    }

    public Map<String, Object> getPostAnalytics(String accessToken, String videoId) {
        try {
            String url = apiBaseUrl + "/v2/video/query/";
            
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> filters = new HashMap<>();
            filters.put("video_ids", new String[]{videoId});
            request.put("filters", filters);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String requestBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            Map<String, Object> result = new HashMap<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("videos")) {
                JsonNode videos = jsonNode.get("data").get("videos");
                if (videos.size() > 0) {
                    JsonNode video = videos.get(0);
                    result.put("success", true);
                    result.put("views", video.get("view_count").asLong());
                    result.put("likes", video.get("like_count").asLong());
                    result.put("comments", video.get("comment_count").asLong());
                    result.put("shares", video.get("share_count").asLong());
                }
            } else {
                result.put("success", false);
                result.put("error", "Video analytics not found");
            }
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "Failed to fetch analytics: " + e.getMessage());
            return errorResult;
        }
    }
}