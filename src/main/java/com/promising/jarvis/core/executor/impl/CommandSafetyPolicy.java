package com.promising.jarvis.core.executor.impl;

import java.util.Set;

/** Validates commands returned by the LLM before Minecraft executes them. */
public final class CommandSafetyPolicy {
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "weather", "time", "locate", "give", "clear", "effect", "teleport", "tp",
            "gamemode", "difficulty", "seed", "recipe", "say"
    );

    private CommandSafetyPolicy() {}

    public static boolean isAllowed(String command) {
        if (command == null || command.isBlank()) return false;
        String normalized = command.trim();
        if (!normalized.startsWith("/")) return false;
        if (normalized.length() > 256 || normalized.contains("\n") || normalized.contains("\r")) return false;

        String commandName = normalized.substring(1).trim().split("\\s+", 2)[0].toLowerCase();
        return ALLOWED_COMMANDS.contains(commandName);
    }
}
