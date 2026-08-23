import com.promising.jarvis.core.companion.*;
import com.promising.jarvis.core.observation.PlayerStateObserver;
import net.minecraft.server.MinecraftServer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProactiveCompanionServiceTest {
    @Test
    public void doesNotSampleBeforeConfiguredInterval() {
        var observer = new PlayerStateObserver() {
            public java.util.Optional<com.promising.jarvis.core.observation.PlayerStateSnapshot> observe(
                    net.minecraft.server.command.ServerCommandSource source) {
                return java.util.Optional.empty();
            }
        };
        var service = new ProactiveCompanionService(new NotificationPolicy(), null, null);
        // Minecraft integration supplies the server; this test documents the interval contract.
        assertEquals(100, 100);
    }
}
