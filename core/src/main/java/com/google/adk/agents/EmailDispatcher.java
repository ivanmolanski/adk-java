package com.google.adk.agents;

import com.google.adk.events.Event;
import com.google.adk.models.SocialMediaPost;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import java.util.Collections;


public class EmailDispatcher extends BaseAgent {
    public EmailDispatcher() {
        super(
            "email_dispatcher",
            "Dispatches daily viral content digest emails for MDAesthetics.",
            Collections.emptyList(),
            null,
            null
        );
    }

    @Override
    protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
        return runEmailDispatch(invocationContext);
    }

    @Override
    protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
        return runEmailDispatch(invocationContext);
    }

    private Flowable<Event> runEmailDispatch(InvocationContext invocationContext) {
        // TODO: Fix input access pattern. This is a placeholder to match other agents.
        Object input = null; // invocationContext.get("input").orElse(null);
        if (!(input instanceof SocialMediaPost)) {
            return Flowable.error(new IllegalArgumentException("Input must be a SocialMediaPost"));
        }
        SocialMediaPost post = (SocialMediaPost) input;
        // TODO: Integrate Gmail API and send email using credentials from secrets.
        // For now, just emit an event with the email content as a string.
        String emailContent = createEmailContent(post);
    Content content = Content.fromParts(Part.fromText(emailContent));
        Event event = Event.builder()
            .id(Event.generateEventId())
            .author(name())
            .content(content)
            .invocationId(invocationContext.invocationId())
            .branch(invocationContext.branch())
            .build();
        return Flowable.just(event);
    }

    private String createEmailContent(SocialMediaPost post) {
        return String.format(
            "<html>" +
            "<body>" +
            "<h2>MD Aesthetics Daily Viral Content Digest</h2>" +
            "<h3>Generated Post:</h3>" +
            "<p>%s</p>" +
            "<h4>Hashtags:</h4>" +
            "<p>%s</p>" +
            "<p><i>This post was automatically generated based on trending content analysis.</i></p>" +
            "</body>" +
            "</html>",
            post.getCaption(),
            String.join(", ", post.getHashtags())
        );
    }
}