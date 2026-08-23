package com.promising.jarvis.core.memory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Process-local bounded memory. Replaceable with a persistent implementation later. */
public final class InMemoryMemoryStore implements MemoryStore {
    private final int maxTurns;
    private final ConcurrentMap<UUID, ConversationMemory> memories = new ConcurrentHashMap<>();

    public InMemoryMemoryStore(int maxTurns) { this.maxTurns = maxTurns; }

    private ConversationMemory memoryFor(UUID playerId) {
        return memories.computeIfAbsent(playerId, ignored -> new ConversationMemory(maxTurns));
    }

    public List<MemoryTurn> recent(UUID playerId) { return memoryFor(playerId).snapshot(); }
    public String promptFor(UUID playerId) { return memoryFor(playerId).asPromptText(); }
    public void append(UUID playerId, MemoryTurn turn) { memoryFor(playerId).append(turn); }
    public void clear(UUID playerId) { memories.remove(playerId); }
}
