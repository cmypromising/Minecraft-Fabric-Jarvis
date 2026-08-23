package com.promising.jarvis.core.awareness;

import com.promising.jarvis.core.observation.PlayerStateSnapshot;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Converts periodic snapshots into semantic events, suppressing unchanged state. */
public final class PlayerStateChangeDetector {
    private final Clock clock;

    public PlayerStateChangeDetector() { this(Clock.systemUTC()); }
    public PlayerStateChangeDetector(Clock clock) { this.clock = clock; }

    public List<PlayerEvent> detect(PlayerStateSnapshot previous, PlayerStateSnapshot current) {
        if (current == null) return List.of();
        if (previous == null) return List.of(event(PlayerEventType.JOINED, null, current, "首次观察到玩家"));
        List<PlayerEvent> events = new ArrayList<>();
        if (!previous.dimension().equals(current.dimension())) {
            events.add(event(PlayerEventType.DIMENSION_CHANGED, previous, current,
                    previous.dimension() + " -> " + current.dimension()));
        }
        if (previous.x() != current.x() || previous.y() != current.y() || previous.z() != current.z()) {
            events.add(event(PlayerEventType.POSITION_CHANGED, previous, current,
                    "位置: (" + previous.x() + "," + previous.y() + "," + previous.z() + ") -> ("
                            + current.x() + "," + current.y() + "," + current.z() + ")"));
        }
        if (previous.health() > 0 && current.health() <= 0) {
            events.add(event(PlayerEventType.DEATH, previous, current, "玩家生命值降至 0"));
        } else if (previous.health() <= 0 && current.health() > 0) {
            events.add(event(PlayerEventType.RESPAWN, previous, current, "玩家恢复生命并重新出现"));
        } else if (previous.health() != current.health()) {
            events.add(event(PlayerEventType.HEALTH_CHANGED, previous, current,
                    "生命值: " + previous.health() + " -> " + current.health()));
        }
        if (previous.foodLevel() != current.foodLevel()) events.add(event(PlayerEventType.FOOD_CHANGED, previous, current,
                "饥饿值: " + previous.foodLevel() + " -> " + current.foodLevel()));
        if (!previous.resources().equals(current.resources())) events.add(event(PlayerEventType.RESOURCE_CHANGED, previous, current,
                "背包资源发生变化"));
        if (!previous.gameMode().equals(current.gameMode()) || !previous.difficulty().equals(current.difficulty())) {
            events.add(event(PlayerEventType.STATE_CHANGED, previous, current, "游戏规则或模式发生变化"));
        }
        return List.copyOf(events);
    }

    private PlayerEvent event(PlayerEventType type, PlayerStateSnapshot previous,
                              PlayerStateSnapshot current, String evidence) {
        return new PlayerEvent(current.playerId(), type, previous, current, List.of(evidence), Instant.now(clock));
    }
}
