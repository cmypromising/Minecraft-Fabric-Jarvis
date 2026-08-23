package com.promising.jarvis.core.application;

import com.promising.jarvis.Jarvis;
import com.promising.jarvis.core.capability.CapabilityRegistry;
import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.core.memory.MemoryStore;
import com.promising.jarvis.core.memory.MemoryTurn;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Coordinates asynchronous parsing, memory, and capability dispatch. */
public final class DefaultJarvisApplicationService implements JarvisApplicationService {
    private final NLParser parser;
    private final CapabilityRegistry capabilities;
    private final MemoryStore memory;

    public DefaultJarvisApplicationService(NLParser parser, CapabilityRegistry capabilities, MemoryStore memory) {
        this.parser = parser;
        this.capabilities = capabilities;
        this.memory = memory;
    }

    public void submit(CommandContext context) {
        context.source().sendMessage(Text.of("Jarvis 正在处理请求…"));
        CompletableFuture.supplyAsync(() -> parse(context))
                .whenComplete((response, error) -> context.source().getServer().execute(() -> complete(context, response, error)));
    }

    private ContentResponseBody parse(CommandContext context) {
        try {
            String promptContext = context.selectedContext()
                    + memory.promptFor(context.player().playerId());
            return parser.parse(context.request().text(), promptContext);
        } catch (IOException exception) {
            throw new CompletionException(exception);
        }
    }

    private void complete(CommandContext context, ContentResponseBody response, Throwable error) {
        if (error != null || response == null) {
            Throwable cause = error != null && error.getCause() != null ? error.getCause() : error;
            context.source().sendError(Text.of("Jarvis 请求失败：" +
                    (cause == null || cause.getMessage() == null ? "未知错误" : cause.getMessage())));
            return;
        }
        if (!capabilities.dispatch(context, response)) {
            Jarvis.LOGGER.warn("No capability matched LLM response type {}", response.getType());
            context.source().sendError(Text.of("Jarvis 无法处理该响应类型。"));
            return;
        }
        memory.append(context.player().playerId(),
                new MemoryTurn(context.request().text(), response.getAdditionalInfo()));
    }
}
