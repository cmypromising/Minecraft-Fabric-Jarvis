import com.promising.jarvis.core.agent.SingleThreadLlmAgent;
import com.promising.jarvis.core.command.CommandRequest;
import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SingleThreadLlmAgentTest {
    @Test
    public void parsesRequestsOnDedicatedAgentThread() throws Exception {
        String submittingThread = Thread.currentThread().getName();
        NLParser parser = new NLParser() {
            public ContentResponseBody parse(String request, String context) {
                ContentResponseBody response = new ContentResponseBody();
                response.setAdditionalInfo(Thread.currentThread().getName());
                return response;
            }

            public int getPriority() { return 0; }
        };

        try (SingleThreadLlmAgent agent = new SingleThreadLlmAgent(parser)) {
            ContentResponseBody response = agent.submit(
                    new CommandContext(new CommandRequest("hello"), null, null, ""), "facts")
                    .get(2, TimeUnit.SECONDS);

            assertEquals("jarvis-llm-agent", response.getAdditionalInfo());
            assertNotEquals(submittingThread, response.getAdditionalInfo());
        }
    }
}
