package com.promising.jarvis.llm.prompt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Loads and validates versioned prompt contracts from packaged resources. */
public final class SystemPromptLoader {
    private static final String RESOURCE = "assets/jarvis/llm/system-prompts.json";
    private static final int SUPPORTED_SCHEMA_VERSION = 1;
    private static final PromptCatalog CATALOG = loadCatalog();

    private SystemPromptLoader() { }

    public static Optional<SystemPrompt> getPrompt(String key) {
        return Optional.ofNullable(CATALOG.prompts().get(key));
    }

    public static int schemaVersion() { return CATALOG.schemaVersion(); }

    private static PromptCatalog loadCatalog() {
        try (InputStream stream = Objects.requireNonNull(
                SystemPromptLoader.class.getClassLoader().getResourceAsStream(RESOURCE), RESOURCE);
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            if (root == null || !root.has("schema_version") || !root.has("prompts")) {
                throw new JsonParseException("Prompt catalog requires schema_version and prompts");
            }
            int schema = root.get("schema_version").getAsInt();
            if (schema != SUPPORTED_SCHEMA_VERSION) {
                throw new JsonParseException("Unsupported prompt schema version: " + schema);
            }
            Map<String, SystemPrompt> prompts = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : root.getAsJsonObject("prompts").entrySet()) {
                prompts.put(entry.getKey(), parsePrompt(entry.getKey(), entry.getValue().getAsJsonObject()));
            }
            if (prompts.isEmpty()) throw new JsonParseException("Prompt catalog cannot be empty");
            return new PromptCatalog(schema, Map.copyOf(prompts));
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid prompt catalog: " + RESOURCE, exception);
        }
    }

    private static SystemPrompt parsePrompt(String key, JsonObject object) {
        requireString(object, "version", key);
        requireString(object, "description", key);
        JsonArray messages = object.getAsJsonArray("messages");
        if (messages == null || messages.isEmpty()) throw new JsonParseException(key + ".messages is empty");
        List<Message> parsed = new ArrayList<>();
        for (var element : messages) {
            JsonObject message = element.getAsJsonObject();
            String role = requireString(message, "role", key);
            String content = requireString(message, "content", key);
            if (!role.equals("system")) throw new JsonParseException(key + " only supports system messages");
            parsed.add(new Message(role, content));
        }
        return new SystemPrompt(key, object.get("version").getAsString(), object.get("description").getAsString(), List.copyOf(parsed));
    }

    private static String requireString(JsonObject object, String field, String key) {
        if (!object.has(field) || object.get(field).getAsString().isBlank()) {
            throw new JsonParseException(key + "." + field + " is required");
        }
        return object.get(field).getAsString();
    }

    private record PromptCatalog(int schemaVersion, Map<String, SystemPrompt> prompts) { }

    public record Message(String role, String content) { }

    public record SystemPrompt(String key, String version, String description, List<Message> messages) {
        public String getRole() { return messages.getFirst().role(); }
        public String getContent() {
            return messages.stream().map(Message::content).reduce((a, b) -> a + "\n" + b).orElse("");
        }
    }
}
