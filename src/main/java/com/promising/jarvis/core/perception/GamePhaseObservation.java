package com.promising.jarvis.core.perception;

import java.util.List;

public record GamePhaseObservation(GamePhase phase, double confidence, List<String> evidence) {
    public GamePhaseObservation {
        confidence = Math.max(0, Math.min(1, confidence));
        evidence = List.copyOf(evidence);
    }
}
