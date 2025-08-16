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

import com.google.adk.models.BaseLlm;
import com.google.adk.models.BaseLlmConnection;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.common.annotations.Beta;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import io.reactivex.rxjava3.core.Flowable;
import java.util.Objects;

@Beta
public class LangChain4j extends BaseLlm {

  private final ChatLanguageModel chatModel;
  private final StreamingChatLanguageModel streamingChatModel;

  public LangChain4j(ChatLanguageModel chatModel) {
    super("langchain4j-chat-model");
    this.chatModel = Objects.requireNonNull(chatModel, "chatModel cannot be null");
    this.streamingChatModel = null;
  }

  public LangChain4j(ChatLanguageModel chatModel, String modelName) {
    super(Objects.requireNonNull(modelName, "chat model name cannot be null"));
    this.chatModel = Objects.requireNonNull(chatModel, "chatModel cannot be null");
    this.streamingChatModel = null;
  }

  public LangChain4j(StreamingChatLanguageModel streamingChatModel) {
    super("langchain4j-streaming-chat-model");
    this.chatModel = null;
    this.streamingChatModel =
        Objects.requireNonNull(streamingChatModel, "streamingChatModel cannot be null");
  }

  public LangChain4j(StreamingChatLanguageModel streamingChatModel, String modelName) {
    super(Objects.requireNonNull(modelName, "model name cannot be null"));
    this.chatModel = null;
    this.streamingChatModel =
        Objects.requireNonNull(streamingChatModel, "streamingChatModel cannot be null");
  }

  public LangChain4j(ChatLanguageModel chatModel, StreamingChatLanguageModel streamingChatModel, String modelName) {
    super(Objects.requireNonNull(modelName, "model name cannot be null"));
    this.chatModel = Objects.requireNonNull(chatModel, "chatModel cannot be null");
    this.streamingChatModel =
        Objects.requireNonNull(streamingChatModel, "streamingChatModel cannot be null");
  }

  @Override
  public Flowable<LlmResponse> generateContent(LlmRequest llmRequest, boolean stream) {
    throw new UnsupportedOperationException("generateContent is not yet implemented for LangChain4j");
  }

  @Override
  public BaseLlmConnection connect(LlmRequest llmRequest) {
    throw new UnsupportedOperationException(
        "Live connection is not supported for LangChain4j models.");
  }
}