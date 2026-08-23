package com.promising.jarvis.core.memory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Bounded short-term memory for one player's conversation. */
public final class ConversationMemory {
    private final int maxTurns;
    private final Deque<MemoryTurn> turns = new ArrayDeque<>();

    public ConversationMemory(int maxTurns) {
        if (maxTurns < 1) throw new IllegalArgumentException("maxTurns must be positive");
        this.maxTurns = maxTurns;
    }

    public synchronized void append(MemoryTurn turn) {
        turns.addLast(turn);
        while (turns.size() > maxTurns) turns.removeFirst();
    }

    public synchronized List<MemoryTurn> snapshot() { return List.copyOf(turns); }

    public synchronized String asPromptText() {
        if (turns.isEmpty()) return "";
        StringBuilder builder = new StringBuilder("\n以下是最近的对话记忆，请仅在相关时使用：\n");
        int index = 1;
        for (MemoryTurn turn : turns) {
            builder.append(index++).append(". 玩家: ").append(turn.userMessage())
                    .append("\n   Jarvis: ").append(turn.assistantMessage()).append('\n');
        }
        return builder.toString();
    }

    public synchronized void clear() { turns.clear(); }
}
