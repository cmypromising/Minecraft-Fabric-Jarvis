package com.promising.jarvis.core.companion;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Evidence-backed suggestion; it is not an executable command. */
public record Recommendation(
        UUID id,
        UUID playerId,
        UUID goalId,
        RecommendationPriority priority,
        String title,
        String message,
        List<String> evidence,
        Instant createdAt) {
    public Recommendation {
        evidence = List.copyOf(evidence);
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("message is required");
    }
}
