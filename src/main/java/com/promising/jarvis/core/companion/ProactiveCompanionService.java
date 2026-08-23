package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.observation.PlayerStateObserver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/** Low-frequency server-thread coordinator for safe proactive reminders. */
public final class ProactiveCompanionService {
    private static final int SAMPLE_INTERVAL_TICKS = 100;
    private final GoalStore goals;
    private final PlayerStateObserver observer;
    private final RecommendationEngine engine;
    private final NotificationPolicy policy;
    private long ticks;

    public ProactiveCompanionService(GoalStore goals, PlayerStateObserver observer,
                                     RecommendationEngine engine, NotificationPolicy policy) {
        this.goals = goals;
        this.observer = observer;
        this.engine = engine;
        this.policy = policy;
    }

    /** Must be called from the Minecraft server thread. */
    public void tick(MinecraftServer server) {
        if (server == null) return;
        if (++ticks % SAMPLE_INTERVAL_TICKS != 0) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            observer.observe(player.getCommandSource()).ifPresent(snapshot -> {
                var recommendations = engine.evaluate(snapshot, goals.findByPlayer(snapshot.playerId()));
                recommendations.stream()
                        .sorted(java.util.Comparator.comparing(Recommendation::priority).reversed())
                        .filter(recommendation -> policy.decide(recommendation).allowed())
                        .limit(1)
                        .forEach(recommendation -> player.sendMessage(Text.of("Jarvis 提醒：" + recommendation.message()), false));
            });
        }
    }

    public NotificationPolicy policy() { return policy; }
}
