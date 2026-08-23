import com.promising.jarvis.core.observation.PlayerStateSnapshot;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class PlayerStateSnapshotTest {
    @Test
    public void snapshotDefensivelyCopiesResources() {
        var resources = new java.util.HashMap<String, Integer>();
        resources.put("minecraft:stone", 32);
        PlayerStateSnapshot snapshot = new PlayerStateSnapshot(UUID.randomUUID(), "Alex", 20, 20, 3,
                1, 2, 3, "minecraft:overworld", Difficulty.NORMAL, GameMode.SURVIVAL, resources);
        resources.put("minecraft:diamond", 1);

        assertEquals(32, snapshot.resourceCount("minecraft:stone"));
        assertEquals(0, snapshot.resourceCount("minecraft:diamond"));
        try {
            snapshot.resources().put("minecraft:dirt", 1);
        } catch (UnsupportedOperationException expected) { }
    }
}
