package com.mdaesthetics.viral.model;

import java.time.Instant;
import java.util.List;

/**
 * Raw scraped competitor post stored in Firestore collection competitorPosts.
 * Enhanced to support rich Apify Instagram Scraper data while maintaining backward compatibility.
 */
public record CompetitorPost(
    String id,               // Firestore document ID (set after save)
    String platform,         // instagram | tiktok
    String profile,          // profile handle or URL
    String postUrl,
    String caption,
    List<String> hashtags,
    Long likes,
    Long comments,
    Long shares,
    Long views,
    Double engagementRate,   // (likes+comments)/views or synthetic
    Double evs,              // Engagement Velocity Score
    Instant postedAt,
    Instant scrapedAt,
    // Enhanced Apify metadata (nullable for backward compatibility)
    String mediaType,        // Post, Video, Reel, Story
    String displayUrl,       // Direct media URL from Apify
    String ownerFullName,    // Account display name
    String ownerUsername,    // Account username
    Instant timestamp        // Original post timestamp from Instagram
) {
    
    /**
     * Constructor for backward compatibility with existing code
     */
    public CompetitorPost(String id, String platform, String profile, String postUrl, 
                         String caption, List<String> hashtags, Long likes, Long comments, 
                         Long shares, Long views, Double engagementRate, Double evs, 
                         Instant postedAt, Instant scrapedAt) {
        this(id, platform, profile, postUrl, caption, hashtags, likes, comments, shares, views, 
             engagementRate, evs, postedAt, scrapedAt, null, null, null, null, null);
    }
    
    /**
     * Create a new instance with updated ID (preserving all other fields)
     */
    public CompetitorPost withId(String newId) {
        return new CompetitorPost(newId, platform, profile, postUrl, caption, hashtags, 
                                likes, comments, shares, views, engagementRate, evs, 
                                postedAt, scrapedAt, mediaType, displayUrl, ownerFullName, 
                                ownerUsername, timestamp);
    }
    
    /**
     * Check if this post has enhanced Apify metadata
     */
    public boolean hasApifyMetadata() {
        return mediaType != null || displayUrl != null || ownerFullName != null;
    }
    
    /**
     * Get the best available timestamp (Apify timestamp if available, otherwise postedAt)
     */
    public Instant getBestTimestamp() {
        return timestamp != null ? timestamp : postedAt;
    }
    
    /**
     * Get display name with fallback to username
     */
    public String getDisplayName() {
        if (ownerFullName != null && !ownerFullName.trim().isEmpty()) {
            return ownerFullName;
        }
        return ownerUsername != null ? ownerUsername : profile;
    }
}
