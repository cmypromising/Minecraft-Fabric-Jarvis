package com.promising.jarvis.core.agent;

import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.core.context.ContextToolRegistry;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Serializes LLM calls on one named, daemon worker thread. */
public final class SingleThreadLlmAgent implements LlmAgent {
    private static final int QUEUE_CAPACITY = 32;
    private static final int MAX_REACT_STEPS = 4;
    private final NLParser parser;
    private final ContextToolRegistry tools;
    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final Thread worker;
    private volatile boolean running = true;

    public SingleThreadLlmAgent(NLParser parser, ContextToolRegistry tools) {
        this.parser = parser;
        this.tools = tools;
        this.worker = new Thread(this::runLoop, "jarvis-llm-agent");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    @Override
    public CompletableFuture<ContentResponseBody> submit(CommandContext context, String promptContext) {
        CompletableFuture<ContentResponseBody> result = new CompletableFuture<>();
        if (!running) {
            result.completeExceptionally(new IllegalStateException("LLM agent is stopped"));
            return result;
        }
        if (!queue.offer(new Task(context, promptContext, result))) {
            result.completeExceptionally(new IllegalStateException("LLM agent queue is full"));
        }
        return result;
    }

    private void runLoop() {
        while (running || !queue.isEmpty()) {
            try {
                Task task = queue.poll(250, TimeUnit.MILLISECONDS);
                if (task == null) continue;
                try {
                    task.result.complete(runReasoningLoop(task));
                } catch (Throwable error) {
                    task.result.completeExceptionally(error);
                }
            } catch (InterruptedException ignored) {
                if (!running) Thread.currentThread().interrupt();
            }
        }
    }

    private ContentResponseBody runReasoningLoop(Task task) throws Exception {
        String context = task.promptContext;
        for (int step = 0; step < MAX_REACT_STEPS; step++) {
            ContentResponseBody response = parser.parse(task.context.request().text(),
                    context + "\n\n可用上下文工具（只读）：\n" + tools.describe()
                            + "\n如果需要事实，请返回 capability=context.tool、tool=工具名、tool_arguments=参数；"
                            + "获取工具结果后再返回最终 minecraft.command 或 minecraft.information 响应。"
                            + "最多调用工具 " + MAX_REACT_STEPS + " 次。");
            if (response == null || !response.isContextToolRequest()) return response;

            String toolResult = executeToolOnMinecraftThread(task.context, response.getTool(), response.getToolArguments());
            context += "\n\n工具调用结果 [" + response.getTool() + "]: " + toolResult;
        }
        throw new IllegalStateException("LLM agent exceeded the context tool step limit");
    }

    private String executeToolOnMinecraftThread(CommandContext context, String name, String arguments)
            throws Exception {
        if (context.source() == null) {
            return tools.execute(name, null, arguments).orElse("未知上下文工具: " + name);
        }
        var result = new CompletableFuture<String>();
        context.source().getServer().execute(() -> {
            try {
                result.complete(tools.execute(name, context.source(), arguments).orElse("未知上下文工具: " + name));
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        return result.get(5, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        running = false;
        worker.interrupt();
        if (Thread.currentThread() != worker) {
            try {
                worker.join(2000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private record Task(CommandContext context, String promptContext,
                        CompletableFuture<ContentResponseBody> result) { }
}
