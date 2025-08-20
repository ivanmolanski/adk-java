
package com.google.adk.models.langchain4j;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LangChain4jTest {
    private static final String MODEL_NAME = "test-model";

    @Test
    @DisplayName("Should construct LangChain4j with model name")
    void testConstructor() {
    LangChain4j langChain4j = new LangChain4j(MODEL_NAME);
        assertNotNull(langChain4j);
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when connect is called")
    void testConnectThrowsUnsupportedOperationException() {
    LangChain4j langChain4j = new LangChain4j(MODEL_NAME);
        assertThrows(UnsupportedOperationException.class, () -> langChain4j.connect(null));
    }
}
