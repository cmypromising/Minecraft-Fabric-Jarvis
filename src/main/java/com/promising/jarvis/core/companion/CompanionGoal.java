package com.promising.jarvis.core.companion;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Immutable player goal tracked by the companion; it contains no execution command. */
public record CompanionGoal(
        UUID id,
        UUID playerId,
        String title,
        String description,
        GoalPriority priority,
        GoalStatus status,
        String targetItemId,
        int targetCount,
        boolean guidanceEnabled,
        Instant createdAt,
        Instant updatedAt) {

    public CompanionGoal {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(playerId, "playerId");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        if (description == null) description = "";
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(status, "status");
        if (targetCount < 0) throw new IllegalArgumentException("targetCount cannot be negative");
        if (targetCount > 0 && (targetItemId == null || targetItemId.isBlank())) {
            throw new IllegalArgumentException("targetItemId is required when targetCount is positive");
        }
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public CompanionGoal withStatus(GoalStatus nextStatus, Instant now) {
        return new CompanionGoal(id, playerId, title, description, priority, nextStatus,
                targetItemId, targetCount, guidanceEnabled, createdAt, now);
    }

    public CompanionGoal withGuidanceEnabled(boolean enabled, Instant now) {
        return new CompanionGoal(id, playerId, title, description, priority, status,
                targetItemId, targetCount, enabled, createdAt, now);
    }
}
