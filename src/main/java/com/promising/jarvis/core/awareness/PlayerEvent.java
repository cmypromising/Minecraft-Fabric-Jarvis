package com.promising.jarvis.core.awareness;

import com.promising.jarvis.core.observation.PlayerStateSnapshot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Immutable event emitted when observable player state or lifecycle changes. */
public record PlayerEvent(UUID playerId, PlayerEventType type, PlayerStateSnapshot previous,
                          PlayerStateSnapshot current, List<String> evidence, Instant occurredAt) {
    public PlayerEvent {
        evidence = List.copyOf(evidence);
    }
}
