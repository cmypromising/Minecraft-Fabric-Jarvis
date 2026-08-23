package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.awareness.ActivityObservation;
import com.promising.jarvis.core.observation.PlayerStateSnapshot;

import java.time.Instant;
import java.util.Objects;

/** Immutable, versioned active-awareness snapshot shared with the proactive LLM. */
public record ActiveAwarenessContext(long version, Instant capturedAt, PlayerStateSnapshot player,
                                     ProactivePerception perception, ActivityObservation activity,
                                     String recentEvents) {
    public ActiveAwarenessContext {
        Objects.requireNonNull(capturedAt);
        Objects.requireNonNull(player);
        Objects.requireNonNull(perception);
        Objects.requireNonNull(activity);
        recentEvents = recentEvents == null ? "" : recentEvents;
    }

    public String promptText() {
        return "主动感知快照 v" + version + "（采集时间 " + capturedAt + "）\n"
                + "玩家：生命=" + player.health() + "，饥饿=" + player.foodLevel()
                + "，经验等级=" + player.experienceLevel() + "，坐标=(" + player.x() + "," + player.y() + "," + player.z() + ")"
                + "，维度=" + player.dimension() + "，模式=" + player.gameMode() + "\n"
                + "资源摘要：" + player.resources() + "\n"
                + "环境：生物群系=" + perception.biome() + "，夜晚=" + perception.night()
                + "，天气(雨/雷)=" + perception.raining() + "/" + perception.thundering()
                + "，光照=" + perception.lightLevel() + "，天空可见=" + perception.skyVisible()
                + "，水下=" + perception.underwater() + "，熔岩=" + perception.inLava()
                + "，着火=" + perception.onFire() + "，氧气=" + perception.air() + "/" + perception.maxAir()
                + "，附近敌对生物=" + perception.hostileNearby() + "，附近实体=" + perception.nearbyEntities()
                + "，阶段=" + perception.gamePhase() + "，危险=" + perception.dangers() + "\n"
                + "活动：" + activity.activity() + "，置信度=" + activity.confidence()
                + "，证据=" + String.join("、", activity.evidence()) + "\n"
                + recentEvents;
    }
}
