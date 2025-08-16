
package com.google.adk.models.langchain4j;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LangChain4jTest {
    private static final String MODEL_NAME = "test-model";

    @Test
    @DisplayName("Should construct LangChain4j with model name")
    void testConstructor() {
        LangChain4j langChain4j = new LangChain4j(null, MODEL_NAME);
        assertNotNull(langChain4j);
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when connect is called")
    void testConnectThrowsUnsupportedOperationException() {
        LangChain4j langChain4j = new LangChain4j(null, MODEL_NAME);
        assertThrows(UnsupportedOperationException.class, () -> langChain4j.connect(null));
    }


    final List<Part> parts = response.content().get().parts().orElseThrow();
    assertThat(parts).hasSize(1);
    assertThat(parts.get(0).functionCall()).isPresent();

    final FunctionCall functionCall = parts.get(0).functionCall().orElseThrow();
    assertThat(functionCall.name()).isEqualTo(Optional.of("getWeather"));

package com.google.adk.models.langchain4j;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LangChain4jTest {
    private static final String MODEL_NAME = "test-model";

    @Test
    @DisplayName("Should construct LangChain4j with model name")
    void testConstructor() {
        LangChain4j langChain4j = new LangChain4j(null, MODEL_NAME);
        assertNotNull(langChain4j);
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when connect is called")
    void testConnectThrowsUnsupportedOperationException() {
        LangChain4j langChain4j = new LangChain4j(null, MODEL_NAME);
        assertThrows(UnsupportedOperationException.class, () -> langChain4j.connect(null));
    }

        """
        {
            "name": "John Doe",
            "age": "30",
            "city": "New York"
        }
        """;
    final AiMessage aiMessage = AiMessage.from(jsonResponse);

    final ChatResponse chatResponse = mock(ChatResponse.class);
    when(chatResponse.aiMessage()).thenReturn(aiMessage);
    when(chatModel.chat(any(ChatRequest.class))).thenReturn(chatResponse);

    // When
    final LlmResponse response = langChain4j.generateContent(llmRequest, false).blockingFirst();

    // Then
    // Verify the response contains the expected JSON data
    assertThat(response).isNotNull();

