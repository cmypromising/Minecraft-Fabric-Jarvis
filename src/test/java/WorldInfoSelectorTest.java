import com.promising.jarvis.core.world.WorldInfoComponent;
import com.promising.jarvis.core.world.WorldInfoSelector;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorldInfoSelectorTest {
    @Test
    public void selectsOnlyComponentsRelevantToRequest() {
        WorldInfoSelector selector = new WorldInfoSelector(List.of(
                component("weather", "weather facts", true),
                component("location", "location facts", false)));

        assertEquals("weather facts", selector.collect("天气怎么样", null));
    }

    @Test
    public void doesNotInjectWorldInfoForUnrelatedRequest() {
        WorldInfoSelector selector = new WorldInfoSelector(List.of(component("weather", "facts", false)));
        assertTrue(selector.collect("讲个笑话", null).isEmpty());
    }

    @Test
    public void boundsCollectedContext() {
        WorldInfoSelector selector = new WorldInfoSelector(List.of(
                component("large", "x".repeat(3000), true)));
        assertTrue(selector.collect("相关请求", null).length() <= 2000);
    }

    private static WorldInfoComponent component(String id, String output, boolean selected) {
        return new WorldInfoComponent() {
            public String id() { return id; }
            public boolean supports(String request) { return selected; }
            public String collect(ServerCommandSource source) { return output; }
        };
    }
}
