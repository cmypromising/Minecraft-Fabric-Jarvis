package com.promising.jarvis.core.context;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

import java.util.UUID;

/** Immutable player and world snapshot used by command safety policies. */
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
}
