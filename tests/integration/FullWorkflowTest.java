package com.google.adk;

import com.google.adk.agents.*;
import com.google.adk.models.*;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FullWorkflowTest {

    @Autowired
    private TrendAnalyzerAgent trendAnalyzer;

    @Autowired
    private ContentCreatorAgent contentCreator;

    @Autowired
    private ComplianceAgent complianceAgent;

    @Autowired
    private Firestore firestore;

    @Test
    public void testFullWorkflow() throws Exception {
        // 1. Simulate scraped post data
        String testPost = """
            {
                "platform": "instagram",
                "profile": "test_aesthetics",
                "caption": "Check out our new Botox specials! Only $99",
                "likes": 150,
                "comments": 20,
                "timestamp": "2025-08-12T10:00:00Z"
            }
            """;

        // 2. Run through TrendAnalyzer
        Object analysis = trendAnalyzer.process(testPost, null);
        assertNotNull(analysis);
        assertTrue(analysis instanceof Map);

        // 3. Run through ContentCreator
        Object generatedContent = contentCreator.process(analysis, null);
        assertNotNull(generatedContent);
        assertTrue(generatedContent instanceof String);

        // 4. Run through ComplianceAgent
        Object complianceCheck = complianceAgent.process(generatedContent, null);
        assertNotNull(complianceCheck);
        assertTrue(complianceCheck instanceof Map);

        Map<String, Object> result = (Map<String, Object>) complianceCheck;
        assertFalse(result.get("sanitized").toString().contains("Botox"));
        assertFalse(result.get("sanitized").toString().contains("$99"));
        assertTrue(result.get("sanitized").toString().contains("Tox"));

        // 5. Verify Firestore integration
        var docRef = firestore.collection("processed_posts").document("test_post");
        docRef.set(Map.of(
            "original", testPost,
            "processed", result.get("sanitized"),
            "timestamp", System.currentTimeMillis()
        )).get();
        
        var doc = docRef.get().get();
        assertTrue(doc.exists());
    }
}