import com.promising.jarvis.core.executor.impl.CommandSafetyPolicy;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}
