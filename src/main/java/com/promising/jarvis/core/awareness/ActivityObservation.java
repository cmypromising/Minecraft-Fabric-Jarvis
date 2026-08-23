package com.promising.jarvis.core.awareness;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Current inferred activity with transparent evidence and confidence. */
public record ActivityObservation(UUID playerId, PlayerActivity activity, double confidence,
                                  List<String> evidence, Instant observedAt) {
    public ActivityObservation {
        if (confidence < 0 || confidence > 1) throw new IllegalArgumentException("confidence must be between 0 and 1");
        evidence = List.copyOf(evidence);
    }
}
