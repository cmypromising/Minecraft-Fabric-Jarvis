package com.promising.jarvis.core.companion;

public record NotificationDecision(boolean allowed, String reason) {
    public static NotificationDecision allow() { return new NotificationDecision(true, "allowed"); }
    public static NotificationDecision deny(String reason) { return new NotificationDecision(false, reason); }
}
