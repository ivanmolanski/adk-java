package com.mdaesthetics.viral.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class SocialMediaPost {
    @JsonProperty("platform")
    private String platform; // "tiktok" or "instagram"
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("hashtags")
    private List<String> hashtags;
    
    @JsonProperty("mediaUrl")
    private String mediaUrl; // Optional video/image URL
    
    @JsonProperty("mediaType")
    private String mediaType; // "video", "image", "carousel"
    
    @JsonProperty("scheduledTime")
    private LocalDateTime scheduledTime; // Optional scheduling
    
    @JsonProperty("location")
    private String location; // Optional geo-tagging
    
    @JsonProperty("privacy")
    private String privacy = "public"; // "public", "private", "friends"

    // Default constructor
    public SocialMediaPost() {}

    // Constructor
    public SocialMediaPost(String platform, String content, List<String> hashtags) {
        this.platform = platform;
        this.content = content;
        this.hashtags = hashtags;
    }

    // Getters and Setters
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPrivacy() { return privacy; }
    public void setPrivacy(String privacy) { this.privacy = privacy; }

    public String getFormattedContent() {
        StringBuilder formatted = new StringBuilder(content);
        if (hashtags != null && !hashtags.isEmpty()) {
            formatted.append("\n\n");
            hashtags.forEach(tag -> formatted.append(tag.startsWith("#") ? tag : "#" + tag).append(" "));
        }
        return formatted.toString().trim();
    }
}