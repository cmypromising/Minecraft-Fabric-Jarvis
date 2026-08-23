import com.promising.jarvis.core.memory.ConversationMemory;
import com.promising.jarvis.core.memory.MemoryTurn;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConversationMemoryTest {
    @Test
    public void keepsOnlyTheMostRecentTurns() {
        ConversationMemory memory = new ConversationMemory(2);
        memory.append(new MemoryTurn("one", "first"));
        memory.append(new MemoryTurn("two", "second"));
        memory.append(new MemoryTurn("three", "third"));

        assertEquals(2, memory.snapshot().size());
        assertEquals("two", memory.snapshot().get(0).userMessage());
        assertTrue(memory.asPromptText().contains("third"));
    }

    @Test
    public void canClearMemory() {
        ConversationMemory memory = new ConversationMemory(2);
        memory.append(new MemoryTurn("hello", "hi"));
        memory.clear();
        assertTrue(memory.snapshot().isEmpty());
    }
}
