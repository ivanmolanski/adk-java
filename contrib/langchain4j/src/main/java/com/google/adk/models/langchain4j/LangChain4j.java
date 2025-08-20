
package com.google.adk.models.langchain4j;

import com.google.adk.models.BaseLlm;
import com.google.adk.models.BaseLlmConnection;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import io.reactivex.rxjava3.core.Flowable;

public class LangChain4j extends BaseLlm {
  public LangChain4j(String model) {
    super(model);
  }

  @Override
  public Flowable<LlmResponse> generateContent(LlmRequest llmRequest, boolean stream) {
    throw new UnsupportedOperationException("generateContent is not yet implemented for LangChain4j 0.25.0");
  }

  @Override
  public BaseLlmConnection connect(LlmRequest llmRequest) {
    throw new UnsupportedOperationException("Live connection is not supported for LangChain4j models.");
  }
}
