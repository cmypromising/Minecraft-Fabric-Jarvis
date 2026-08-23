package com.promising.jarvis.core.awareness;

import com.promising.jarvis.core.observation.PlayerStateObserver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-thread bridge: samples state, emits semantic events, and tracks activity. */
public final class PlayerAwarenessService {
    private static final int SAMPLE_INTERVAL_TICKS = 10;
    private final PlayerStateObserver observer;
    private final PlayerStateChangeDetector detector;
    private final PlayerEventBus events;
    private final PlayerActivityTracker activities;
    private final Map<UUID, com.promising.jarvis.core.observation.PlayerStateSnapshot> previous = new ConcurrentHashMap<>();
    private long ticks;

    public PlayerAwarenessService(PlayerStateObserver observer, PlayerStateChangeDetector detector,
                                  PlayerEventBus events, PlayerActivityTracker activities) {
        this.observer = observer;
        this.detector = detector;
        this.events = events;
        this.activities = activities;
        events.subscribe(activities);
    }

    public void tick(MinecraftServer server) {
        if (server == null || ++ticks % SAMPLE_INTERVAL_TICKS != 0) return;
        java.util.Set<UUID> online = new java.util.HashSet<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            observer.observe(player.getCommandSource()).ifPresent(current -> {
                online.add(current.playerId());
                previous.computeIfAbsent(current.playerId(), ignored -> {
                    detector.detect(null, current).forEach(events::publish);
                    return current;
                });
                var old = previous.put(current.playerId(), current);
                detector.detect(old, current).forEach(events::publish);
            });
        }
        previous.keySet().removeIf(playerId -> {
            if (online.contains(playerId)) return false;
            events.publish(new PlayerEvent(playerId, PlayerEventType.LEFT, previous.get(playerId), null,
                    java.util.List.of("玩家不再在线"), java.time.Instant.now()));
            activities.forget(playerId);
            return true;
        });
    }

    public ActivityObservation activityFor(UUID playerId) { return activities.activityFor(playerId); }
    public PlayerEventBus events() { return events; }
}
