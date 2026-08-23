import com.promising.jarvis.core.capability.Capability;
import com.promising.jarvis.core.capability.CapabilityRegistry;
import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class CapabilityProtocolTest {
    @Test
    public void unknownNamedCapabilityDoesNotFallbackToLegacyType() {
        CapabilityRegistry registry = new CapabilityRegistry();
        registry.register(new Capability() {
            public String id() { return "minecraft.command"; }
            public boolean supports(ContentResponseBody response) { return true; }
            public void execute(CommandContext context, ContentResponseBody response) { }
        });

        ContentResponseBody response = new ContentResponseBody();
        response.setCapability("unknown.capability");
        response.setType(1);
        assertFalse(registry.dispatch(null, response));
    }
}
