package com.promising.jarvis.core.awareness;

import com.promising.jarvis.core.observation.PlayerStateObserver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Box;

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
    private final java.util.Set<UUID> nightPlayers = ConcurrentHashMap.newKeySet();
    private final java.util.Set<UUID> hostilePlayers = ConcurrentHashMap.newKeySet();
    private final java.util.Set<UUID> dangerPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, java.util.Set<String>> completedAdvancements = new ConcurrentHashMap<>();

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
                boolean firstObservation = !previous.containsKey(current.playerId());
                previous.computeIfAbsent(current.playerId(), ignored -> {
                    detector.detect(null, current).forEach(events::publish);
                    return current;
                });
                var old = previous.put(current.playerId(), current);
                detector.detect(old, current).forEach(events::publish);
                if (firstObservation) publish(current.playerId(), PlayerEventType.FIRST_JOIN, current, "首次观察到玩家进入世界");
                detectWorldEvents(player, current);
                detectAdvancements(server, player, current, firstObservation);
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

    private void detectWorldEvents(ServerPlayerEntity player, com.promising.jarvis.core.observation.PlayerStateSnapshot current) {
        UUID id = current.playerId();
        boolean night = isNight(player.getWorld().getTimeOfDay());
        boolean hostile = !player.getWorld().getEntitiesByClass(HostileEntity.class,
                new Box(player.getBlockPos()).expand(16), entity -> entity.isAlive()).isEmpty();
        boolean danger = current.health() <= 4 || current.foodLevel() <= 6
                || player.isSubmergedInWater() || player.isInLava() || player.isOnFire() || hostile;
        if (night && nightPlayers.add(id)) publish(id, PlayerEventType.NIGHTFALL, current, "当前已进入夜晚");
        if (!night) nightPlayers.remove(id);
        if (hostile && hostilePlayers.add(id)) publish(id, PlayerEventType.HOSTILE_NEARBY, current, "16格内发现敌对生物");
        if (!hostile) hostilePlayers.remove(id);
        if (danger && dangerPlayers.add(id)) publish(id, PlayerEventType.DANGER_DETECTED, current, "检测到低生命/饥饿、火焰、熔岩或附近敌对生物");
        if (!danger) dangerPlayers.remove(id);
    }

    private void publish(UUID id, PlayerEventType type, com.promising.jarvis.core.observation.PlayerStateSnapshot current, String evidence) {
        events.publish(new PlayerEvent(id, type, previous.get(id), current, java.util.List.of(evidence), java.time.Instant.now()));
    }

    private static boolean isNight(long timeOfDay) {
        long time = timeOfDay % 24000;
        return time >= 13000 && time < 23000;
    }

    private void detectAdvancements(MinecraftServer server, ServerPlayerEntity player,
                                    com.promising.jarvis.core.observation.PlayerStateSnapshot current, boolean firstObservation) {
        var known = completedAdvancements.computeIfAbsent(current.playerId(), ignored -> ConcurrentHashMap.newKeySet());
        for (var advancement : server.getAdvancementLoader().getAdvancements()) {
            if (!player.getAdvancementTracker().getProgress(advancement).isDone()) continue;
            String id = advancement.id().toString();
            if (known.add(id) && !firstObservation) {
                publish(current.playerId(), PlayerEventType.ADVANCEMENT_COMPLETED, current, "完成进度: " + id);
            }
        }
    }

    public ActivityObservation activityFor(UUID playerId) { return activities.activityFor(playerId); }
    public PlayerEventBus events() { return events; }
}
