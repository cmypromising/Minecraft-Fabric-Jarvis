import com.promising.jarvis.core.executor.impl.CommandSafetyPolicy;
import com.promising.jarvis.core.context.PlayerContext;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

public class CommandSafetyPolicyTest {
    @Test
    public void allowsSafeGameplayCommands() {
        assertTrue(CommandSafetyPolicy.isAllowed("/weather clear"));
        assertTrue(CommandSafetyPolicy.isAllowed("/give @s minecraft:diamond 1"));
    }

    @Test
    public void rejectsAdministrativeAndMalformedCommands() {
        assertFalse(CommandSafetyPolicy.isAllowed("/op player"));
        assertFalse(CommandSafetyPolicy.isAllowed("/stop"));
        assertFalse(CommandSafetyPolicy.isAllowed("weather clear"));
        assertFalse(CommandSafetyPolicy.isAllowed("/say hello\n/stop"));
    }

    @Test
    public void rejectsDifficultyChangesAndModeMismatchedGive() {
        PlayerContext survival = context(GameMode.SURVIVAL);
        assertFalse(CommandSafetyPolicy.isAllowed("/difficulty hard", survival));
        assertFalse(CommandSafetyPolicy.isAllowed("/give @s minecraft:diamond 1", survival));
        assertTrue(CommandSafetyPolicy.isAllowed("/give @s minecraft:diamond 1", context(GameMode.CREATIVE)));
    }

    @Test
    public void gamemodeCanOnlyTargetSelfAndNotSpectator() {
        PlayerContext survival = context(GameMode.SURVIVAL);
        assertTrue(CommandSafetyPolicy.isAllowed("/gamemode creative", survival));
        assertFalse(CommandSafetyPolicy.isAllowed("/gamemode spectator", survival));
        assertFalse(CommandSafetyPolicy.isAllowed("/gamemode creative OtherPlayer", survival));
    }

    private static PlayerContext context(GameMode mode) {
        return new PlayerContext(UUID.randomUUID(), "Player", 0, 64, 0, 20, 20, 0, mode, Difficulty.NORMAL);
    }
}
