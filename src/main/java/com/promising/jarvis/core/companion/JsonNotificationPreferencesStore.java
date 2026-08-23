package com.promising.jarvis.core.companion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Versioned JSON store for player notification consent and limits. */
public final class JsonNotificationPreferencesStore implements NotificationPreferencesStore {
    private static final int SCHEMA_VERSION = 1;
    private final Path file;
    private final Map<UUID, NotificationPreferences> values = new ConcurrentHashMap<>();

    public JsonNotificationPreferencesStore(Path file) { this.file = file; load(); }

    public NotificationPreferences get(UUID playerId) { return values.getOrDefault(playerId, NotificationPreferences.defaults()); }

    public synchronized void save(UUID playerId, NotificationPreferences preferences) {
        values.put(playerId, preferences);
        persist();
    }

    private void load() {
        if (!Files.exists(file) || isEmpty(file)) return;
        try {
            JsonObject root = new Gson().fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
            if (root == null || root.get("schema_version").getAsInt() != SCHEMA_VERSION) throw new IllegalStateException("Unsupported notification schema");
            JsonObject entries = root.getAsJsonObject("players");
            if (entries == null) return;
            entries.entrySet().forEach(entry -> {
                JsonObject json = entry.getValue().getAsJsonObject();
                values.put(UUID.fromString(entry.getKey()), new NotificationPreferences(
                        json.get("enabled").getAsBoolean(), Duration.ofSeconds(json.get("cooldown_seconds").getAsLong()),
                        json.get("hourly_limit").getAsInt(), LocalTime.parse(json.get("quiet_start").getAsString()),
                        LocalTime.parse(json.get("quiet_end").getAsString())));
            });
        } catch (Exception exception) { throw new IllegalStateException("Cannot load notification preferences: " + file, exception); }
    }

    private static boolean isEmpty(Path path) {
        try { return Files.size(path) == 0; }
        catch (IOException exception) { throw new IllegalStateException("Cannot inspect notification store: " + path, exception); }
    }

    private synchronized void persist() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("schema_version", SCHEMA_VERSION);
            JsonObject players = new JsonObject();
            values.forEach((id, preferences) -> {
                JsonObject json = new JsonObject();
                json.addProperty("enabled", preferences.proactiveEnabled());
                json.addProperty("cooldown_seconds", preferences.cooldown().toSeconds());
                json.addProperty("hourly_limit", preferences.maxNotificationsPerHour());
                json.addProperty("quiet_start", preferences.quietStart().toString());
                json.addProperty("quiet_end", preferences.quietEnd().toString());
                players.add(id.toString(), json);
            });
            root.add("players", players);
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) { throw new IllegalStateException("Cannot persist notification preferences: " + file, exception); }
    }
}
