import com.promising.jarvis.core.context.ContextComponent;
import com.promising.jarvis.core.context.DynamicContextSelector;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DynamicContextSelectorTest {
    @Test
    public void combinesSelectedComponentsWithoutDuplicateFragments() {
        DynamicContextSelector selector = new DynamicContextSelector(List.of(
                component("player", "player status", true),
                component("world", "world status", true),
                component("duplicate", "player status", true)));

        String selected = selector.collect("相关请求", null);
        assertEquals("player status\nworld status", selected);
    }

    @Test
    public void unrelatedRequestHasNoOptionalContext() {
        DynamicContextSelector selector = new DynamicContextSelector(List.of(component("x", "facts", false)));
        assertTrue(selector.collect("讲个笑话", null).isEmpty());
    }

    private static ContextComponent component(String id, String output, boolean selected) {
        return new ContextComponent() {
            public String id() { return id; }
            public boolean supports(String request) { return selected; }
            public String collect(ServerCommandSource source) { return output; }
        };
    }
}
