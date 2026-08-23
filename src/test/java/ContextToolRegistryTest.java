import com.promising.jarvis.core.context.ContextTool;
import com.promising.jarvis.core.context.ContextToolRegistry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContextToolRegistryTest {
    @Test
    public void discoversAndExecutesRegisteredTool() {
        ContextToolRegistry registry = new ContextToolRegistry()
                .register(new ContextTool() {
                    public String name() { return "test.fact"; }
                    public String description() { return "test fact"; }
                    public String execute(net.minecraft.server.command.ServerCommandSource source, String arguments) {
                        return "fact:" + arguments;
                    }
                });

        assertTrue(registry.describe().contains("test.fact"));
        assertEquals("fact:x", registry.execute("test.fact", null, "x").orElseThrow());
        assertTrue(registry.execute("missing", null, "").isEmpty());
    }
}
