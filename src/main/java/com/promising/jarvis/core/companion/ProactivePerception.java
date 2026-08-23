package com.promising.jarvis.core.companion;

import java.util.List;

/** Bounded world facts used by proactive rules; it never contains raw entities or NBT. */
public record ProactivePerception(String biome, String dimension, boolean night,
                                  boolean hostileNearby, boolean danger, String gamePhase,
                                  List<String> evidence, List<DangerSignal> dangers) {
    public ProactivePerception(String biome, String dimension, boolean night, boolean hostileNearby,
                               boolean danger, String gamePhase, List<String> evidence) {
        this(biome, dimension, night, hostileNearby, danger, gamePhase, evidence, List.of());
    }
    public ProactivePerception {
        evidence = List.copyOf(evidence);
        dangers = List.copyOf(dangers);
    }
}
