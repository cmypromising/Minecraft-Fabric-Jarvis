package com.promising.jarvis.core.companion;

import java.util.List;
import java.util.UUID;

public interface GoalStore {
    CompanionGoal save(CompanionGoal goal);
    List<CompanionGoal> findByPlayer(UUID playerId);
    boolean remove(UUID playerId, UUID goalId);
}
