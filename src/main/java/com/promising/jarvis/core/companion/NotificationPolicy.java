package com.promising.jarvis.core.companion;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Pure policy decision boundary for proactive notifications. */
public final class NotificationPolicy {
    private final Clock clock;
    private final Map<UUID, NotificationPreferences> preferences = new HashMap<>();
    private final Map<NotificationKey, Instant> lastSent = new HashMap<>();
    private final Map<UUID, Integer> hourlyCount = new HashMap<>();
    private final Map<UUID, String> hourlyBucket = new HashMap<>();

    public NotificationPolicy() { this(Clock.systemUTC()); }
    public NotificationPolicy(Clock clock) { this.clock = clock; }

    public synchronized void setPreferences(UUID playerId, NotificationPreferences value) {
        preferences.put(playerId, value);
    }

    public synchronized NotificationPreferences preferencesFor(UUID playerId) {
        return preferences.getOrDefault(playerId, NotificationPreferences.defaults());
    }

    public synchronized NotificationDecision decide(Recommendation recommendation) {
        UUID playerId = recommendation.playerId();
        NotificationPreferences policy = preferencesFor(playerId);
        if (!policy.proactiveEnabled()) return NotificationDecision.deny("proactive notifications disabled");
        LocalDateTime now = LocalDateTime.now(clock);
        if (policy.isQuiet(now.toLocalTime())) return NotificationDecision.deny("quiet hours");

        String bucket = now.toLocalDate() + "T" + now.getHour();
        if (!bucket.equals(hourlyBucket.get(playerId))) {
            hourlyBucket.put(playerId, bucket);
            hourlyCount.put(playerId, 0);
        }
        if (hourlyCount.getOrDefault(playerId, 0) >= policy.maxNotificationsPerHour()) {
            return NotificationDecision.deny("hourly notification limit reached");
        }
        NotificationKey key = new NotificationKey(playerId, recommendation.goalId(), recommendation.title());
        Instant previous = lastSent.get(key);
        if (previous != null && previous.plus(policy.cooldown()).isAfter(Instant.now(clock))) {
            return NotificationDecision.deny("recommendation cooldown");
        }
        lastSent.put(key, Instant.now(clock));
        hourlyCount.merge(playerId, 1, Integer::sum);
        return NotificationDecision.allow();
    }

    private record NotificationKey(UUID playerId, UUID goalId, String title) { }
}
