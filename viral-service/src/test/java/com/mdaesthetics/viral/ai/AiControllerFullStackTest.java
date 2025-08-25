package com.mdaesthetics.viral.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiControllerFullStackTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    AiService aiService; // ensure bean loads

    @Test
    @DisplayName("Health endpoint full stack")
    void healthFullStack() {
    ResponseEntity<Map<String, Object>> resp = rest.exchange(url("/viral-service/api/ai/health"), HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>(){});
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsEntry("status", "UP");
    }

    @Test
    @DisplayName("Chat endpoint full stack (stub or real)")
    void chatFullStack() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"history\":[],\"prompt\":\"Hi\"}";
    ResponseEntity<Map<String, Object>> resp = rest.exchange(url("/viral-service/api/ai/chat"), HttpMethod.POST, new HttpEntity<>(payload, headers), new ParameterizedTypeReference<Map<String, Object>>(){});
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsEntry("ok", true);
        assertThat(resp.getBody()).containsKey("response");
    }

    @Test
    @DisplayName("Chat endpoint rejects empty prompt")
    void chatRejectsEmptyPrompt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"history\":[],\"prompt\":\"   \"}"; // blank
        ResponseEntity<Map<String, Object>> resp = rest.exchange(url("/viral-service/api/ai/chat"), HttpMethod.POST, new HttpEntity<>(payload, headers), new ParameterizedTypeReference<Map<String, Object>>(){});
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsEntry("ok", false).containsEntry("error", "ValidationError");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
