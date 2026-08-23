import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReActProtocolTest {
    @Test
    public void recognizesOnlyContextToolCapabilityAsToolRequest() {
        ContentResponseBody tool = new ContentResponseBody();
        tool.setCapability("context.tool");
        tool.setTool("world.location");
        assertTrue(tool.isContextToolRequest());

        ContentResponseBody finalResponse = new ContentResponseBody();
        finalResponse.setCapability("minecraft.information");
        finalResponse.setTool("world.location");
        assertFalse(finalResponse.isContextToolRequest());
    }
}
