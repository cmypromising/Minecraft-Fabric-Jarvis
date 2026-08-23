package com.promising.jarvis.core.world;

import net.minecraft.server.command.ServerCommandSource;

/** Current difficulty and player game mode facts. */
public final class DifficultyComponent implements WorldInfoComponent {
    public String id() { return "world.difficulty"; }

    public boolean supports(String request) {
        return request.contains("难度") || request.contains("游戏模式") || request.contains("模式")
                || request.contains("difficulty") || request.contains("gamemode");
    }

    public String collect(ServerCommandSource source) {
        var player = source.getPlayer();
        if (player == null) return "";
        return String.format("[世界规则] 难度: %s，玩家游戏模式: %s",
                player.getWorld().getDifficulty().getName(), player.interactionManager.getGameMode().asString());
    }
}
