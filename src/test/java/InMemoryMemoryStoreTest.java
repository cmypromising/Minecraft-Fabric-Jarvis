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
        assertEquals(1, store.size(first));
        assertTrue(store.recent(second).isEmpty());
    }

    @Test
    public void clearingOnePlayerDoesNotAffectAnother() {
        InMemoryMemoryStore store = new InMemoryMemoryStore(4);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        store.append(first, new MemoryTurn("first", "reply"));
        store.append(second, new MemoryTurn("second", "reply"));

        store.clear(first);

        assertEquals(0, store.size(first));
        assertEquals(1, store.size(second));
    }
}
