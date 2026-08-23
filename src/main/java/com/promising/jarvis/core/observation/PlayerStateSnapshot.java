package com.promising.jarvis.core.observation;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

import java.util.Map;
import java.util.UUID;

/** Immutable, bounded observation that can safely cross from Minecraft to an Agent thread. */
public record PlayerStateSnapshot(
        UUID playerId,
        String playerName,
        float health,
        int foodLevel,
        int experienceLevel,
        int x,
        int y,
        int z,
        String dimension,
        Difficulty difficulty,
        GameMode gameMode,
        Map<String, Integer> resources) {

    public PlayerStateSnapshot {
        resources = Map.copyOf(resources);
    }

    public int resourceCount(String itemId) {
        return resources.getOrDefault(itemId, 0);
    }
}
