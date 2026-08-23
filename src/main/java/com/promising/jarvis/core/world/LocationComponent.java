package com.promising.jarvis.core.world;

import com.promising.jarvis.core.context.ContextComponent;
import net.minecraft.server.command.ServerCommandSource;

/** Current player location and dimension facts. */
public final class LocationComponent implements ContextComponent {
    public String id() { return "world.location"; }

    public boolean supports(String request) {
        return containsAny(request, "位置", "坐标", "哪里", "哪儿", "在哪", "当前位置", "维度",
                "dimension", "location", "where", "coordinate");
    }

    public String collect(ServerCommandSource source) {
        var player = source.getPlayer();
        if (player == null) return "";
        var pos = player.getBlockPos();
        return String.format("[世界位置] 维度: %s，方块坐标: (%d, %d, %d)",
                player.getWorld().getRegistryKey().getValue(), pos.getX(), pos.getY(), pos.getZ());
    }

    private static boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
