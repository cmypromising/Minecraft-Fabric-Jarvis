package com.promising.jarvis.core.context;

import com.promising.jarvis.core.world.DifficultyComponent;
import com.promising.jarvis.core.world.LocationComponent;
import com.promising.jarvis.core.world.TimeWeatherComponent;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

/** Selects all optional player/world context through one deduplicated pipeline. */
public final class DynamicContextSelector {
    private static final int MAX_CONTEXT_LENGTH = 2000;
    private final List<ContextComponent> components;

    public DynamicContextSelector(List<ContextComponent> components) {
        this.components = List.copyOf(components);
    }

    public String collect(String request, ServerCommandSource source) {
        String normalized = request == null ? "" : request.toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder();
        Set<String> selectedValues = new HashSet<>();
        for (ContextComponent component : components) {
            if (!component.supports(normalized)) continue;
            String value = component.collect(source);
            if (value == null || value.isBlank()) continue;
            if (!selectedValues.add(value)) continue;
            if (result.length() > 0) result.append('\n');
            result.append(value);
            if (result.length() >= MAX_CONTEXT_LENGTH) break;
        }
        return result.length() > MAX_CONTEXT_LENGTH ? result.substring(0, MAX_CONTEXT_LENGTH) : result.toString();
    }

    public static DynamicContextSelector defaults() {
        return new DynamicContextSelector(List.of(
                new PlayerStatusComponent(),
                new TimeWeatherComponent(), new LocationComponent(), new DifficultyComponent()));
    }
}
