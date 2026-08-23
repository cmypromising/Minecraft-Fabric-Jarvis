import com.promising.jarvis.core.awareness.*;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class PlayerEventBusTest {
    @Test
    public void deliversEventsToSubscribers() {
        var bus = new PlayerEventBus();
        var count = new AtomicInteger();
        bus.subscribe(event -> count.incrementAndGet());
        bus.publish(new PlayerEvent(UUID.randomUUID(), PlayerEventType.JOINED, null, null,
                List.of("test"), Instant.EPOCH));
        assertEquals(1, count.get());
    }
}
