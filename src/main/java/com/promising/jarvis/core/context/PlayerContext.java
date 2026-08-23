package com.promising.jarvis.core.context;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

import java.util.UUID;

/** Immutable player and world snapshot shared with services and policies. */
public record PlayerContext(UUID playerId, String playerName, int x, int y, int z, float health, int foodLevel,
                            int experienceLevel, GameMode gameMode, Difficulty difficulty) {
    public static PlayerContext from(ServerCommandSource source) {
        if (source.getPlayer() == null) throw new IllegalArgumentException("A player context is required");
        var player = source.getPlayer();
        var position = player.getBlockPos();
        return new PlayerContext(player.getUuid(), source.getName(), position.getX(), position.getY(), position.getZ(),
                player.getHealth(), player.getHungerManager().getFoodLevel(), player.experienceLevel,
                player.interactionManager.getGameMode(), player.getWorld().getDifficulty());
    }

    public String asPromptText() {
        return String.format("\n姓名: %s\n坐标: (%d, %d, %d)\n生命值: %.1f\n饥饿值: %d\n经验等级: %d\n游戏模式: %s\n世界难度: %s",
                playerName, x, y, z, health, foodLevel, experienceLevel, gameMode.asString(), difficulty.getName());
    }
}
