package com.promising.jarvis.core.awareness;

import com.promising.jarvis.core.observation.PlayerStateSnapshot;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Maintains the latest activity observation per player from the event stream. */
public final class PlayerActivityTracker implements PlayerEventListener {
    private static final Duration IDLE_AFTER = Duration.ofMinutes(2);
    private final Clock clock;
    private final Map<UUID, ActivityObservation> activities = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> lastMeaningfulEvent = new ConcurrentHashMap<>();

    public PlayerActivityTracker() { this(Clock.systemUTC()); }
    public PlayerActivityTracker(Clock clock) { this.clock = clock; }

    @Override
    public void onEvent(PlayerEvent event) {
        Instant now = event.occurredAt();
        PlayerActivity activity = classify(event);
        if (activity != PlayerActivity.IDLE && activity != PlayerActivity.UNKNOWN) {
            lastMeaningfulEvent.put(event.playerId(), now);
        }
        ActivityObservation candidate = new ActivityObservation(event.playerId(), activity,
                confidence(event, activity), event.evidence(), now);
        activities.compute(event.playerId(), (ignored, current) -> shouldReplace(current, candidate) ? candidate : current);
    }

    public ActivityObservation activityFor(UUID playerId) {
        ActivityObservation current = activities.get(playerId);
        Instant last = lastMeaningfulEvent.get(playerId);
        if (current != null && last != null && Duration.between(last, Instant.now(clock)).compareTo(IDLE_AFTER) >= 0) {
            return new ActivityObservation(playerId, PlayerActivity.IDLE, 0.75,
                    java.util.List.of("超过 2 分钟没有检测到明显活动"), Instant.now(clock));
        }
        return current == null ? new ActivityObservation(playerId, PlayerActivity.UNKNOWN, 0,
                java.util.List.of("尚无足够事件"), Instant.now(clock)) : current;
    }

    public void forget(UUID playerId) {
        activities.remove(playerId);
        lastMeaningfulEvent.remove(playerId);
    }

    private static PlayerActivity classify(PlayerEvent event) {
        return switch (event.type()) {
            case DIMENSION_CHANGED -> PlayerActivity.EXPLORING;
            case POSITION_CHANGED -> PlayerActivity.TRAVELING;
            case RESOURCE_CHANGED -> resourceActivity(event);
            case HEALTH_CHANGED, DEATH, RESPAWN -> PlayerActivity.COMBAT;
            case FOOD_CHANGED -> PlayerActivity.INVENTORY_MANAGEMENT;
            case JOINED, LEFT -> PlayerActivity.UNKNOWN;
            case STATE_CHANGED, ADVANCEMENT_COMPLETED, FIRST_JOIN, NIGHTFALL,
                    HOSTILE_NEARBY, DANGER_DETECTED -> PlayerActivity.UNKNOWN;
        };
    }

    private static PlayerActivity resourceActivity(PlayerEvent event) {
        if (event.previous() == null || event.current() == null) return PlayerActivity.INVENTORY_MANAGEMENT;
        java.util.Set<String> itemIds = new java.util.HashSet<>(event.previous().resources().keySet());
        itemIds.addAll(event.current().resources().keySet());
        int netChange = itemIds.stream().mapToInt(itemId ->
                event.current().resourceCount(itemId) - event.previous().resourceCount(itemId)).sum();
        if (netChange > 0) return PlayerActivity.MINING;
        if (netChange < 0) return PlayerActivity.BUILDING;
        return PlayerActivity.INVENTORY_MANAGEMENT;
    }

    private static double confidence(PlayerEvent event, PlayerActivity activity) {
        return switch (event.type()) {
            case DIMENSION_CHANGED, DEATH, RESPAWN -> 0.95;
            case HEALTH_CHANGED -> healthDecreased(event) ? 0.9 : 0.65;
            case RESOURCE_CHANGED -> activity == PlayerActivity.INVENTORY_MANAGEMENT ? 0.6 : 0.8;
            case POSITION_CHANGED -> movementDistance(event) >= 16 ? 0.8 : 0.55;
            case FOOD_CHANGED -> 0.7;
            default -> 0.4;
        };
    }

    private static boolean shouldReplace(ActivityObservation current, ActivityObservation candidate) {
        if (current == null) return true;
        Duration gap = Duration.between(current.observedAt(), candidate.observedAt()).abs();
        return gap.compareTo(Duration.ofSeconds(1)) > 0 || candidate.confidence() >= current.confidence();
    }

    private static boolean healthDecreased(PlayerEvent event) {
        return event.previous() != null && event.current() != null
                && event.current().health() < event.previous().health();
    }

    private static double movementDistance(PlayerEvent event) {
        if (event.previous() == null || event.current() == null) return 0;
        long dx = event.current().x() - event.previous().x();
        long dy = event.current().y() - event.previous().y();
        long dz = event.current().z() - event.previous().z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
