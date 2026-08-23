package com.promising.jarvis.core.companion;

import java.time.Instant;

public record NotificationHistory(Instant sentAt, String hourlyBucket) { }
