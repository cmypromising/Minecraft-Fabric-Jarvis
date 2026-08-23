package com.promising.jarvis.core.context;

import net.minecraft.server.command.ServerCommandSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Local, read-only tool registry; it follows MCP's discover-and-call shape without network exposure. */
public final class ContextToolRegistry {
    private final Map<String, ContextTool> tools = new LinkedHashMap<>();

    public ContextToolRegistry register(ContextTool tool) {
        if (tools.putIfAbsent(tool.name(), tool) != null) {
            throw new IllegalArgumentException("Duplicate context tool: " + tool.name());
        }
        return this;
    }

    public Optional<String> execute(String name, ServerCommandSource source, String arguments) {
        ContextTool tool = tools.get(name);
        if (tool == null) return Optional.empty();
        return Optional.ofNullable(tool.execute(source, arguments));
    }

    public String describe() {
        return tools.values().stream()
                .map(tool -> "- " + tool.name() + ": " + tool.description())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("(no context tools available)");
    }
}
