package com.promising.jarvis.core.companion;

import java.util.UUID;

public interface NotificationPreferencesStore {
    NotificationPreferences get(UUID playerId);
    void save(UUID playerId, NotificationPreferences preferences);
}
