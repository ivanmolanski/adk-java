package com.mdaesthetics.viral.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.endpoints.web.exposure.include=health,info,metrics",
        "management.endpoint.metrics.enabled=true"
})
public class MetricsExposureTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String url(String p){ return "http://localhost:"+port+"/viral-service/actuator"+p; }

    @Test
    void customMetricsRegistered() {
        // Query list of metrics
        ResponseEntity<String> list = rest.getForEntity(url("/metrics"), String.class);
        assertEquals(200, list.getStatusCode().value());
        String body = list.getBody();
        assertNotNull(body);
        assertTrue(body.contains("trendAnalysis.cache.hit"), "trendAnalysis.cache.hit metric missing");
        assertTrue(body.contains("workflow.execute.timer"), "workflow.execute.timer metric missing");
        assertTrue(body.contains("llm.call.latency"), "llm.call.latency metric missing");
        assertTrue(body.contains("email.sent.count"), "email.sent.count metric missing");
    }
}
