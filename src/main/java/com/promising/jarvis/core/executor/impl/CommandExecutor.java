package com.promising.jarvis.core.executor.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.core.parser.impl.DeepSeekParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Executes natural-language requests and applies validated Minecraft commands. */
public class CommandExecutor {
    private static final NLParser parser = new DeepSeekParser();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("nl")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                .executes(CommandExecutor::action))
        );
    }

    private static int action(CommandContext<ServerCommandSource> context) {
        String input = StringArgumentType.getString(context, "text");
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendError(Text.of("Jarvis 只能由玩家执行。"));
            return 0;
        }

        String currentPlayerInfo = getCurrentPlayerInformation(source);
        source.sendMessage(Text.of("Jarvis 正在处理请求…"));

        CompletableFuture
                .supplyAsync(() -> parseSafely(input, currentPlayerInfo))
                .whenComplete((resultBody, error) -> source.getServer().execute(() -> {
                    if (error != null || resultBody == null) {
                        Throwable cause = error != null && error.getCause() != null ? error.getCause() : error;
                        source.sendError(Text.of("Jarvis 请求失败：" +
                                (cause == null || cause.getMessage() == null ? "未知错误" : cause.getMessage())));
                        return;
                    }
                    if (resultBody.getType() == null) {
                        source.sendError(Text.of("Jarvis 返回了无效的响应。"));
                        return;
                    }
                    if (resultBody.getType() == 1) {
                        String command = resultBody.getCommand();
                        if (!CommandSafetyPolicy.isAllowed(command)) {
                            source.sendError(Text.of("Jarvis 生成的命令未通过安全检查。"));
                            return;
                        }
                        source.getServer().getCommandManager().executeWithPrefix(source, command);
                    }
                    source.sendMessage(Text.of("Jarvis: " + resultBody.getAdditionalInfo()));
                }));
        return 1;
    }

    private static ContentResponseBody parseSafely(String input, String currentPlayerInfo) {
        try {
            return parser.parse(input, currentPlayerInfo);
        } catch (IOException exception) {
            throw new CompletionException(exception);
        }
    }

    private static String getCurrentPlayerInformation(ServerCommandSource source) {
        BlockPos playerPosition = source.getPlayer().getBlockPos();
        float health = source.getPlayer().getHealth();
        int foodLevel = source.getPlayer().getHungerManager().getFoodLevel();
        int experience = source.getPlayer().experienceLevel;
        return String.format("\n姓名: %s\n坐标: (%d, %d, %d)\n生命值: %.1f\n饥饿值: %d\n经验等级: %d",
                source.getName(), playerPosition.getX(), playerPosition.getY(), playerPosition.getZ(),
                health, foodLevel, experience);
    }
}
