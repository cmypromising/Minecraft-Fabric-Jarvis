package com.promising.jarvis.core.context;

import net.minecraft.server.command.ServerCommandSource;

/** Selective player status context; location and world rules are separate components. */
public final class PlayerStatusComponent implements ContextComponent {
    public String id() { return "player.status"; }

    public boolean supports(String request) {
        return request.contains("玩家") && (request.contains("状态") || request.contains("生命")
                || request.contains("血") || request.contains("饥饿") || request.contains("经验")
                || request.contains("等级") || request.contains("health") || request.contains("status"));
    }

    public String collect(ServerCommandSource source) {
        var player = source.getPlayer();
        if (player == null) return "";
        return String.format("[玩家状态] 姓名: %s，生命值: %.1f，饥饿值: %d，经验等级: %d",
                source.getName(), player.getHealth(), player.getHungerManager().getFoodLevel(), player.experienceLevel);
    }
}
