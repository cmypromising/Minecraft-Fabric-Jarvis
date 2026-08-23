package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.observation.PlayerStateSnapshot;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class RecommendationEngine {
    private final Clock clock;
    public RecommendationEngine() { this(Clock.systemUTC()); }
    public RecommendationEngine(Clock clock) { this.clock = clock; }
    public List<Recommendation> evaluate(PlayerStateSnapshot state, List<CompanionGoal> goals) {
        if (state == null || goals == null || goals.isEmpty()) return List.of();
        List<Recommendation> result = new ArrayList<>();
        for (CompanionGoal goal : goals) {
            if (!goal.playerId().equals(state.playerId()) || goal.status() != GoalStatus.ACTIVE || !goal.guidanceEnabled()) continue;
            if (goal.targetCount() > 0) {
                int current = state.resourceCount(goal.targetItemId());
                result.add(new Recommendation(UUID.randomUUID(), state.playerId(), goal.id(), mapPriority(goal.priority()),
                        "目标进度提醒", "你正在推进目标“" + goal.title() + "”，当前资源数量为 " + current + "。",
                        List.of("当前数量: " + current, "目标资源: " + goal.targetItemId(), "目标数量: " + goal.targetCount()), Instant.now(clock)));
            }
        }
        if (state.health() <= 4 || state.foodLevel() <= 6) result.add(new Recommendation(UUID.randomUUID(), state.playerId(), null,
                RecommendationPriority.HIGH, "生存状态提醒", "生命值或饥饿值偏低，请先寻找安全位置并恢复状态。",
                List.of("生命值: " + state.health(), "饥饿值: " + state.foodLevel()), Instant.now(clock)));
        return result;
    }
    public List<Recommendation> evaluate(PlayerStateSnapshot state, List<CompanionGoal> goals, ProactivePerception perception) {
        List<Recommendation> result = new ArrayList<>(evaluate(state, goals));
        if (state == null || perception == null) return result;
        if (perception.danger()) result.add(dangerRecommendation(state, perception));
        else if (perception.night()) result.add(new Recommendation(UUID.randomUUID(), state.playerId(), null, RecommendationPriority.NORMAL,
                "夜晚提醒", "现在已进入夜晚，建议回到庇护所、睡觉或确保周围有足够照明。", perception.evidence(), Instant.now(clock)));
        else if (goals.isEmpty() && "EARLY_SURVIVAL".equals(perception.gamePhase())) result.add(new Recommendation(UUID.randomUUID(), state.playerId(), null, RecommendationPriority.LOW,
                "阶段性生存建议", "你正处于生存早期，建议优先准备食物、基础工具和安全庇护所。", perception.evidence(), Instant.now(clock)));
        return result;
    }
    private Recommendation dangerRecommendation(PlayerStateSnapshot state, ProactivePerception perception) {
        DangerSignal danger = perception.dangers().stream().max(Comparator.comparingInt(DangerSignal::severity)).orElse(null);
        if (danger == null) return new Recommendation(UUID.randomUUID(), state.playerId(), null, RecommendationPriority.CRITICAL,
                "危险环境提醒", "检测到环境风险，请先前往安全位置。", perception.evidence(), Instant.now(clock));
        String message = switch (danger.type()) {
            case DROWNING -> "你正在水下且氧气正在减少，请立即上浮到水面或进入空气方块，避免溺水。";
            case LAVA -> "你正在熔岩中，请立即离开熔岩并寻找安全方块。";
            case FIRE -> "你正在着火，请立即进入水中或寻找灭火方式，并远离火源。";
            case LOW_HEALTH -> "你的生命值很低，请立即停止探索，进入安全位置并恢复生命。";
            case LOW_HUNGER -> "你的饥饿值较低，请尽快补充食物，避免进一步陷入危险。";
            case HOSTILE_NEARBY -> "附近有敌对生物，请点亮周围或退回安全位置，避免被包围。";
        };
        return new Recommendation(UUID.randomUUID(), state.playerId(), null,
                danger.severity() >= 3 ? RecommendationPriority.CRITICAL : RecommendationPriority.HIGH,
                "具体危险提醒: " + danger.type(), message, List.of(danger.evidence()), Instant.now(clock));
    }
    private static RecommendationPriority mapPriority(GoalPriority priority) { return switch (priority) {
        case LOW -> RecommendationPriority.LOW; case NORMAL -> RecommendationPriority.NORMAL; case HIGH -> RecommendationPriority.HIGH; case CRITICAL -> RecommendationPriority.CRITICAL; };
    }
}
