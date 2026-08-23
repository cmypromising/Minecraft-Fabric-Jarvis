import com.promising.jarvis.core.companion.JsonNotificationHistoryStore;
import com.promising.jarvis.core.companion.NotificationHistory;
import org.junit.Test;

import java.nio.file.Files;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class JsonNotificationHistoryStoreTest {
    @Test
    public void historySurvivesStoreReload() throws Exception {
        var file = Files.createTempFile("jarvis-history", ".json");
        try {
            var history = new NotificationHistory(Instant.EPOCH, "2026-01-01T12");
            new JsonNotificationHistoryStore(file).save("key", history);
            assertEquals(history, new JsonNotificationHistoryStore(file).find("key").orElseThrow());
        } finally { Files.deleteIfExists(file); }
    }
}
