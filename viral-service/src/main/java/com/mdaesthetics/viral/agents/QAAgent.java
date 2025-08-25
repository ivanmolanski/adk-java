package com.mdaesthetics.viral.agents;

import com.mdaesthetics.viral.model.ContentDraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Lightweight QA/Compliance validator for generated content before emailing or exposing in UI.
 * Rules (initial):
 *  - Must contain a CTA (book, consultation, link in bio)
 *  - Has between 5 and 15 hashtags
 *  - No forbidden terms (e.g., "Botox" literal); enforce substitution policy
 *  - Mentions at least one pillar service keyword (duo-c-lift, skintyte, radiesse, vivier, body, collagen)
 */
@Component
public class QAAgent {
    private static final Logger log = LoggerFactory.getLogger(QAAgent.class);

    private static final Set<String> FORBIDDEN = Set.of("botox");
    private static final List<String> CTA_KEYWORDS = List.of("book", "consult", "link in bio", "call", "schedule");
    private static final List<String> SERVICE_KEYWORDS = List.of(
        "duo-c-lift", "duoclift", "ultherapy", "skintyte", "radiesse", "vivier", "collagen", "body", "buttock", "firm"
    );

    public ValidationResult validate(ContentDraft draft) {
        List<String> notes = new ArrayList<>();
        boolean passed = true;

        String textAggregate = (draft.hook() + "\n" + draft.body() + "\n" + draft.callToAction()).toLowerCase();

        // CTA presence
        boolean hasCta = CTA_KEYWORDS.stream().anyMatch(textAggregate::contains);
        if (!hasCta) { notes.add("Missing clear CTA"); passed = false; }

        // Hashtag count
        int hashtagCount = draft.hashtags()==null?0:draft.hashtags().size();
        if (hashtagCount < 5 || hashtagCount > 15) { notes.add("Hashtag count out of range (5-15): "+hashtagCount); passed = false; }

        // Forbidden terms
        boolean forbiddenFound = FORBIDDEN.stream().anyMatch(textAggregate::contains);
        if (forbiddenFound) { notes.add("Contains forbidden term 'Botox' (should use Tox/Neuromodulator/Neurotoxin)"); passed = false; }

        // Service keyword presence
        boolean hasService = SERVICE_KEYWORDS.stream().anyMatch(textAggregate::contains);
        if (!hasService) { notes.add("No core service keyword detected"); passed = false; }

        log.info("[qa] draftId={} passed={} notes={}", draft.id(), passed, notes);
        return new ValidationResult(passed, notes);
    }

    public record ValidationResult(boolean passed, List<String> notes) {}
}
