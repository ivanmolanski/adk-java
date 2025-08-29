package com.google.adk.models;

import io.reactivex.rxjava3.core.Flowable;
import java.util.Collections;
import com.google.genai.types.Content;
import com.google.genai.types.Candidate;
import com.google.genai.types.GenerateContentResponse;

/**
 * Lightweight local stub LLM used for tests and local runs when OpenRouter provider
 * is not available. It returns a deterministic simple response.
 */
public final class LocalStubLlm extends BaseLlm {

  public LocalStubLlm(String model) {
    super(model);
  }

  @Override
  public Flowable<LlmResponse> generateContent(LlmRequest llmRequest, boolean stream) {
    // Return a single simple response which echoes the last user part or a fixed token.
    String reply = "(stub) READY";
    try {
      if (llmRequest != null && llmRequest.contents() != null && !llmRequest.contents().isEmpty()) {
        Content last = llmRequest.contents().get(llmRequest.contents().size() - 1);
        if (last.parts().isPresent() && !last.parts().get().isEmpty()) {
          last.parts().get().get(0).text().ifPresent(t -> {
            // ignore for now
          });
        }
      }
    } catch (Exception ignored) {
    }
    Candidate candidate = Candidate.builder().content(Content.builder().parts(java.util.List.of(com.google.genai.types.Part.builder().text(reply).build())).build()).build();
    GenerateContentResponse resp = GenerateContentResponse.builder().candidates(Collections.singletonList(candidate)).build();
    return Flowable.just(LlmResponse.create(resp));
  }

  @Override
  public BaseLlmConnection connect(LlmRequest llmRequest) {
    throw new UnsupportedOperationException("LocalStubLlm does not support live connections");
  }
}
