import com.promising.jarvis.core.awareness.PlayerEvent;
import com.promising.jarvis.core.awareness.PlayerEventType;
import com.promising.jarvis.core.companion.ProactiveEventWindow;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class ProactiveEventWindowTest {
    @Test
    public void keepsRecentBoundedTemporalContext() {
        UUID player = UUID.randomUUID();
        var window = new ProactiveEventWindow();
        window.add(new PlayerEvent(player, PlayerEventType.NIGHTFALL, null, null,
                List.of("进入夜晚"), Instant.now()));
        assertTrue(window.promptText(player).contains("NIGHTFALL"));
        assertTrue(window.snapshot().size() <= 12);
    }
}
