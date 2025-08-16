
package com.google.adk.models;

import java.util.List;

public class SocialMediaPost {
    private String id;
    private String platform;
    private String caption;
    private List<String> hashtags;
    private String suggestedMediaType;
    private boolean complianceChecked;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getters and setters
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public String getSuggestedMediaType() {
        return suggestedMediaType;
    }

    public void setSuggestedMediaType(String suggestedMediaType) {
        this.suggestedMediaType = suggestedMediaType;
    }

    public boolean isComplianceChecked() {
        return complianceChecked;
    }

    public void setComplianceChecked(boolean complianceChecked) {
        this.complianceChecked = complianceChecked;
    }
}