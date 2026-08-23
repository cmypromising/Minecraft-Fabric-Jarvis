package com.promising.jarvis.core.agent;

import com.promising.jarvis.core.context.ContextToolRegistry;
import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.core.agent.task.AgentTask;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.Set;

/** Serializes LLM calls on one named, daemon worker thread. */
public final class SingleThreadLlmAgent implements LlmAgent {
    private static final int QUEUE_CAPACITY = 32;
    private final NLParser parser;
    private final ContextToolRegistry tools;
    private final BlockingQueue<AgentTask> queue = new PriorityBlockingQueue<>();
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
    public AgentTask submit(AgentTask task) {
        CompletableFuture<ContentResponseBody> result = task.result();
        if (!running) {
            task.cancel();
            return task;
        }
        if (queue.size() >= QUEUE_CAPACITY || !queue.offer(task)) {
            task.cancel();
        }
        return task;
    }

    private void runLoop() {
        while (running || !queue.isEmpty()) {
            try {
                AgentTask task = queue.poll(250, TimeUnit.MILLISECONDS);
                if (task == null) continue;
                if (task.isExpired()) {
                    task.expire();
                    continue;
                }
                if (!task.start()) continue;
                try {
                    task.succeed(runReasoningLoop(task));
                } catch (Throwable error) {
                    task.fail(error);
                }
            } catch (InterruptedException ignored) {
                if (!running) Thread.currentThread().interrupt();
            }
        }
    }

    private ContentResponseBody runReasoningLoop(AgentTask task) throws Exception {
        String context = task.promptContext();
        Set<String> executedToolCalls = new HashSet<>();
        boolean forceFinalResponse = false;
        for (int step = 0; step < task.maxReasoningSteps(); step++) {
            if (task.isExpired()) {
                task.expire();
                throw new IllegalStateException("Agent task expired: " + task.id());
            }
            ContentResponseBody response = parser.parse(task.context().request().text(),
                    context + "\n\n可用上下文工具（只读）：\n" + tools.describe()
                            + "\n如果需要事实，请返回 capability=context.tool、tool=工具名、tool_arguments=参数；"
                            + "获取工具结果后再返回最终 minecraft.command 或 minecraft.information 响应。"
                            + "最多调用工具 " + task.maxReasoningSteps() + " 次。");
            if (response == null || !response.isContextToolRequest()) return response;

            String toolName = response.getTool();
            String toolArguments = response.getToolArguments() == null ? "" : response.getToolArguments();
            String callKey = toolName + "\u0000" + toolArguments;
            if (forceFinalResponse || !executedToolCalls.add(callKey)) {
                forceFinalResponse = true;
                context += "\n\nSystem constraint: tool " + toolName
                        + " was already called. Return the final response without calling tools again.";
                continue;
            }
            String toolResult = executeToolOnMinecraftThread(task.context(), toolName, toolArguments);
            context += "\n\n工具调用结果 [" + response.getTool() + "]: " + toolResult;
        }
        ContentResponseBody fallback = new ContentResponseBody();
        fallback.setCapability("minecraft.information");
        fallback.setType(3);
        fallback.setAdditionalInfo("上下文工具调用已达到安全上限，已停止重复读取。请重新描述需求，或明确指定需要查询的信息。");
        return fallback;
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
        queue.forEach(AgentTask::cancel);
        queue.clear();
        if (Thread.currentThread() != worker) {
            try {
                worker.join(2000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
