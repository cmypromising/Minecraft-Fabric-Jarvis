package com.promising.jarvis.core.capability;

import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;

/** A pluggable service for one class of model response. */
public interface Capability {
    String id();
    boolean supports(ContentResponseBody response);
    void execute(CommandContext context, ContentResponseBody response);
}
