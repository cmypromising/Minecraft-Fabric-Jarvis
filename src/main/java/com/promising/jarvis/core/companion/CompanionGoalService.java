package com.promising.jarvis.core.companion;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Application service for goal lifecycle; future command adapters can depend on this boundary. */
public final class CompanionGoalService {
    private final GoalStore store;
    private final Clock clock;

    public CompanionGoalService(GoalStore store) { this(store, Clock.systemUTC()); }
    public CompanionGoalService(GoalStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    public CompanionGoal create(UUID playerId, String title, String description, GoalPriority priority,
                                 String targetItemId, int targetCount, boolean guidanceEnabled) {
        Instant now = Instant.now(clock);
        return store.save(new CompanionGoal(UUID.randomUUID(), playerId, title, description, priority,
                GoalStatus.ACTIVE, targetItemId, targetCount, guidanceEnabled, now, now));
    }

    public List<CompanionGoal> activeGoals(UUID playerId) {
        return store.findByPlayer(playerId).stream().filter(goal -> goal.status() == GoalStatus.ACTIVE).toList();
    }

    public CompanionGoal setStatus(UUID playerId, UUID goalId, GoalStatus status) {
        CompanionGoal goal = find(playerId, goalId);
        if (goal.status() == GoalStatus.ARCHIVED || goal.status() == GoalStatus.COMPLETED) {
            throw new IllegalStateException("Terminal goal cannot change");
        }
        if (status == GoalStatus.ARCHIVED && goal.status() != GoalStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed goals can be archived");
        }
        return store.save(goal.withStatus(status, Instant.now(clock)));
    }

    public CompanionGoal setGuidance(UUID playerId, UUID goalId, boolean enabled) {
        return store.save(find(playerId, goalId).withGuidanceEnabled(enabled, Instant.now(clock)));
    }

    public boolean remove(UUID playerId, UUID goalId) { return store.remove(playerId, goalId); }

    public CompanionGoal get(UUID playerId, UUID goalId) { return find(playerId, goalId); }

    private CompanionGoal find(UUID playerId, UUID goalId) {
        return store.findByPlayer(playerId).stream().filter(goal -> goal.id().equals(goalId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
    }
}
