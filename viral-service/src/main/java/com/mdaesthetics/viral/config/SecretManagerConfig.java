package com.mdaesthetics.viral.config;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for managing Firebase/Google Cloud secrets in production deployment.
 * 
 * This class integrates with Google Cloud Secret Manager to retrieve sensitive configuration
 * values like API keys, service account credentials, and other secrets that should not be
 * hardcoded in the application.
 */
@Configuration
@Profile("production") // Only active in production mode
public class SecretManagerConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecretManagerConfig.class);
    
    @Value("${google.project.id:contentforge-ai-ygy25}")
    private String projectId;
    
    private SecretManagerServiceClient secretManagerClient;
    private final Map<String, String> secretCache = new HashMap<>();
    
    @PostConstruct
    public void init() {
        try {
            secretManagerClient = SecretManagerServiceClient.create();
            logger.info("Secret Manager client initialized for project: {}", projectId);
            
            // Pre-load critical secrets
            preloadSecrets();
        } catch (IOException e) {
            logger.error("Failed to initialize Secret Manager client", e);
            throw new RuntimeException("Cannot initialize Secret Manager", e);
        }
    }
    
    /**
     * Pre-load commonly used secrets to reduce latency during runtime
     */
    private void preloadSecrets() {
        String[] criticalSecrets = {
            "OPENROUTER_API_KEY",
            "SERVICE_ACCOUNT_JSON",
            "FIREBASE_API_KEY"
        };
        
        for (String secretName : criticalSecrets) {
            try {
                getSecret(secretName);
                logger.debug("Pre-loaded secret: {}", secretName);
            } catch (Exception e) {
                logger.warn("Failed to pre-load secret {}: {}", secretName, e.getMessage());
            }
        }
    }
    
    /**
     * Retrieve a secret from Google Cloud Secret Manager
     * 
     * @param secretName The name of the secret to retrieve
     * @return The secret value as a string
     * @throws RuntimeException if the secret cannot be retrieved
     */
    public String getSecret(String secretName) {
        // Check cache first
        if (secretCache.containsKey(secretName)) {
            return secretCache.get(secretName);
        }
        
        try {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretName, "latest");
            AccessSecretVersionResponse response = secretManagerClient.accessSecretVersion(secretVersionName);
            String secretValue = response.getPayload().getData().toStringUtf8();
            
            // Cache the secret for subsequent requests
            secretCache.put(secretName, secretValue);
            
            logger.debug("Successfully retrieved secret: {}", secretName);
            return secretValue;
        } catch (Exception e) {
            logger.error("Failed to access secret {}: {}", secretName, e.getMessage());
            throw new RuntimeException("Cannot retrieve secret: " + secretName, e);
        }
    }
    
    /**
     * Get the OpenRouter API key from Secret Manager
     */
    @Bean("openRouterApiKey")
    public String getOpenRouterApiKey() {
        return getSecret("OPENROUTER_API_KEY");
    }
    
    /**
     * Get the service account JSON from Secret Manager
     */
    @Bean("serviceAccountJson")
    public String getServiceAccountJson() {
        return getSecret("SERVICE_ACCOUNT_JSON");
    }
    
    /**
     * Get Firebase API key from Secret Manager
     */
    @Bean("firebaseApiKey")
    public String getFirebaseApiKey() {
        return getSecret("FIREBASE_API_KEY");
    }
    
    /**
     * Clean up resources
     */
    public void destroy() {
        if (secretManagerClient != null) {
            secretManagerClient.close();
        }
    }
}