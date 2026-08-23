import com.promising.jarvis.core.companion.*;
import com.promising.jarvis.core.observation.PlayerStateSnapshot;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecommendationEngineTest {
    @Test
    public void recommendsWhenTargetResourceIsMissing() {
        UUID player = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        var goal = new CompanionGoal(goalId, player, "找到钻石", "", GoalPriority.HIGH, GoalStatus.ACTIVE,
                "minecraft:diamond", 3, true, Instant.EPOCH, Instant.EPOCH);
        var state = state(player, 20, 20, Map.of("minecraft:diamond", 1));

        var recommendations = new RecommendationEngine(fixedClock()).evaluate(state, List.of(goal));
        assertEquals(1, recommendations.size());
        assertTrue(recommendations.getFirst().evidence().getFirst().contains("1"));
        assertEquals(goalId, recommendations.getFirst().goalId());
    }

    @Test
    public void emitsSurvivalWarningAndIgnoresPausedGoals() {
        UUID player = UUID.randomUUID();
        var goal = new CompanionGoal(UUID.randomUUID(), player, "探索", "", GoalPriority.NORMAL, GoalStatus.PAUSED,
                null, 0, true, Instant.EPOCH, Instant.EPOCH);
        var recommendations = new RecommendationEngine(fixedClock()).evaluate(state(player, 3, 5, Map.of()), List.of(goal));
        assertEquals(1, recommendations.size());
        assertEquals("生存状态提醒", recommendations.getFirst().title());
    }

    private static PlayerStateSnapshot state(UUID player, float health, int food, Map<String, Integer> resources) {
        return new PlayerStateSnapshot(player, "Alex", health, food, 1, 0, 64, 0,
                "minecraft:overworld", Difficulty.NORMAL, GameMode.SURVIVAL, resources);
    }

    private static Clock fixedClock() { return Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC); }
}
