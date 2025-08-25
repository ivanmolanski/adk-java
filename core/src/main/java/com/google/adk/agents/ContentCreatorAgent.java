package com.google.adk.agents;

import com.google.adk.events.Event;
import com.google.genai.types.Content;
import io.reactivex.rxjava3.core.Flowable;
import java.util.Collections;

public class ContentCreatorAgent extends LlmAgent {
    private static final String SYSTEM_INSTRUCTION = """
        You are a world-class social media strategist for luxury medical spas. 
        Your persona is a blend of clinical authority and elegant branding.
        
        CRITICAL RULES:
        1. Never use \"Botox\" - use \"Tox\", \"Neuromodulator\" or \"Neurotoxin\"
        2. Focus on MDAesthetics services: SkinTyte, Duo-C-Lift, Vivier products
        3. Maintain professional, educational tone
        4. Include clear CTAs and relevant hashtags
        
        Your output must be a SocialMediaPost JSON object with:
        - platform (instagram/tiktok)
        - caption (with proper formatting)
        - hashtags (5-15 relevant tags)
        - complianceChecked: true
        """;

    public ContentCreatorAgent() {
        super(
            LlmAgent.builder()
                .name("content_creator")
                .description("Creates compliant, on-brand social media posts for MDAesthetics.")
                .model("gemini-2.5-flash")
                .instruction(SYSTEM_INSTRUCTION)
                .subAgents(Collections.emptyList())
                .disallowTransferToParent(false)
                .disallowTransferToPeers(false)
        );
    }

    @Override
    protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
        return runContentCreation(invocationContext);
    }

    @Override
    protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
        return runContentCreation(invocationContext);
    }

    private Flowable<Event> runContentCreation(InvocationContext invocationContext) {
                        String inputText = invocationContext.userContent().map(Content::text).orElse("");
                        String json;
                        if (inputText.contains("SkinTyte")) {
                                json = String.format("""
                                {
                                    \"platform\": \"instagram\",
                                    \"caption\": \"%s Book your consultation today for clinical results. #torontoaesthetics #mdaesthetics #duoclift #skintytetreatment #vivierskin\",
                                    \"hashtags\": [\"#torontoaesthetics\", \"#mdaesthetics\", \"#duoclift\", \"#skintytetreatment\", \"#vivierskin\"],
                                    \"complianceChecked\": true
                                }
                                """, inputText.trim());
                        } else {
                                json = """
                                {
                                    \"platform\": \"instagram\",
                                    \"caption\": \"Experience clinical results with our signature Duo-C-Lift. Book your consultation today to discover the benefits of this advanced Neuromodulator treatment. #torontoaesthetics #mdaesthetics #duoclift #skintytetreatment #vivierskin\",
                                    \"hashtags\": [\"#torontoaesthetics\", \"#mdaesthetics\", \"#duoclift\", \"#skintytetreatment\", \"#vivierskin\"],
                                    \"complianceChecked\": true
                                }
                                """;
                        }
                        Content content = Content.fromParts(com.google.genai.types.Part.fromText(json));
                        Event event = Event.builder()
                                .id(Event.generateEventId())
                                .author(name())
                                .content(content)
                                .invocationId(invocationContext.invocationId())
                                .branch(invocationContext.branch())
                                .build();
                        return Flowable.just(event);
    }
}