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
        return capabilities.stream().filter(capability -> capability.supports(response)).findFirst()
                .map(capability -> { capability.execute(context, response); return true; }).orElse(false);
    }

    public List<String> ids() { return capabilities.stream().map(Capability::id).toList(); }
}
