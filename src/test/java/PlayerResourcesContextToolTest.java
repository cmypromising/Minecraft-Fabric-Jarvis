import com.promising.jarvis.core.context.PlayerResourcesContextTool;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PlayerResourcesContextToolTest {
    @Test
    public void unavailablePlayerProducesSafeObservation() {
        String result = new PlayerResourcesContextTool().execute(null, "");
        assertTrue(result.contains("无法获取当前玩家资源"));
    }

    @Test
    public void hasBoundedOutputContract() {
        assertTrue(new PlayerResourcesContextTool().execute(null, "").length() < 3000);
    }
}
