/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.adk.models.langchain4j;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LangChain4jTest {
    private static final String MODEL_NAME = "test-model";

    @Test
    @DisplayName("Should construct LangChain4j with ChatLanguageModel and model name")
    void testConstructorWithChatModelAndName() {
        ChatLanguageModel chatModel = mock(ChatLanguageModel.class);
        LangChain4j langChain4j = new LangChain4j(chatModel, MODEL_NAME);
        assertNotNull(langChain4j);
    }

    @Test
    @DisplayName("Should construct LangChain4j with StreamingChatLanguageModel and model name")
    void testConstructorWithStreamingChatModelAndName() {
        StreamingChatLanguageModel streamingChatModel = mock(StreamingChatLanguageModel.class);
        LangChain4j langChain4j = new LangChain4j(streamingChatModel, MODEL_NAME);
        assertNotNull(langChain4j);
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when connect is called")
    void testConnectThrowsUnsupportedOperationException() {
        ChatLanguageModel chatModel = mock(ChatLanguageModel.class);
        LangChain4j langChain4j = new LangChain4j(chatModel, MODEL_NAME);
        assertThrows(UnsupportedOperationException.class, () -> langChain4j.connect(null));
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when generateContent is called")
    void testGenerateContentThrowsUnsupportedOperationException() {
        ChatLanguageModel chatModel = mock(ChatLanguageModel.class);
        LangChain4j langChain4j = new LangChain4j(chatModel, MODEL_NAME);
        assertThrows(UnsupportedOperationException.class, () -> langChain4j.generateContent(null, false));
    }
}