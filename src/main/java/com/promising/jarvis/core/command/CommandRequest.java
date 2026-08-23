package com.promising.jarvis.core.command;

/** A user's request entered through the intelligent command line. */
public record CommandRequest(String text) {
    public CommandRequest {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Command text must not be blank");
        text = text.trim();
    }
}
