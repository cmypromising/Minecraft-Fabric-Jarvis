import com.promising.jarvis.core.memory.InMemoryMemoryStore;
import com.promising.jarvis.core.memory.MemoryTurn;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InMemoryMemoryStoreTest {
    @Test
    public void isolatesPlayersByUuid() {
        InMemoryMemoryStore store = new InMemoryMemoryStore(4);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        store.append(first, new MemoryTurn("first", "reply"));

        assertEquals(1, store.recent(first).size());
        assertTrue(store.recent(second).isEmpty());
    }
}
