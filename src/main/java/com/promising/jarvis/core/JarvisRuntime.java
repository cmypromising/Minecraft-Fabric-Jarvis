package com.promising.jarvis.core;

import com.promising.jarvis.core.application.DefaultJarvisApplicationService;
import com.promising.jarvis.core.application.JarvisApplicationService;
import com.promising.jarvis.core.capability.CapabilityRegistry;
import com.promising.jarvis.core.capability.impl.InformationalResponseCapability;
import com.promising.jarvis.core.capability.impl.MinecraftCommandCapability;
import com.promising.jarvis.core.parser.impl.DeepSeekParser;
import com.promising.jarvis.core.memory.InMemoryMemoryStore;
import com.promising.jarvis.core.memory.MemoryStore;

/** Composition root for Jarvis application services and capabilities. */
public final class JarvisRuntime {
    private static JarvisApplicationService applicationService;

    private JarvisRuntime() {}

    public static void initialize() {
        CapabilityRegistry registry = new CapabilityRegistry()
                .register(new MinecraftCommandCapability())
                .register(new InformationalResponseCapability());
        MemoryStore memory = new InMemoryMemoryStore(8);
        applicationService = new DefaultJarvisApplicationService(new DeepSeekParser(), registry, memory);
    }

    public static JarvisApplicationService applicationService() {
        if (applicationService == null) throw new IllegalStateException("Jarvis runtime is not initialized");
        return applicationService;
    }
}
