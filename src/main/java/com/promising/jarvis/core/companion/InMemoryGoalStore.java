package com.promising.jarvis.core.companion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Thread-safe development store; persistence is intentionally a later roadmap item. */
public final class InMemoryGoalStore implements GoalStore {
    private final ConcurrentMap<UUID, ConcurrentMap<UUID, CompanionGoal>> goals = new ConcurrentHashMap<>();

    public CompanionGoal save(CompanionGoal goal) {
        goals.computeIfAbsent(goal.playerId(), ignored -> new ConcurrentHashMap<>()).put(goal.id(), goal);
        return goal;
    }

    public List<CompanionGoal> findByPlayer(UUID playerId) {
        var playerGoals = goals.get(playerId);
        return playerGoals == null ? List.of() : List.copyOf(playerGoals.values());
    }

    public boolean remove(UUID playerId, UUID goalId) {
        var playerGoals = goals.get(playerId);
        if (playerGoals == null) return false;
        boolean removed = playerGoals.remove(goalId) != null;
        if (playerGoals.isEmpty()) goals.remove(playerId, playerGoals);
        return removed;
    }
}
