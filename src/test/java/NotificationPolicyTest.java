import com.promising.jarvis.core.companion.*;
import org.junit.Test;

import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotificationPolicyTest {
    private static final UUID PLAYER = UUID.randomUUID();
    private static final Recommendation RECOMMENDATION = new Recommendation(UUID.randomUUID(), PLAYER, null,
            RecommendationPriority.NORMAL, "提醒", "请注意", List.of("evidence"), Instant.EPOCH);

    @Test
    public void isOptInAndDeduplicatesWithinCooldown() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);
        var policy = new NotificationPolicy(clock);
        assertFalse(policy.decide(RECOMMENDATION).allowed());
        policy.setPreferences(PLAYER, new NotificationPreferences(true, Duration.ofMinutes(15), 6,
                LocalTime.of(23, 0), LocalTime.of(7, 0)));
        assertTrue(policy.decide(RECOMMENDATION).allowed());
        assertFalse(policy.decide(RECOMMENDATION).allowed());
    }

    @Test
    public void blocksQuietHours() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T23:30:00Z"), ZoneOffset.UTC);
        var policy = new NotificationPolicy(clock);
        policy.setPreferences(PLAYER, new NotificationPreferences(true, Duration.ofMinutes(1), 6,
                LocalTime.of(23, 0), LocalTime.of(7, 0)));
        assertFalse(policy.decide(RECOMMENDATION).allowed());
    }
}
