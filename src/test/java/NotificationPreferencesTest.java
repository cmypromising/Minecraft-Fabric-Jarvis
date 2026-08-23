import com.promising.jarvis.core.companion.NotificationPreferences;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class NotificationPreferencesTest {
    @Test
    public void proactiveNotificationsAreExplicitlyOptIn() {
        assertFalse(NotificationPreferences.defaults().proactiveEnabled());
    }
}
