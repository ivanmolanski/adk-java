package com.mdaesthetics.viral.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Configuration
    static class StubConfig {}

    @Test
    @DisplayName("Health endpoint returns UP")
    void health() {
        ResponseEntity<java.util.Map<String,Object>> resp = rest.exchange(url("/api/ai/health"), HttpMethod.GET, null, new ParameterizedTypeReference<java.util.Map<String,Object>>(){});
        org.assertj.core.api.Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        org.assertj.core.api.Assertions.assertThat(resp.getBody()).containsEntry("status", "UP");
    }

    @Test
    @DisplayName("Chat endpoint returns stubbed response")
    void chat() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"history\":[],\"prompt\":\"Hello\"}";
        ResponseEntity<java.util.Map<String,Object>> resp = rest.exchange(url("/api/ai/chat"), HttpMethod.POST, new HttpEntity<>(payload, headers), new ParameterizedTypeReference<java.util.Map<String,Object>>(){});
        org.assertj.core.api.Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        org.assertj.core.api.Assertions.assertThat(resp.getBody()).containsEntry("ok", true);
        org.assertj.core.api.Assertions.assertThat(resp.getBody()).containsKey("response");
    }

    private String url(String path) { return "http://localhost:" + port + path; }
}
