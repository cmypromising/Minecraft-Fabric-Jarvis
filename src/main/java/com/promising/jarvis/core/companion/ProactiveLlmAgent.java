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

/** Dedicated proactive ReAct agent. It never executes Minecraft commands. */
public final class ProactiveLlmAgent implements AutoCloseable {
    private final SingleThreadLlmAgent agent;

    public ProactiveLlmAgent(NLParser parser, ContextToolRegistry tools) {
        this.agent = new SingleThreadLlmAgent(parser, tools);
    }

    public CompletableFuture<ContentResponseBody> submit(ServerCommandSource source, UUID playerId,
                                                          String eventWindow, ProactivePerception perception) {
        String prompt = "你是 Minecraft 玩家的主动陪伴助手。请基于最近事件时序判断玩家是否确实需要被提醒。\n"
                + "只在有明确帮助价值时给出简洁、因地制宜的建议；如果事件只是正常行为，也要返回简短说明而不是制造危险。\n"
                + "禁止执行任何命令，最终只能返回 minecraft.information，type=2；不要声称执行了操作。\n"
                + "当前主动感知摘要：维度=" + perception.dimension() + "，生物群系=" + perception.biome()
                + "，阶段=" + perception.gamePhase() + "，夜晚=" + perception.night()
                + "，危险信号=" + perception.dangers() + "。\n" + eventWindow;
        var context = new CommandContext(new CommandRequest("主动评估玩家当前情况"), source,
                PlayerContext.from(source), prompt);
        AgentTask task = AgentTask.builder(context).type("proactive.awareness")
                .priority(com.promising.jarvis.core.agent.task.TaskPriority.LOW)
                .timeout(Duration.ofSeconds(45)).maxReasoningSteps(3).build();
        return agent.submit(task).result();
    }

    @Override public void close() { agent.close(); }
}
