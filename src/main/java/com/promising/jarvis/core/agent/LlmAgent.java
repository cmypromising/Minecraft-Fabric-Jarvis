package com.promising.jarvis.core.agent;

import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import com.promising.jarvis.core.agent.task.AgentTask;

import java.util.concurrent.CompletableFuture;

/** Dedicated LLM worker boundary for natural-language requests. */
public interface LlmAgent extends AutoCloseable {
    AgentTask submit(AgentTask task);

    default CompletableFuture<ContentResponseBody> submit(CommandContext context, String promptContext) {
        return submit(AgentTask.builder(context).promptContext(promptContext).build()).result();
    }

    @Override
    void close();
}
