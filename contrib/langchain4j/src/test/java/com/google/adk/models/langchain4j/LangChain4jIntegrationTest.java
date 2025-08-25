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

import static com.google.adk.models.langchain4j.RunLoop.askAgentStreaming;
import static org.junit.jupiter.api.Assertions.*;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.tools.FunctionTool;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

public class LangChain4jIntegrationTest {

  public static final String GEMINI_2_5_FLASH = "gemini-2.5-flash";
  public static final String GPT_4_O_MINI = "gpt-4o-mini";

  // TODO: Re-enable when Anthropic dependency is available
  /*
  @Test
  @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = "\\S+")
  public void testStreamingRunConfig() {
        LlmAgent agent = LlmAgent.builder()
                .name("streaming-agent")
                .description("Friendly science teacher agent")
                .instruction("""
                        You're a friendly science teacher.
                        You give concise answers about science topics.
                        When someone greets you, respond with "Hello".
                        If someone asks about the weather, call the `getWeather` function.
                """)
                .model(new LangChain4j(GPT_4_O_MINI))
                .tools(FunctionTool.create(ToolExample.class, "getWeather"))
                .build();

    List<Event> eventsHi = askAgentStreaming(agent, "Hi");
    String responseToHi = String.join("", eventsHi.stream().map(event -> event.content().get().text()).toList());
    assertFalse(eventsHi.isEmpty(), "eventsHi should not be empty");
    assertTrue(responseToHi.trim().contains("Hello"), "Response to 'Hi' should be 'Hello'");
  }
  */
}
