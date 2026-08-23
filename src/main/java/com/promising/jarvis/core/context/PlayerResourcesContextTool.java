package com.promising.jarvis.core.context;

import com.promising.jarvis.core.observation.MinecraftPlayerStateObserver;
import com.promising.jarvis.core.observation.PlayerStateSnapshot;
import net.minecraft.server.command.ServerCommandSource;

/** Read-only context tool exposing aggregated inventory resources on demand. */
public final class PlayerResourcesContextTool implements ContextTool {
    private static final int MAX_OUTPUT_LENGTH = 3000;
    private final MinecraftPlayerStateObserver observer = new MinecraftPlayerStateObserver();

    public String name() { return "player.resources"; }
    public String description() { return "当前玩家背包中各类物品的聚合数量"; }

    public String execute(ServerCommandSource source, String arguments) {
        return observer.observe(source)
                .map(PlayerResourcesContextTool::format)
                .orElse("无法获取当前玩家资源：玩家不在线或上下文不可用。");
    }

    private static String format(PlayerStateSnapshot snapshot) {
        if (snapshot.resources().isEmpty()) return "[玩家资源] 背包为空";
        StringBuilder result = new StringBuilder("[玩家资源]");
        for (var entry : snapshot.resources().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).toList()) {
            String line = "\n" + entry.getKey() + ": " + entry.getValue();
            if (result.length() + line.length() > MAX_OUTPUT_LENGTH) break;
            result.append(line);
        }
        return result.toString();
    }
}
