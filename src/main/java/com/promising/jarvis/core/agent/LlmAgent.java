package com.promising.jarvis.core.agent;

import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;

import java.util.concurrent.CompletableFuture;

/** Dedicated LLM worker boundary for natural-language requests. */
public interface LlmAgent extends AutoCloseable {
    CompletableFuture<ContentResponseBody> submit(CommandContext context, String promptContext);

    @Override
    void close();
}
