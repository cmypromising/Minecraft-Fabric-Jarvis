package com.promising.jarvis.core.executor.impl;

import com.promising.jarvis.core.context.PlayerContext;

import java.util.Locale;
import java.util.Set;

/** Validates model-generated commands against syntax, command and game-context constraints. */
public final class CommandSafetyPolicy {
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "weather", "time", "locate", "give", "clear", "effect", "teleport", "tp",
            "gamemode", "seed", "recipe", "say"
    );

    private CommandSafetyPolicy() {}

    public static boolean isAllowed(String command) {
        return isAllowed(command, null);
    }

    public static boolean isAllowed(String command, PlayerContext context) {
        if (command == null || command.isBlank()) return false;
        String normalized = command.trim();
        if (!normalized.startsWith("/") || normalized.length() > 256
                || normalized.contains("\n") || normalized.contains("\r")
                || normalized.contains(";") || normalized.contains("&&") || normalized.contains("||")) return false;

        String[] tokens = normalized.substring(1).trim().split("\\s+");
        if (tokens.length == 0) return false;
        String name = tokens[0].toLowerCase(Locale.ROOT);
        if (!ALLOWED_COMMANDS.contains(name)) return false;
        if (context == null) return true;

        return switch (name) {
            case "give" -> context.gameMode().isCreative();
            case "gamemode" -> isSafeGameModeChange(tokens, context);
            default -> true;
        };
    }

    private static boolean isSafeGameModeChange(String[] tokens, PlayerContext context) {
        if (tokens.length > 3) return false;
        String target = tokens.length == 2 ? "@s" : tokens[2];
        if (!"@s".equals(target) && !context.playerName().equals(target)) return false;
        String requestedMode = tokens[1].toLowerCase(Locale.ROOT);
        return requestedMode.equals("survival") || requestedMode.equals("creative")
                || requestedMode.equals("adventure");
    }
}
