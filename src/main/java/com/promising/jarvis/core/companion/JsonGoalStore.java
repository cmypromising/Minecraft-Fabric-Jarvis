package com.promising.jarvis.core.companion;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Versioned local JSON goal store with atomic replacement on writes. */
public final class JsonGoalStore implements GoalStore {
    private static final int SCHEMA_VERSION = 1;
    private final Path file;
    private final ConcurrentMap<UUID, ConcurrentMap<UUID, CompanionGoal>> goals = new ConcurrentHashMap<>();

    public JsonGoalStore(Path file) {
        this.file = file;
        load();
    }

    public synchronized CompanionGoal save(CompanionGoal goal) {
        goals.computeIfAbsent(goal.playerId(), ignored -> new ConcurrentHashMap<>()).put(goal.id(), goal);
        persist();
        return goal;
    }

    public List<CompanionGoal> findByPlayer(UUID playerId) {
        var playerGoals = goals.get(playerId);
        return playerGoals == null ? List.of() : List.copyOf(playerGoals.values());
    }

    public synchronized boolean remove(UUID playerId, UUID goalId) {
        var playerGoals = goals.get(playerId);
        if (playerGoals == null || playerGoals.remove(goalId) == null) return false;
        if (playerGoals.isEmpty()) goals.remove(playerId, playerGoals);
        persist();
        return true;
    }

    private void load() {
        if (!Files.exists(file) || isEmpty(file)) return;
        try {
            JsonObject root = new com.google.gson.Gson().fromJson(
                    Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
            if (root == null || root.get("schema_version").getAsInt() != SCHEMA_VERSION) {
                throw new IllegalStateException("Unsupported goal store schema");
            }
            JsonArray entries = root.getAsJsonArray("goals");
            if (entries == null) return;
            for (var element : entries) {
                JsonObject json = element.getAsJsonObject();
                CompanionGoal goal = new CompanionGoal(
                        UUID.fromString(json.get("id").getAsString()), UUID.fromString(json.get("player_id").getAsString()),
                        json.get("title").getAsString(), json.get("description").getAsString(),
                        GoalPriority.valueOf(json.get("priority").getAsString()), GoalStatus.valueOf(json.get("status").getAsString()),
                        nullable(json, "target_item_id"), json.get("target_count").getAsInt(), json.get("guidance_enabled").getAsBoolean(),
                        Instant.parse(json.get("created_at").getAsString()), Instant.parse(json.get("updated_at").getAsString()));
                goals.computeIfAbsent(goal.playerId(), ignored -> new ConcurrentHashMap<>()).put(goal.id(), goal);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load goal store: " + file, exception);
        }
    }

    private static boolean isEmpty(Path path) {
        try { return Files.size(path) == 0; }
        catch (IOException exception) { throw new IllegalStateException("Cannot inspect goal store: " + path, exception); }
    }

    private synchronized void persist() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("schema_version", SCHEMA_VERSION);
            JsonArray entries = new JsonArray();
            goals.values().forEach(playerGoals -> playerGoals.values().forEach(goal -> {
                JsonObject json = new JsonObject();
                json.addProperty("id", goal.id().toString());
                json.addProperty("player_id", goal.playerId().toString());
                json.addProperty("title", goal.title());
                json.addProperty("description", goal.description());
                json.addProperty("priority", goal.priority().name());
                json.addProperty("status", goal.status().name());
                if (goal.targetItemId() != null) json.addProperty("target_item_id", goal.targetItemId());
                json.addProperty("target_count", goal.targetCount());
                json.addProperty("guidance_enabled", goal.guidanceEnabled());
                json.addProperty("created_at", goal.createdAt().toString());
                json.addProperty("updated_at", goal.updatedAt().toString());
                entries.add(json);
            }));
            root.add("goals", entries);
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot persist goal store: " + file, exception);
        }
    }

    private static String nullable(JsonObject json, String field) {
        return json.has(field) && !json.get(field).isJsonNull() ? json.get(field).getAsString() : null;
    }
}
