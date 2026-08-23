package com.promising.jarvis.core.agent;

import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Serializes LLM calls on one named, daemon worker thread. */
public final class SingleThreadLlmAgent implements LlmAgent {
    private static final int QUEUE_CAPACITY = 32;
    private final NLParser parser;
    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final Thread worker;
    private volatile boolean running = true;

    public SingleThreadLlmAgent(NLParser parser) {
        this.parser = parser;
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
                    task.result.complete(parser.parse(task.context.request().text(), task.promptContext));
                } catch (Throwable error) {
                    task.result.completeExceptionally(error);
                }
            } catch (InterruptedException ignored) {
                if (!running) Thread.currentThread().interrupt();
            }
        }
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
