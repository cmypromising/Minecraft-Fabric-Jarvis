package com.promising.jarvis.core.executor.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.promising.jarvis.core.JarvisRuntime;
import com.promising.jarvis.core.command.CommandRequest;
import com.promising.jarvis.core.companion.GoalPriority;
import com.promising.jarvis.core.companion.GoalStatus;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/** Player-facing controls for companion goals and proactive notification consent. */
public final class CompanionCommandExecutor {
    private CompanionCommandExecutor() { }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nl-goal")
                .requires(source -> source.getPlayer() != null)
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("title", StringArgumentType.greedyString())
                                .executes(CompanionCommandExecutor::addGoal)))
                .then(CommandManager.literal("list").executes(CompanionCommandExecutor::listGoals))
                .then(CommandManager.literal("pause").then(CommandManager.argument("id", StringArgumentType.word())
                        .executes(context -> setStatus(context, GoalStatus.PAUSED))))
                .then(CommandManager.literal("resume").then(CommandManager.argument("id", StringArgumentType.word())
                        .executes(context -> setStatus(context, GoalStatus.ACTIVE))))
                .then(CommandManager.literal("complete").then(CommandManager.argument("id", StringArgumentType.word())
                        .executes(context -> setStatus(context, GoalStatus.COMPLETED))))
                .then(CommandManager.literal("guide").then(CommandManager.argument("id", StringArgumentType.word())
                        .executes(CompanionCommandExecutor::guideGoal)))
                .then(CommandManager.literal("remove").then(CommandManager.argument("id", StringArgumentType.word())
                        .executes(CompanionCommandExecutor::removeGoal))));

        dispatcher.register(CommandManager.literal("nl-proactive")
                .requires(source -> source.getPlayer() != null)
                .then(CommandManager.literal("on").executes(context -> setProactive(context, true)))
                .then(CommandManager.literal("off").executes(context -> setProactive(context, false)))
                .then(CommandManager.literal("status").executes(CompanionCommandExecutor::proactiveStatus)));
    }

    private static int addGoal(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        String title = StringArgumentType.getString(context, "title");
        var goal = JarvisRuntime.goalService().create(player.getUuid(), title, "", GoalPriority.NORMAL, null, 0, true);
        context.getSource().sendFeedback(() -> Text.of("Jarvis：已记录目标「" + goal.title() + "」\n目标 ID：" + goal.id()), false);
        return 1;
    }

    private static int listGoals(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        var goals = JarvisRuntime.goalService().activeGoals(player.getUuid());
        if (goals.isEmpty()) context.getSource().sendFeedback(() -> Text.of("Jarvis：当前没有活动目标。"), false);
        else goals.forEach(goal -> context.getSource().sendFeedback(
                () -> Text.of("- " + goal.id() + " | " + goal.title() + " | " + goal.status()), false));
        return goals.size();
    }

    private static int setStatus(CommandContext<ServerCommandSource> context, GoalStatus status) {
        try {
            var player = context.getSource().getPlayer();
            var goal = JarvisRuntime.goalService().setStatus(player.getUuid(), java.util.UUID.fromString(
                    StringArgumentType.getString(context, "id")), status);
            context.getSource().sendFeedback(() -> Text.of("Jarvis：目标「" + goal.title() + "」已变更为 " + status), false);
            return 1;
        } catch (IllegalArgumentException exception) {
            context.getSource().sendError(Text.of("Jarvis：目标 ID 无效或目标不存在。"));
            return 0;
        }
    }

    private static int removeGoal(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        boolean removed;
        try {
            removed = JarvisRuntime.goalService().remove(player.getUuid(), java.util.UUID.fromString(
                    StringArgumentType.getString(context, "id")));
        } catch (IllegalArgumentException exception) { removed = false; }
        if (removed) context.getSource().sendFeedback(() -> Text.of("Jarvis：目标已移除。"), false);
        else context.getSource().sendError(Text.of("Jarvis：目标 ID 无效或目标不存在。"));
        return removed ? 1 : 0;
    }

    private static int guideGoal(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        try {
            var goal = JarvisRuntime.goalService().get(player.getUuid(), java.util.UUID.fromString(
                    StringArgumentType.getString(context, "id")));
            String prompt = "请作为 Minecraft 陪伴助手，为玩家制定完成目标的分步指导。"
                    + "只提供说明、所需资源、风险和下一步建议，不要声称已经执行任何操作。"
                    + "目标标题: " + goal.title() + "；目标描述: " + goal.description()
                    + "；目标优先级: " + goal.priority() + "；目标资源: " + goal.targetItemId()
                    + "；目标数量: " + goal.targetCount();
            JarvisRuntime.applicationService().submit(com.promising.jarvis.core.context.CommandContext.from(
                    new CommandRequest(prompt), context.getSource()));
            return 1;
        } catch (IllegalArgumentException exception) {
            context.getSource().sendError(Text.of("Jarvis：目标 ID 无效或目标不存在。"));
            return 0;
        }
    }

    private static int setProactive(CommandContext<ServerCommandSource> context, boolean enabled) {
        var player = context.getSource().getPlayer();
        var current = JarvisRuntime.proactiveCompanion().policy().preferencesFor(player.getUuid());
        JarvisRuntime.proactiveCompanion().policy().setPreferences(player.getUuid(),
                new com.promising.jarvis.core.companion.NotificationPreferences(enabled, current.cooldown(),
                        current.maxNotificationsPerHour(), current.quietStart(), current.quietEnd()));
        context.getSource().sendFeedback(() -> Text.of("Jarvis：主动提醒已" + (enabled ? "开启" : "关闭") + "。"), false);
        return 1;
    }

    private static int proactiveStatus(CommandContext<ServerCommandSource> context) {
        var preferences = JarvisRuntime.proactiveCompanion().policy().preferencesFor(context.getSource().getPlayer().getUuid());
        context.getSource().sendFeedback(() -> Text.of("Jarvis：主动提醒 " + (preferences.proactiveEnabled() ? "已开启（/nl-proactive on 后生效）" : "未开启（默认关闭，请执行 /nl-proactive on）")
                + "，每小时上限 " + preferences.maxNotificationsPerHour() + " 条。"), false);
        return 1;
    }
}
