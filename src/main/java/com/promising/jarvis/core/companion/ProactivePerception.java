package com.promising.jarvis.core.companion;

import java.util.List;

/** Bounded world facts used by proactive rules; it never contains raw entities or NBT. */
public record ProactivePerception(String biome, String dimension, boolean night,
                                  boolean hostileNearby, boolean danger, String gamePhase,
                                  List<String> evidence, List<DangerSignal> dangers,
                                  boolean raining, boolean thundering, int lightLevel,
                                  boolean skyVisible, boolean underwater, boolean inLava,
                                  boolean onFire, int air, int maxAir, String nearbyEntities) {
    public ProactivePerception(String biome, String dimension, boolean night, boolean hostileNearby,
                               boolean danger, String gamePhase, List<String> evidence) {
        this(biome, dimension, night, hostileNearby, danger, gamePhase, evidence, List.of(),
                false, false, 0, false, false, false, false, 0, 0, "");
    }
    public ProactivePerception(String biome, String dimension, boolean night, boolean hostileNearby,
                               boolean danger, String gamePhase, List<String> evidence, List<DangerSignal> dangers) {
        this(biome, dimension, night, hostileNearby, danger, gamePhase, evidence, dangers,
                false, false, 0, false, false, false, false, 0, 0, "");
    }
    public ProactivePerception {
        evidence = List.copyOf(evidence);
        dangers = List.copyOf(dangers);
    }
}
