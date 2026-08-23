import com.promising.jarvis.core.awareness.*;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlayerActivityTrackerTest {
    @Test
    public void classifiesDimensionAndPositionEvents() {
        UUID player = UUID.randomUUID();
        var clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        var tracker = new PlayerActivityTracker(clock);
        tracker.onEvent(new PlayerEvent(player, PlayerEventType.DIMENSION_CHANGED, null, null,
                List.of("dimension"), Instant.now(clock)));
        assertEquals(PlayerActivity.EXPLORING, tracker.activityFor(player).activity());
        tracker.onEvent(new PlayerEvent(player, PlayerEventType.POSITION_CHANGED, null, null,
                List.of("position"), Instant.now(clock).plusSeconds(2)));
        assertEquals(PlayerActivity.TRAVELING, tracker.activityFor(player).activity());
        assertTrue(tracker.activityFor(player).confidence() > 0);
    }

    @Test
    public void strongerEventWinsWithinSameObservationBurst() {
        UUID player = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        var tracker = new PlayerActivityTracker(Clock.fixed(now, ZoneOffset.UTC));
        tracker.onEvent(new PlayerEvent(player, PlayerEventType.DIMENSION_CHANGED, null, null,
                List.of("dimension"), now));
        tracker.onEvent(new PlayerEvent(player, PlayerEventType.POSITION_CHANGED, null, null,
                List.of("position"), now));
        assertEquals(PlayerActivity.EXPLORING, tracker.activityFor(player).activity());
    }
}
