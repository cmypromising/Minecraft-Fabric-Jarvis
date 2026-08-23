package com.promising.jarvis.core.memory;

/** One completed conversational turn retained by the context manager. */
public record MemoryTurn(String userMessage, String assistantMessage) {
    public MemoryTurn {
        if (userMessage == null || userMessage.isBlank()) throw new IllegalArgumentException("User message must not be blank");
        if (assistantMessage == null) assistantMessage = "";
    }
}
