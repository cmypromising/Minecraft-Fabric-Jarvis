package com.promising.jarvis.core.agent.task;

import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Extensible unit of work consumed by an agent. It owns request metadata,
 * scheduling policy, lifecycle state and the asynchronous result.
 */
public final class AgentTask implements Comparable<AgentTask> {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    public static final int DEFAULT_MAX_REASONING_STEPS = 4;

    private final UUID id;
    private final CommandContext context;
    private final String promptContext;
    private final String type;
    private final Map<String, String> metadata;
    private final TaskPriority priority;
    private final Instant createdAt;
    private final Instant deadline;
    private final int maxReasoningSteps;
    private final CompletableFuture<ContentResponseBody> result = new CompletableFuture<>();
    private final AtomicReference<TaskStatus> status = new AtomicReference<>(TaskStatus.QUEUED);

    private AgentTask(Builder builder) {
        this.id = UUID.randomUUID();
        this.context = Objects.requireNonNull(builder.context, "context");
        this.promptContext = Objects.requireNonNullElse(builder.promptContext, "");
        this.type = Objects.requireNonNull(builder.type, "type");
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
        this.priority = Objects.requireNonNull(builder.priority, "priority");
        this.createdAt = Instant.now();
        this.deadline = createdAt.plus(builder.timeout);
        this.maxReasoningSteps = builder.maxReasoningSteps;
    }

    public static Builder builder(CommandContext context) { return new Builder(context); }

    public UUID id() { return id; }
    public CommandContext context() { return context; }
    public String promptContext() { return promptContext; }
    public String type() { return type; }
    public Map<String, String> metadata() { return metadata; }
    public TaskPriority priority() { return priority; }
    public Instant createdAt() { return createdAt; }
    public Instant deadline() { return deadline; }
    public int maxReasoningSteps() { return maxReasoningSteps; }
    public CompletableFuture<ContentResponseBody> result() { return result; }
    public TaskStatus status() { return status.get(); }

    public boolean isExpired() { return Instant.now().isAfter(deadline); }

    @Override
    public int compareTo(AgentTask other) {
        int priorityOrder = Integer.compare(other.priority.weight(), priority.weight());
        return priorityOrder != 0 ? priorityOrder : createdAt.compareTo(other.createdAt);
    }

    public boolean start() { return status.compareAndSet(TaskStatus.QUEUED, TaskStatus.RUNNING); }
    public void succeed(ContentResponseBody response) {
        if (status.compareAndSet(TaskStatus.RUNNING, TaskStatus.SUCCEEDED)) result.complete(response);
    }
    public void fail(Throwable error) {
        if (status.compareAndSet(TaskStatus.RUNNING, TaskStatus.FAILED)) result.completeExceptionally(error);
    }
    public boolean cancel() {
        TaskStatus current = status.get();
        while (current == TaskStatus.QUEUED || current == TaskStatus.RUNNING) {
            if (status.compareAndSet(current, TaskStatus.CANCELLED)) {
                result.cancel(false);
                return true;
            }
            current = status.get();
        }
        return false;
    }
    public boolean expire() {
        if (status.compareAndSet(TaskStatus.QUEUED, TaskStatus.EXPIRED)
                || status.compareAndSet(TaskStatus.RUNNING, TaskStatus.EXPIRED)) {
            result.completeExceptionally(new IllegalStateException("Agent task expired: " + id));
            return true;
        }
        return false;
    }

    public static final class Builder {
        private final CommandContext context;
        private String promptContext;
        private String type = "llm.request";
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private TaskPriority priority = TaskPriority.NORMAL;
        private Duration timeout = DEFAULT_TIMEOUT;
        private int maxReasoningSteps = DEFAULT_MAX_REASONING_STEPS;

        private Builder(CommandContext context) { this.context = context; }
        public Builder promptContext(String value) { promptContext = value; return this; }
        public Builder type(String value) { type = Objects.requireNonNull(value); return this; }
        public Builder metadata(String key, String value) {
            metadata.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
            return this;
        }
        public Builder priority(TaskPriority value) { priority = value; return this; }
        public Builder timeout(Duration value) { timeout = Objects.requireNonNull(value); return this; }
        public Builder maxReasoningSteps(int value) { maxReasoningSteps = value; return this; }
        public AgentTask build() {
            if (timeout.isZero() || timeout.isNegative()) throw new IllegalArgumentException("timeout must be positive");
            if (maxReasoningSteps < 1) throw new IllegalArgumentException("maxReasoningSteps must be positive");
            return new AgentTask(this);
        }
    }
}
