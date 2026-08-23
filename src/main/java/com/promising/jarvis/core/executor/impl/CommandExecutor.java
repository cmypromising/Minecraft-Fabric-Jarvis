package com.promising.jarvis.core.executor.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.promising.jarvis.core.JarvisRuntime;
import com.promising.jarvis.core.command.CommandRequest;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/** Brigadier adapter for the intelligent command-line entry points. */
public final class CommandExecutor {
    private CommandExecutor() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nl")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("text", StringArgumentType.greedyString())
                        .executes(CommandExecutor::legacyAction)));
        dispatcher.register(CommandManager.literal("nlp")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> help(context.getSource()))
                .then(CommandManager.argument("text", StringArgumentType.greedyString())
                        .executes(CommandExecutor::nlpAction)));
    }

    private static int legacyAction(CommandContext<ServerCommandSource> context) {
        return submit(context.getSource(), StringArgumentType.getString(context, "text"));
    }

    private static int nlpAction(CommandContext<ServerCommandSource> context) {
        String input = StringArgumentType.getString(context, "text");
        if (input.trim().equalsIgnoreCase("-help") || input.isBlank()) return help(context.getSource());
        return submit(context.getSource(), input);
    }

    private static int submit(ServerCommandSource source, String input) {
        if (source.getPlayer() == null) {
            source.sendError(Text.of("Jarvis 只能由玩家执行。"));
            return 0;
        }
        JarvisRuntime.applicationService().submit(com.promising.jarvis.core.context.CommandContext.from(
                new CommandRequest(input), source));
        return 1;
    }

    private static int help(ServerCommandSource source) {
        source.sendMessage(Text.of("=== Jarvis 智能命令行 ==="));
        source.sendMessage(Text.of("用法：/nlp <你的自然语言请求>"));
        source.sendMessage(Text.of("示例：/nlp 帮我把天气改成晴天"));
        source.sendMessage(Text.of("示例：/nlp 制作附魔台需要什么材料"));
        source.sendMessage(Text.of("记忆管理：/nl-memory status | /nl-memory clear"));
        source.sendMessage(Text.of("要求：玩家权限等级 2；命令会受到游戏模式和安全策略限制。"));
        source.sendMessage(Text.of("兼容入口：/nl <请求>"));
        return 1;
    }
}
