package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.agent.SingleThreadLlmAgent;
import com.promising.jarvis.core.agent.task.AgentTask;
import com.promising.jarvis.core.command.CommandRequest;
import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.core.context.ContextToolRegistry;
import com.promising.jarvis.core.context.PlayerContext;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import net.minecraft.server.command.ServerCommandSource;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Dedicated proactive ReAct agent consuming immutable awareness snapshots. */
public final class ProactiveLlmAgent implements AutoCloseable {
    public static final int MAX_REASONING_STEPS = 6;
    private final SingleThreadLlmAgent agent;
    public ProactiveLlmAgent(NLParser parser, ContextToolRegistry tools) { this.agent = new SingleThreadLlmAgent(parser, tools); }

    public CompletableFuture<ContentResponseBody> submit(ServerCommandSource source, UUID playerId,
                                                          ActiveAwarenessContext awareness) {
        String prompt = "你是 Minecraft 玩家的主动陪伴助手。请基于主动感知缓存和最近事件时序判断玩家是否需要提醒。\n"
                + "只在有明确帮助价值时给出简洁建议；禁止执行任何命令，最终只能返回 minecraft.information、type=2。\n"
                + "以下是主动感知缓存，请优先使用；如需更新事实，再调用只读工具：\n" + awareness.promptText();
        var context = new CommandContext(new CommandRequest("主动评估玩家当前情况"), source,
                PlayerContext.from(source), prompt);
        AgentTask task = AgentTask.builder(context).type("proactive.awareness")
                .priority(com.promising.jarvis.core.agent.task.TaskPriority.LOW)
                .timeout(Duration.ofSeconds(45)).maxReasoningSteps(MAX_REASONING_STEPS).build();
        return agent.submit(task).result();
    }
    @Override public void close() { agent.close(); }
}
