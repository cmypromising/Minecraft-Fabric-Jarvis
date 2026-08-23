package com.promising.jarvis.core.companion;

import java.time.Duration;
import java.time.LocalTime;

/** Player-controlled proactive notification policy. Opt-in is intentionally false by default. */
public record NotificationPreferences(
        boolean proactiveEnabled,
        Duration cooldown,
        int maxNotificationsPerHour,
        LocalTime quietStart,
        LocalTime quietEnd) {
    public NotificationPreferences {
        if (cooldown.isNegative() || cooldown.isZero()) throw new IllegalArgumentException("cooldown must be positive");
        if (maxNotificationsPerHour < 1) throw new IllegalArgumentException("notification limit must be positive");
    }

    public static NotificationPreferences defaults() {
        return new NotificationPreferences(false, Duration.ofMinutes(15), 6,
                LocalTime.of(23, 0), LocalTime.of(7, 0));
    }

    public boolean isQuiet(LocalTime time) {
        if (quietStart.equals(quietEnd)) return false;
        if (quietStart.isBefore(quietEnd)) return !time.isBefore(quietStart) && time.isBefore(quietEnd);
        return !time.isBefore(quietStart) || time.isBefore(quietEnd);
    }
}
