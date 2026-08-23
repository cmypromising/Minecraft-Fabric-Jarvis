package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.awareness.PlayerEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Small per-player temporal window; it bounds what is sent to the proactive LLM. */
public final class ProactiveEventWindow {
    private static final int MAX_EVENTS = 12;
    private static final Duration RETENTION = Duration.ofSeconds(45);
    private final Deque<PlayerEvent> events = new ArrayDeque<>();

    public synchronized void add(PlayerEvent event) {
        events.addLast(event);
        trim(event.occurredAt());
    }

    public synchronized String promptText(UUID playerId) {
        Instant now = Instant.now();
        trim(now);
        if (events.isEmpty()) return "玩家 " + playerId + " 暂无可用事件。";
        return events.stream().map(event -> "- " + event.occurredAt() + " | " + event.type()
                + " | " + String.join("、", event.evidence())).collect(Collectors.joining("\n",
                "玩家 " + playerId + " 最近45秒事件:\n", ""));
    }

    public synchronized List<PlayerEvent> snapshot() { return List.copyOf(events); }

    private void trim(Instant now) {
        while (events.size() > MAX_EVENTS || (!events.isEmpty()
                && Duration.between(events.peekFirst().occurredAt(), now).compareTo(RETENTION) > 0)) events.removeFirst();
    }
}
