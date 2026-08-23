package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.observation.PlayerStateSnapshot;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Deterministic first-pass rule engine. It proposes guidance and never executes actions. */
public final class RecommendationEngine {
    private final Clock clock;

    public RecommendationEngine() { this(Clock.systemUTC()); }
    public RecommendationEngine(Clock clock) { this.clock = clock; }

    public List<Recommendation> evaluate(PlayerStateSnapshot state, List<CompanionGoal> goals) {
        if (state == null || goals == null || goals.isEmpty()) return List.of();
        List<Recommendation> result = new ArrayList<>();
        for (CompanionGoal goal : goals) {
            if (!goal.playerId().equals(state.playerId()) || goal.status() != GoalStatus.ACTIVE
                    || !goal.guidanceEnabled()) continue;
            if (goal.targetCount() > 0) {
                int current = state.resourceCount(goal.targetItemId());
                if (current < goal.targetCount()) {
                    result.add(new Recommendation(UUID.randomUUID(), state.playerId(), goal.id(),
                            mapPriority(goal.priority()), "目标进度提醒",
                            "你正在推进“" + goal.title() + "”，还需要继续收集目标资源。",
                            List.of("目标资源 " + goal.targetItemId() + " 当前数量: " + current,
                                    "目标数量: " + goal.targetCount()), Instant.now(clock)));
                } else {
                    result.add(new Recommendation(UUID.randomUUID(), state.playerId(), goal.id(),
                            RecommendationPriority.HIGH, "目标进度已满足",
                            "你已经拥有足够的目标资源，可以继续完成“" + goal.title() + "”。",
                            List.of("目标资源 " + goal.targetItemId() + " 当前数量: " + current), Instant.now(clock)));
                }
            }
        }
        if (state.health() <= 4 || state.foodLevel() <= 6) {
            result.add(new Recommendation(UUID.randomUUID(), state.playerId(), null,
                    RecommendationPriority.HIGH, "生存状态提醒", "你的生命值或饥饿值偏低，建议先寻找安全位置并恢复状态。",
                    List.of("生命值: " + state.health(), "饥饿值: " + state.foodLevel()), Instant.now(clock)));
        }
        return result;
    }

    private static RecommendationPriority mapPriority(GoalPriority priority) {
        return switch (priority) {
            case LOW -> RecommendationPriority.LOW;
            case NORMAL -> RecommendationPriority.NORMAL;
            case HIGH -> RecommendationPriority.HIGH;
            case CRITICAL -> RecommendationPriority.CRITICAL;
        };
    }
}
