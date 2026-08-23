package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.awareness.PlayerEvent;
import com.promising.jarvis.core.awareness.PlayerEventListener;
import com.promising.jarvis.core.awareness.PlayerEventType;
import com.promising.jarvis.core.observation.PlayerStateObserver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProactiveCompanionService implements PlayerEventListener, AutoCloseable {
    private static final int SAMPLE_INTERVAL_TICKS = 100;
    private final GoalStore goals; private final PlayerStateObserver observer; private final RecommendationEngine engine; private final NotificationPolicy policy;
    private final MinecraftProactivePerception perception = new MinecraftProactivePerception();
    private final ProactiveLlmAgent proactiveAgent;
    private final Map<UUID, ProactiveEventWindow> windows = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> inFlight = new ConcurrentHashMap<>();
    private MinecraftServer server; private long ticks;
    public ProactiveCompanionService(GoalStore goals, PlayerStateObserver observer, RecommendationEngine engine, NotificationPolicy policy) {
        this(goals, observer, engine, policy, null);
    }
    public ProactiveCompanionService(GoalStore goals, PlayerStateObserver observer, RecommendationEngine engine, NotificationPolicy policy, ProactiveLlmAgent proactiveAgent) {
        this.goals = goals; this.observer = observer; this.engine = engine; this.policy = policy; this.proactiveAgent = proactiveAgent;
    }
    public void tick(MinecraftServer server) {
        if (server == null) return; this.server = server;
        if (++ticks % SAMPLE_INTERVAL_TICKS != 0 || proactiveAgent != null) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) observer.observe(player.getCommandSource()).ifPresent(snapshot ->
                engine.evaluate(snapshot, goals.findByPlayer(snapshot.playerId()), perception.observe(player)).stream()
                        .sorted(java.util.Comparator.comparing(Recommendation::priority).reversed()).filter(r -> policy.decide(r).allowed()).limit(1)
                        .forEach(r -> player.sendMessage(Text.of("Jarvis: " + r.message()), false)));
    }
    @Override public void onEvent(PlayerEvent event) {
        if (server == null || proactiveAgent == null || event == null || event.current() == null || !isTriggerEvent(event.type())) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(event.playerId()); if (player == null) return;
        UUID id = event.playerId(); windows.computeIfAbsent(id, ignored -> new ProactiveEventWindow()).add(event);
        if (!policy.preferencesFor(id).proactiveEnabled() || inFlight.putIfAbsent(id, true) != null) return;
        proactiveAgent.submit(player.getCommandSource(), id, windows.get(id).promptText(id), perception.observe(player)).whenComplete((response, error) -> server.execute(() -> {
            inFlight.remove(id);
            if (error != null || response == null || response.getAdditionalInfo() == null || !"minecraft.information".equals(response.getCapability())) return;
            Recommendation recommendation = new Recommendation(UUID.randomUUID(), id, null, RecommendationPriority.NORMAL, "主动陪伴建议", response.getAdditionalInfo(), event.evidence(), Instant.now());
            if (policy.decide(recommendation).allowed()) player.sendMessage(Text.of("Jarvis 提醒：" + recommendation.message()), false);
        }));
    }
    private static boolean isTriggerEvent(PlayerEventType type) { return switch (type) {
        case FIRST_JOIN, JOINED, ADVANCEMENT_COMPLETED, NIGHTFALL, HOSTILE_NEARBY, DANGER_DETECTED, DIMENSION_CHANGED, DEATH, RESPAWN -> true;
        default -> false; };
    }
    public NotificationPolicy policy() { return policy; }
    @Override public void close() { if (proactiveAgent != null) proactiveAgent.close(); }
}
