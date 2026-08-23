package com.promising.jarvis.core.executor.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.promising.jarvis.core.JarvisRuntime;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/** Player-scoped management commands for short-term Jarvis memory. */
public final class MemoryCommandExecutor {
    private MemoryCommandExecutor() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nl-memory")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("clear").executes(context -> clear(context.getSource())))
                .then(CommandManager.literal("status").executes(context -> status(context.getSource()))));
    }

    private static int clear(ServerCommandSource source) {
        if (source.getPlayer() == null) {
            source.sendError(Text.of("Jarvis 记忆管理只能由玩家执行。"));
            return 0;
        }
        JarvisRuntime.memoryStore().clear(source.getPlayer().getUuid());
        source.sendMessage(Text.of("Jarvis：已清除你的短期对话记忆。"));
        return 1;
    }

    private static int status(ServerCommandSource source) {
        if (source.getPlayer() == null) {
            source.sendError(Text.of("Jarvis 记忆管理只能由玩家执行。"));
            return 0;
        }
        int turns = JarvisRuntime.memoryStore().size(source.getPlayer().getUuid());
        source.sendMessage(Text.of("Jarvis：当前保留 " + turns + " 轮短期对话记忆。"));
        return turns;
    }
}
