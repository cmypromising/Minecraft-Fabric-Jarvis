package com.promising.jarvis.core.world;

import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Selects and collects only request-relevant world components on the server thread. */
public final class WorldInfoSelector {
    private static final int MAX_CONTEXT_LENGTH = 2000;
    private final List<WorldInfoComponent> components;

    public WorldInfoSelector(List<WorldInfoComponent> components) {
        this.components = List.copyOf(components);
    }

    public String collect(String request, ServerCommandSource source) {
        String normalized = request == null ? "" : request.toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder();
        for (WorldInfoComponent component : components) {
            if (!component.supports(normalized)) continue;
            String info = component.collect(source);
            if (info == null || info.isBlank()) continue;
            if (result.length() > 0) result.append('\n');
            result.append(info);
            if (result.length() >= MAX_CONTEXT_LENGTH) break;
        }
        return result.length() > MAX_CONTEXT_LENGTH
                ? result.substring(0, MAX_CONTEXT_LENGTH)
                : result.toString();
    }

    public static WorldInfoSelector defaults() {
        List<WorldInfoComponent> components = new ArrayList<>();
        components.add(new TimeWeatherComponent());
        components.add(new LocationComponent());
        components.add(new DifficultyComponent());
        return new WorldInfoSelector(components);
    }
}
