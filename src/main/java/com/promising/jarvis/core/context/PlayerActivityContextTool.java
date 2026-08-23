package com.promising.jarvis.core.context;

import com.promising.jarvis.core.awareness.ActivityObservation;
import com.promising.jarvis.core.awareness.PlayerActivityTracker;
import net.minecraft.server.command.ServerCommandSource;

/** Read-only context tool exposing the locally inferred player activity. */
public final class PlayerActivityContextTool implements ContextTool {
    private final PlayerActivityTracker tracker;

    public PlayerActivityContextTool(PlayerActivityTracker tracker) { this.tracker = tracker; }
    public String name() { return "player.activity"; }
    public String description() { return "根据最近事件推断的玩家当前活动、置信度和证据"; }

    public String execute(ServerCommandSource source, String arguments) {
        if (source == null || source.getPlayer() == null) return "无法获取玩家活动：玩家不在线。";
        ActivityObservation observation = tracker.activityFor(source.getPlayer().getUuid());
        return "[玩家活动] " + observation.activity() + "，置信度: " + observation.confidence()
                + "，证据: " + String.join("；", observation.evidence());
    }
}
