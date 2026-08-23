import com.promising.jarvis.llm.prompt.SystemPromptLoader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemPromptLoaderTest {
    @Test
    public void loadsVersionedMinecraftAssistantContract() {
        var prompt = SystemPromptLoader.getPrompt("minecraft_assistant").orElseThrow();
        assertEquals(1, SystemPromptLoader.schemaVersion());
        assertEquals("1.0.0", prompt.version());
        assertEquals("system", prompt.getRole());
        assertTrue(prompt.getContent().contains("context.tool"));
        assertTrue(prompt.getContent().contains("minecraft.command"));
    }

    @Test
    public void missingPromptIsExplicitlyAbsent() {
        assertFalse(SystemPromptLoader.getPrompt("missing").isPresent());
    }
}
