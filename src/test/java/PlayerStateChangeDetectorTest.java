import com.promising.jarvis.core.awareness.PlayerEventType;
import com.promising.jarvis.core.awareness.PlayerStateChangeDetector;
import com.promising.jarvis.core.observation.PlayerStateSnapshot;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlayerStateChangeDetectorTest {
    @Test
    public void emitsOnlyChangedSemanticEvents() {
        UUID player = UUID.randomUUID();
        var previous = snapshot(player, 0, 64, 0, "minecraft:overworld", 20, 20, Map.of("minecraft:stone", 1));
        var current = snapshot(player, 10, 64, 0, "minecraft:the_nether", 18, 20, Map.of("minecraft:stone", 3));
        var events = new PlayerStateChangeDetector().detect(previous, current);
        assertEquals(4, events.size());
        assertTrue(events.stream().anyMatch(event -> event.type() == PlayerEventType.DIMENSION_CHANGED));
        assertTrue(events.stream().anyMatch(event -> event.type() == PlayerEventType.POSITION_CHANGED));
        assertTrue(events.stream().anyMatch(event -> event.type() == PlayerEventType.HEALTH_CHANGED));
        assertTrue(events.stream().anyMatch(event -> event.type() == PlayerEventType.RESOURCE_CHANGED));
    }

    @Test
    public void emitsDeathAndRespawnEvents() {
        UUID player = UUID.randomUUID();
        var detector = new PlayerStateChangeDetector();
        var alive = snapshot(player, 0, 64, 0, "minecraft:overworld", 20, 20, Map.of());
        var dead = snapshot(player, 0, 64, 0, "minecraft:overworld", 0, 20, Map.of());
        assertEquals(PlayerEventType.DEATH, detector.detect(alive, dead).getFirst().type());
        assertEquals(PlayerEventType.RESPAWN, detector.detect(dead, alive).getFirst().type());
    }

    @Test
    public void unchangedSnapshotProducesNoEvents() {
        UUID player = UUID.randomUUID();
        var state = snapshot(player, 0, 64, 0, "minecraft:overworld", 20, 20, Map.of());
        assertTrue(new PlayerStateChangeDetector().detect(state, state).isEmpty());
    }

    private static PlayerStateSnapshot snapshot(UUID id, int x, int y, int z, String dimension,
                                                float health, int food, Map<String, Integer> resources) {
        return new PlayerStateSnapshot(id, "Alex", health, food, 1, x, y, z, dimension,
                Difficulty.NORMAL, GameMode.SURVIVAL, resources);
    }
}
