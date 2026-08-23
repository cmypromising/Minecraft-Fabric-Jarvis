import com.promising.jarvis.core.agent.task.AgentTask;
import com.promising.jarvis.core.companion.ProactiveLlmAgent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProactiveAgentConfigurationTest {
    @Test
    public void proactiveReasoningBudgetAllowsToolCallsAndFinalAnswer() {
        assertEquals(4, AgentTask.DEFAULT_MAX_REASONING_STEPS);
        assertEquals(6, ProactiveLlmAgent.MAX_REASONING_STEPS);
    }
}
