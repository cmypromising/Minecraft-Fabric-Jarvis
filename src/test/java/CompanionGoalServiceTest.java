import com.promising.jarvis.core.companion.*;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.Assert.*;

public class CompanionGoalServiceTest {
    private static final UUID PLAYER = UUID.randomUUID();
    private final CompanionGoalService service = new CompanionGoalService(
            new InMemoryGoalStore(), Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    public void createsAndTransitionsGoalWithoutEmbeddingCommands() {
        CompanionGoal goal = service.create(PLAYER, "找到钻石", "准备制作钻石镐", GoalPriority.HIGH,
                "minecraft:diamond", 3, true);
        assertEquals(GoalStatus.ACTIVE, goal.status());
        assertTrue(service.activeGoals(PLAYER).contains(goal));

        CompanionGoal paused = service.setStatus(PLAYER, goal.id(), GoalStatus.PAUSED);
        assertEquals(GoalStatus.PAUSED, paused.status());
        assertTrue(service.activeGoals(PLAYER).isEmpty());
        assertFalse(paused.description().contains("/"));
    }

    @Test
    public void guidanceCanBeDisabledIndependently() {
        CompanionGoal goal = service.create(PLAYER, "探索", "探索下界", GoalPriority.NORMAL, null, 0, true);
        assertFalse(service.setGuidance(PLAYER, goal.id(), false).guidanceEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetCountRequiresTargetItem() {
        service.create(PLAYER, "收集", "", GoalPriority.NORMAL, null, 4, true);
    }

    @Test(expected = IllegalStateException.class)
    public void completedGoalIsTerminal() {
        CompanionGoal goal = service.create(PLAYER, "完成", "", GoalPriority.NORMAL, null, 0, true);
        service.setStatus(PLAYER, goal.id(), GoalStatus.COMPLETED);
        service.setStatus(PLAYER, goal.id(), GoalStatus.ACTIVE);
    }
}
