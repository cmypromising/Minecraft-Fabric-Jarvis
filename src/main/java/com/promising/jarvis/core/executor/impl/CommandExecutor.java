package com.promising.jarvis.core.executor.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.promising.jarvis.core.JarvisRuntime;
import com.promising.jarvis.core.command.CommandRequest;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/** Thin Brigadier adapter for the intelligent command-line entry point. */
public final class CommandExecutor {
    private CommandExecutor() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nl")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("text", StringArgumentType.greedyString())
                        .executes(CommandExecutor::action)));
    }

    private static int action(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendError(Text.of("Jarvis 只能由玩家执行。"));
            return 0;
        }
        JarvisRuntime.applicationService().submit(com.promising.jarvis.core.context.CommandContext.from(
                new CommandRequest(StringArgumentType.getString(context, "text")), source));
        return 1;
    }
}
