package com.promising.jarvis.core.memory;

import java.util.List;
import java.util.UUID;

/** Storage abstraction for player conversation memory. */
public interface MemoryStore {
    List<MemoryTurn> recent(UUID playerId);
    int size(UUID playerId);
    String promptFor(UUID playerId);
    void append(UUID playerId, MemoryTurn turn);
    void clear(UUID playerId);
}
