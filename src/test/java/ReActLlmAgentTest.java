import com.promising.jarvis.core.agent.SingleThreadLlmAgent;
import com.promising.jarvis.core.command.CommandRequest;
import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.core.context.ContextTool;
import com.promising.jarvis.core.context.ContextToolRegistry;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ReActLlmAgentTest {
    @Test
    public void callsToolThenReturnsFinalResponse() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        NLParser parser = new NLParser() {
            public ContentResponseBody parse(String request, String context) {
                ContentResponseBody response = new ContentResponseBody();
                if (calls.getAndIncrement() == 0) {
                    response.setCapability("context.tool");
                    response.setTool("test.fact");
                } else {
                    response.setCapability("minecraft.information");
                    response.setAdditionalInfo(context);
                }
                return response;
            }

        };
        ContextToolRegistry tools = new ContextToolRegistry().register(new ContextTool() {
            public String name() { return "test.fact"; }
            public String description() { return "fact"; }
            public String execute(net.minecraft.server.command.ServerCommandSource source, String arguments) { return "42"; }
        });

        try (SingleThreadLlmAgent agent = new SingleThreadLlmAgent(parser, tools)) {
            ContentResponseBody result = agent.submit(
                    new CommandContext(new CommandRequest("fact"), null, null, ""), "base")
                    .get(2, TimeUnit.SECONDS);
            assertEquals("minecraft.information", result.getCapability());
            assertEquals(2, calls.get());
        }
    }

    @Test
    public void doesNotExecuteTheSameToolCallTwice() throws Exception {
        AtomicInteger parserCalls = new AtomicInteger();
        AtomicInteger toolCalls = new AtomicInteger();
        NLParser parser = (request, context) -> {
            ContentResponseBody response = new ContentResponseBody();
            if (parserCalls.getAndIncrement() < 2) {
                response.setCapability("context.tool");
                response.setTool("test.fact");
                response.setToolArguments("same");
            } else {
                response.setCapability("minecraft.information");
                response.setAdditionalInfo(context);
            }
            return response;
        };
        ContextToolRegistry tools = new ContextToolRegistry().register(new ContextTool() {
            public String name() { return "test.fact"; }
            public String description() { return "fact"; }
            public String execute(net.minecraft.server.command.ServerCommandSource source, String arguments) {
                toolCalls.incrementAndGet();
                return "42";
            }
        });
        try (SingleThreadLlmAgent agent = new SingleThreadLlmAgent(parser, tools)) {
            ContentResponseBody result = agent.submit(
                    new CommandContext(new CommandRequest("fact"), null, null, ""), "base")
                    .get(2, TimeUnit.SECONDS);
            assertEquals("minecraft.information", result.getCapability());
            assertEquals(1, toolCalls.get());
            assertEquals(3, parserCalls.get());
        }
    }
}
