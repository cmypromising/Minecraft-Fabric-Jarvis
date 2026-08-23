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
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Versioned durable store for proactive deduplication and cooldown history. */
public final class JsonNotificationHistoryStore implements NotificationHistoryStore {
    private static final int SCHEMA_VERSION = 1;
    private final Path file;
    private final Map<String, NotificationHistory> values = new ConcurrentHashMap<>();

    public JsonNotificationHistoryStore(Path file) { this.file = file; load(); }
    public Optional<NotificationHistory> find(String key) { return Optional.ofNullable(values.get(key)); }

    public synchronized void save(String key, NotificationHistory history) {
        values.put(key, history);
        persist();
    }

    private void load() {
        if (!Files.exists(file) || isEmpty(file)) return;
        try {
            JsonObject root = new Gson().fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
            if (root == null || root.get("schema_version").getAsInt() != SCHEMA_VERSION) throw new IllegalStateException("Unsupported notification history schema");
            JsonObject entries = root.getAsJsonObject("entries");
            if (entries == null) return;
            entries.entrySet().forEach(entry -> {
                JsonObject json = entry.getValue().getAsJsonObject();
                values.put(entry.getKey(), new NotificationHistory(Instant.parse(json.get("sent_at").getAsString()),
                        json.get("hourly_bucket").getAsString()));
            });
        } catch (Exception exception) { throw new IllegalStateException("Cannot load notification history: " + file, exception); }
    }

    private synchronized void persist() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("schema_version", SCHEMA_VERSION);
            JsonObject entries = new JsonObject();
            values.forEach((key, history) -> {
                JsonObject json = new JsonObject();
                json.addProperty("sent_at", history.sentAt().toString());
                json.addProperty("hourly_bucket", history.hourlyBucket());
                entries.add(key, json);
            });
            root.add("entries", entries);
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
            try { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException exception) { throw new IllegalStateException("Cannot persist notification history: " + file, exception); }
    }

    private static boolean isEmpty(Path path) {
        try { return Files.size(path) == 0; }
        catch (IOException exception) { throw new IllegalStateException("Cannot inspect notification history: " + path, exception); }
    }
}
