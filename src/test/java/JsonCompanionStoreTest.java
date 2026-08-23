import com.promising.jarvis.core.companion.*;
import org.junit.Test;

import java.nio.file.Files;
import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonCompanionStoreTest {
    @Test
    public void goalsSurviveStoreReload() throws Exception {
        var file = Files.createTempFile("jarvis-goals", ".json");
        try {
            UUID player = UUID.randomUUID();
            CompanionGoal goal = new CompanionGoal(UUID.randomUUID(), player, "目标", "说明", GoalPriority.HIGH,
                    GoalStatus.ACTIVE, "minecraft:stone", 4, true, Instant.EPOCH, Instant.EPOCH);
            new JsonGoalStore(file).save(goal);
            assertEquals(goal, new JsonGoalStore(file).findByPlayer(player).getFirst());
        } finally { Files.deleteIfExists(file); }
    }

    @Test
    public void notificationPreferencesSurviveStoreReload() throws Exception {
        var file = Files.createTempFile("jarvis-preferences", ".json");
        try {
            UUID player = UUID.randomUUID();
            var preferences = new NotificationPreferences(true, java.time.Duration.ofMinutes(5), 2,
                    java.time.LocalTime.of(22, 0), java.time.LocalTime.of(6, 0));
            var store = new JsonNotificationPreferencesStore(file);
            store.save(player, preferences);
            assertTrue(new JsonNotificationPreferencesStore(file).get(player).proactiveEnabled());
            assertEquals(2, new JsonNotificationPreferencesStore(file).get(player).maxNotificationsPerHour());
        } finally { Files.deleteIfExists(file); }
    }
}
