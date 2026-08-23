package com.promising.jarvis.core.capability;

import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;

import java.util.ArrayList;
import java.util.List;

/** Ordered registry for future command-line services. */
public final class CapabilityRegistry {
    private final List<Capability> capabilities = new ArrayList<>();

    public CapabilityRegistry register(Capability capability) {
        capabilities.add(capability);
        return this;
    }

    public boolean dispatch(CommandContext context, ContentResponseBody response) {
        boolean namedResponse = response != null && response.getCapability() != null && !response.getCapability().isBlank();
        return capabilities.stream().filter(capability -> {
                    if (namedResponse) return capability.id().equals(response.getCapability());
                    return capability.supports(response);
                }).findFirst()
                .map(capability -> { capability.execute(context, response); return true; }).orElse(false);
    }

    public List<String> ids() { return capabilities.stream().map(Capability::id).toList(); }
}
