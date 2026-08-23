package com.promising.jarvis.core.companion;

import java.time.Instant;
import java.util.Optional;

public interface NotificationHistoryStore {
    Optional<NotificationHistory> find(String key);
    void save(String key, NotificationHistory history);
}
