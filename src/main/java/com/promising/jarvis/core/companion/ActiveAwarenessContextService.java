package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.awareness.ActivityObservation;
import com.promising.jarvis.core.awareness.PlayerActivityTracker;
import com.promising.jarvis.core.awareness.PlayerEvent;
import com.promising.jarvis.core.awareness.PlayerEventListener;
import com.promising.jarvis.core.observation.MinecraftPlayerStateObserver;
import com.promising.jarvis.core.observation.PlayerStateSnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/** Server-thread sampler and thread-safe cache for active awareness. */
public final class ActiveAwarenessContextService implements PlayerEventListener {
    private static final int REFRESH_INTERVAL_TICKS = 300;
    private final MinecraftPlayerStateObserver observer;
    private final MinecraftProactivePerception perception;
    private final PlayerActivityTracker activities;
    private final Map<UUID, AtomicReference<ActiveAwarenessContext>> contexts = new ConcurrentHashMap<>();
    private final Map<UUID, ProactiveEventWindow> windows = new ConcurrentHashMap<>();
    private final AtomicLong version = new AtomicLong();
    private int ticks;

    public ActiveAwarenessContextService(MinecraftPlayerStateObserver observer,
                                         MinecraftProactivePerception perception,
                                         PlayerActivityTracker activities) {
        this.observer = observer;
        this.perception = perception;
        this.activities = activities;
    }

    /** Must run on the Minecraft server thread. */
    public void tick(MinecraftServer server) {
        if (server == null || ++ticks % REFRESH_INTERVAL_TICKS != 0) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) refresh(player);
        contexts.keySet().removeIf(id -> server.getPlayerManager().getPlayer(id) == null);
    }

    public ActiveAwarenessContext refresh(ServerPlayerEntity player) {
        PlayerStateSnapshot snapshot = observer.observe(player.getCommandSource()).orElse(null);
        if (snapshot == null) return null;
        UUID id = snapshot.playerId();
        ProactiveEventWindow window = windows.computeIfAbsent(id, ignored -> new ProactiveEventWindow());
        ActivityObservation activity = activities.activityFor(id);
        ActiveAwarenessContext context = new ActiveAwarenessContext(version.incrementAndGet(), Instant.now(), snapshot,
                perception.observe(player), activity, window.promptText(id));
        contexts.computeIfAbsent(id, ignored -> new AtomicReference<>()).set(context);
        return context;
    }

    public ActiveAwarenessContext current(UUID playerId) {
        AtomicReference<ActiveAwarenessContext> reference = contexts.get(playerId);
        return reference == null ? null : reference.get();
    }

    public ProactiveEventWindow window(UUID playerId) {
        return windows.computeIfAbsent(playerId, ignored -> new ProactiveEventWindow());
    }

    @Override
    public void onEvent(PlayerEvent event) {
        if (event == null) return;
        ProactiveEventWindow eventWindow = window(event.playerId());
        eventWindow.add(event);
        AtomicReference<ActiveAwarenessContext> reference = contexts.get(event.playerId());
        if (reference != null) {
            ActiveAwarenessContext previous = reference.get();
            if (previous != null) reference.set(new ActiveAwarenessContext(version.incrementAndGet(), Instant.now(),
                    previous.player(), previous.perception(), previous.activity(), eventWindow.promptText(event.playerId())));
        }
    }
}
