import com.promising.jarvis.core.agent.task.AgentTask;
import com.promising.jarvis.core.agent.task.TaskPriority;
import com.promising.jarvis.core.agent.task.TaskStatus;
import com.promising.jarvis.core.command.CommandRequest;
import com.promising.jarvis.core.context.CommandContext;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgentTaskTest {
    private static CommandContext context() {
        return new CommandContext(new CommandRequest("hello"), null, null, "");
    }

    @Test
    public void builderExposesSchedulingAndReasoningPolicy() {
        AgentTask task = AgentTask.builder(context())
                .promptContext("facts")
                .type("player.request")
                .metadata("source", "nlp")
                .priority(TaskPriority.HIGH)
                .timeout(Duration.ofSeconds(5))
                .maxReasoningSteps(2)
                .build();

        assertEquals("facts", task.promptContext());
        assertEquals("player.request", task.type());
        assertEquals("nlp", task.metadata().get("source"));
        assertEquals(TaskPriority.HIGH, task.priority());
        assertEquals(2, task.maxReasoningSteps());
        assertEquals(TaskStatus.QUEUED, task.status());
        assertTrue(task.deadline().isAfter(task.createdAt()));
    }

    @Test
    public void cancellationIsTerminal() {
        AgentTask task = AgentTask.builder(context()).build();
        assertTrue(task.cancel());
        assertEquals(TaskStatus.CANCELLED, task.status());
        assertTrue(task.result().isCancelled());
        assertTrue(!task.cancel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidReasoningLimit() {
        AgentTask.builder(context()).maxReasoningSteps(0).build();
    }

    @Test
    public void higherPriorityTaskSortsFirst() {
        AgentTask low = AgentTask.builder(context()).priority(TaskPriority.LOW).build();
        AgentTask high = AgentTask.builder(context()).priority(TaskPriority.HIGH).build();
        List<AgentTask> tasks = new ArrayList<>(List.of(low, high));
        Collections.sort(tasks);
        assertEquals(high, tasks.getFirst());
    }
}
