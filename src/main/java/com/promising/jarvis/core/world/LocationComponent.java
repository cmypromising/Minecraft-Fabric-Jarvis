package com.promising.jarvis.core.world;

import net.minecraft.server.command.ServerCommandSource;

/** Current player location and dimension facts. */
public final class LocationComponent implements WorldInfoComponent {
    public String id() { return "world.location"; }

    public boolean supports(String request) {
        return request.contains("位置") || request.contains("坐标") || request.contains("哪里")
                || request.contains("维度") || request.contains("dimension") || request.contains("location");
    }

    public String collect(ServerCommandSource source) {
        var player = source.getPlayer();
        if (player == null) return "";
        var pos = player.getBlockPos();
        return String.format("[世界位置] 维度: %s，方块坐标: (%d, %d, %d)",
                player.getWorld().getRegistryKey().getValue(), pos.getX(), pos.getY(), pos.getZ());
    }
}
