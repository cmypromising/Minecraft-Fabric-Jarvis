import com.promising.jarvis.core.awareness.PlayerActivityTracker;
import com.promising.jarvis.core.context.PlayerActivityContextTool;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PlayerActivityContextToolTest {
    @Test
    public void unavailableSourceIsSafe() {
        assertTrue(new PlayerActivityContextTool(new PlayerActivityTracker()).execute(null, "")
                .contains("无法获取玩家活动"));
    }
}
