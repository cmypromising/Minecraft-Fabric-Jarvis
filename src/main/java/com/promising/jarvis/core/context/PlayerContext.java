package com.promising.jarvis.core.context;

import net.minecraft.server.command.ServerCommandSource;

/** Immutable player snapshot shared with services and language models. */
public record PlayerContext(String playerName, int x, int y, int z, float health, int foodLevel, int experienceLevel) {
    public static PlayerContext from(ServerCommandSource source) {
        if (source.getPlayer() == null) throw new IllegalArgumentException("A player context is required");
        var player = source.getPlayer();
        var position = player.getBlockPos();
        return new PlayerContext(source.getName(), position.getX(), position.getY(), position.getZ(),
                player.getHealth(), player.getHungerManager().getFoodLevel(), player.experienceLevel);
    }

    public String asPromptText() {
        return String.format("\n姓名: %s\n坐标: (%d, %d, %d)\n生命值: %.1f\n饥饿值: %d\n经验等级: %d",
                playerName, x, y, z, health, foodLevel, experienceLevel);
    }
}
