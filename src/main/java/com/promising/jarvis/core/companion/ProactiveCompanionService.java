package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.awareness.PlayerEvent;
import com.promising.jarvis.core.awareness.PlayerEventListener;
import com.promising.jarvis.core.awareness.PlayerEventType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Routes events to the proactive LLM; awareness sampling and caching are separate. */
public final class ProactiveCompanionService implements PlayerEventListener, AutoCloseable {
    private final NotificationPolicy policy;
    private final ActiveAwarenessContextService awareness;
    private final ProactiveLlmAgent proactiveAgent;
    private final Map<UUID, Boolean> inFlight = new ConcurrentHashMap<>();
    private final Map<UUID, Long> submittedVersions = new ConcurrentHashMap<>();
    private MinecraftServer server;

    public ProactiveCompanionService(NotificationPolicy policy, ProactiveLlmAgent proactiveAgent,
                                     ActiveAwarenessContextService awareness) {
        this.policy = policy; this.proactiveAgent = proactiveAgent; this.awareness = awareness;
    }
    public void tick(MinecraftServer server) { if (server != null) this.server = server; }

    @Override public void onEvent(PlayerEvent event) {
        if (server == null || proactiveAgent == null || event == null || event.current() == null || !isTriggerEvent(event.type())) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(event.playerId());
        if (player == null) return;
        UUID id = event.playerId();
        ActiveAwarenessContext context = awareness.current(id);
        if (context == null) context = awareness.refresh(player);
        if (context == null || !policy.preferencesFor(id).proactiveEnabled() || inFlight.putIfAbsent(id, true) != null) return;
        Long previousVersion = submittedVersions.put(id, context.version());
        if (previousVersion != null && previousVersion.longValue() == context.version()) {
            inFlight.remove(id);
            return;
        }
        ActiveAwarenessContext submitted = context;
        proactiveAgent.submit(player.getCommandSource(), id, submitted).whenComplete((response, error) -> server.execute(() -> {
            inFlight.remove(id);
            if (error != null || response == null || response.getAdditionalInfo() == null
                    || !"minecraft.information".equals(response.getCapability()) || Integer.valueOf(3).equals(response.getType())) return;
            Recommendation recommendation = new Recommendation(UUID.randomUUID(), id, null, RecommendationPriority.NORMAL,
                    "主动陪伴建议", response.getAdditionalInfo(), event.evidence(), Instant.now());
            if (policy.decide(recommendation).allowed()) player.sendMessage(Text.of("Jarvis 提醒：" + recommendation.message()), false);
        }));
    }
    private static boolean isTriggerEvent(PlayerEventType type) { return switch (type) {
        case FIRST_JOIN, JOINED, ADVANCEMENT_COMPLETED, NIGHTFALL, HOSTILE_NEARBY, DANGER_DETECTED, DIMENSION_CHANGED, DEATH, RESPAWN -> true;
        default -> false; };
    }
    public NotificationPolicy policy() { return policy; }
    @Override public void close() { proactiveAgent.close(); }
}
