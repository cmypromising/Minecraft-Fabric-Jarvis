import com.promising.jarvis.core.companion.ActiveAwarenessContext;
import com.promising.jarvis.core.companion.ProactivePerception;
import com.promising.jarvis.core.awareness.ActivityObservation;
import com.promising.jarvis.core.awareness.PlayerActivity;
import com.promising.jarvis.core.observation.PlayerStateSnapshot;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class ActiveAwarenessContextTest {
    @Test
    public void snapshotContainsVersionAndPromptFacts() {
        var player = new PlayerStateSnapshot(UUID.randomUUID(), "Alex", 20, 20, 1, 1, 64, 2,
                "minecraft:overworld", Difficulty.NORMAL, GameMode.SURVIVAL, Map.of());
        var perception = new ProactivePerception("minecraft:plains", "minecraft:overworld", false,
                false, false, "EARLY_SURVIVAL", List.of(), List.of());
        var activity = new ActivityObservation(player.playerId(), PlayerActivity.UNKNOWN, 0.4, List.of("首次观察"), Instant.now());
        var context = new ActiveAwarenessContext(7, Instant.now(), player, perception, activity, "FIRST_JOIN");
        assertTrue(context.promptText().contains("v7"));
        assertTrue(context.promptText().contains("FIRST_JOIN"));
    }
}
