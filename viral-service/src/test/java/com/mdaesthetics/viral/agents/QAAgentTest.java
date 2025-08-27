package com.mdaesthetics.viral.agents;

import com.mdaesthetics.viral.model.ContentDraft;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QAAgentTest {

    @Test
    void passesValidDraft() {
        QAAgent agent = new QAAgent();
    ContentDraft draft = new ContentDraft(null, "trend1", "SkinTyte", "Hook line",
        "Hook line\nEducational body about SkinTyte infrared tightening improving collagen firming. Book your consult now.",
        List.of("#skintyte", "#torontoaesthetics", "#mdaesthetics", "#ultherapy", "#collagen"),
                "Book your consult now.", true, true, "", Instant.now());
        QAAgent.ValidationResult res = agent.validate(draft);
        assertTrue(res.passed(), "Expected QA to pass valid draft");
    }

    @Test
    void failsWithoutCta() {
        QAAgent agent = new QAAgent();
        ContentDraft draft = new ContentDraft(null, "trend1", "SkinTyte", "Hook line",
                "Hook line without CTA", List.of("#skintyte"), "", true, true, "", Instant.now());
        QAAgent.ValidationResult res = agent.validate(draft);
        assertFalse(res.passed(), "Expected QA to fail when CTA missing");
    }

    @Test
    void failsForbiddenBotox() {
        QAAgent agent = new QAAgent();
        ContentDraft draft = new ContentDraft(null, "t2", "SkinTyte", "Hook",
                "Educational body mentioning Botox explicitly which is forbidden.",
                List.of("#skintyte", "#a", "#b", "#c", "#d"),
                "Book now", true, true, "", Instant.now());
        QAAgent.ValidationResult res = agent.validate(draft);
        assertFalse(res.passed(), "Should fail for forbidden term botox");
    }

    @Test
    void failsTooFewHashtags() {
        QAAgent agent = new QAAgent();
        ContentDraft draft = new ContentDraft(null, "t3", "SkinTyte", "Hook",
                "Body with CTA book consult skintyte", List.of("#onlyone"),
                "Book consult", true, true, "", Instant.now());
        QAAgent.ValidationResult res = agent.validate(draft);
        assertFalse(res.passed(), "Should fail with too few hashtags");
    }

    @Test
    void failsTooManyHashtags() {
        QAAgent agent = new QAAgent();
        ContentDraft draft = new ContentDraft(null, "t4", "SkinTyte", "Hook",
                "Body book consult skintyte", List.of(
                "#1","#2","#3","#4","#5","#6","#7","#8","#9","#10","#11","#12","#13","#14","#15","#16"),
                "Book consult", true, true, "", Instant.now());
        QAAgent.ValidationResult res = agent.validate(draft);
        assertFalse(res.passed(), "Should fail with too many hashtags");
    }

    @Test
    void failsMissingServiceKeyword() {
        QAAgent agent = new QAAgent();
        ContentDraft draft = new ContentDraft(null, "t5", "General", "Hook",
                "Educational narrative with CTA inviting a consultation but intentionally omits any core service markers.",
                List.of("#hash1","#hash2","#hash3","#hash4","#hash5"),
                "Book consult", true, true, "", Instant.now());
        QAAgent.ValidationResult res = agent.validate(draft);
        assertFalse(res.passed(), "Should fail missing service keyword");
    }
}
