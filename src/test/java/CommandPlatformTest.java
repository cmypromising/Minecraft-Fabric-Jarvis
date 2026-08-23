import com.promising.jarvis.core.capability.Capability;
import com.promising.jarvis.core.capability.CapabilityRegistry;
import com.promising.jarvis.core.command.CommandRequest;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandPlatformTest {
    @Test
    public void commandRequestNormalizesInput() {
        assertEquals("weather", new CommandRequest("  weather  ").text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void commandRequestRejectsBlankInput() {
        new CommandRequest(" ");
    }

    @Test
    public void capabilityRegistryKeepsRegisteredCapabilities() {
        CapabilityRegistry registry = new CapabilityRegistry();
        registry.register(new Capability() {
            public String id() { return "test"; }
            public boolean supports(ContentResponseBody response) { return true; }
            public void execute(com.promising.jarvis.core.context.CommandContext context, ContentResponseBody response) { }
        });
        assertTrue(registry.ids().contains("test"));
    }
}
