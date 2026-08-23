package com.promising.jarvis.core.companion;

import java.util.List;

/** Bounded world facts used by proactive rules; it never contains raw entities or NBT. */
public record ProactivePerception(String biome, String dimension, boolean night,
                                  boolean hostileNearby, boolean danger, String gamePhase,
                                  List<String> evidence) {
    public ProactivePerception { evidence = List.copyOf(evidence); }
}
