package com.promising.jarvis.core.companion;

public record DangerSignal(DangerType type, int severity, String evidence) {
    public DangerSignal {
        if (severity < 1 || severity > 3) throw new IllegalArgumentException("severity must be 1..3");
    }
}
