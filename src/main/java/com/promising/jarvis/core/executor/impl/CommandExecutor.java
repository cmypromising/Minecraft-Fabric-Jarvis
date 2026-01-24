package com.promising.jarvis.core.executor.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.promising.jarvis.Jarvis;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.core.parser.impl.DeepSeekParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

public class CommandExecutor {
    private static final NLParser parser = new DeepSeekParser();

    /**
     * 注册命令 - 使用与 Jade 相同的方式
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nl")
                // 权限检查 - 使用 Commands.LEVEL_GAMEMASTERS (权限等级2)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                // 使用 MessageArgument.message() 处理带空格的文本
                .then(Commands.argument("text", MessageArgument.message())
                        .executes(CommandExecutor::executeCommand)
                )
        );
    }

    /**
     * 执行命令
     */
    private static int executeCommand(CommandContext<CommandSourceStack> context) {
        // 获取消息参数
        MessageArgument.Message message = context.getArgument("text", MessageArgument.Message.class);
        String input = message.toString();
        CommandSourceStack source = context.getSource();

        try {
            ContentResponseBody resultBody = parser.parse(input, getCurrentPlayerInformation(source));

            if (resultBody.getType() == 1) {
                String commandToExecute = resultBody.getCommand();
                Jarvis.LOGGER.info("执行命令: {}", commandToExecute);

                // 方式2：直接使用命令分发器
                CommandDispatcher<CommandSourceStack> dispatcher = source.getServer()
                        .getCommands()
                        .getDispatcher();

                // 解析并执行命令
                ParseResults<CommandSourceStack> parseResults = dispatcher.parse(commandToExecute, source);
                int result = dispatcher.execute(parseResults);

                if (result > 0) {
                    source.sendSuccess(() -> Component.literal("✓ 命令执行成功"), false);
                }
            }

            // 发送附加信息
            if (resultBody.getAdditionalInfo() != null && !resultBody.getAdditionalInfo().isEmpty()) {
                source.sendSuccess(() -> Component.literal("Jarvis: " + resultBody.getAdditionalInfo()), false);
            }

            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("命令执行失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 获取当前玩家信息
     */
    private static String getCurrentPlayerInformation(CommandSourceStack source) {
        var player = source.getPlayer();
        if (player == null) {
            return "执行者: " + source.getTextName() + " (非玩家实体)";
        }

        // 玩家位置
        BlockPos pos = player.blockPosition();

        // 玩家状态
        float health = player.getHealth();
        int foodLevel = player.getFoodData().getFoodLevel();
        int experienceLevel = player.experienceLevel;

        return String.format(
                "玩家信息:\n" +
                        "  名称: %s\n" +
                        "  坐标: %.1f, %.1f, %.1f\n" +
                        "  生命值: %.1f\n" +
                        "  饥饿值: %d\n" +
                        "  经验等级: %d",
                player.getName().getString(),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                health, foodLevel, experienceLevel
        );
    }
}