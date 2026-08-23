package com.promising.jarvis.core;

import com.promising.jarvis.core.application.DefaultJarvisApplicationService;
import com.promising.jarvis.core.application.JarvisApplicationService;
import com.promising.jarvis.core.agent.LlmAgent;
import com.promising.jarvis.core.agent.SingleThreadLlmAgent;
import com.promising.jarvis.core.context.ContextToolRegistries;
import com.promising.jarvis.core.capability.CapabilityRegistry;
import com.promising.jarvis.core.capability.impl.InformationalResponseCapability;
import com.promising.jarvis.core.capability.impl.MinecraftCommandCapability;
import com.promising.jarvis.core.parser.impl.DeepSeekParser;
import com.promising.jarvis.core.memory.InMemoryMemoryStore;
import com.promising.jarvis.core.memory.MemoryStore;
import com.promising.jarvis.core.companion.CompanionGoalService;
import com.promising.jarvis.core.companion.InMemoryGoalStore;
import com.promising.jarvis.core.companion.NotificationPolicy;
import com.promising.jarvis.core.companion.ProactiveCompanionService;
import com.promising.jarvis.core.observation.MinecraftPlayerStateObserver;
import com.promising.jarvis.core.companion.JsonGoalStore;
import com.promising.jarvis.core.companion.JsonNotificationPreferencesStore;

/** Composition root for Jarvis application services and capabilities. */
public final class JarvisRuntime {
    private static JarvisApplicationService applicationService;
    private static MemoryStore memoryStore;
    private static LlmAgent llmAgent;
    private static ProactiveCompanionService proactiveCompanion;
    private static CompanionGoalService goalService;

    private JarvisRuntime() {}

    public static void initialize() {
        CapabilityRegistry registry = new CapabilityRegistry()
                .register(new MinecraftCommandCapability())
                .register(new InformationalResponseCapability());
        memoryStore = new InMemoryMemoryStore(8);
        var dataDirectory = java.nio.file.Path.of("config", "jarvis");
        var goalStore = new JsonGoalStore(dataDirectory.resolve("goals.json"));
        goalService = new CompanionGoalService(goalStore);
        proactiveCompanion = new ProactiveCompanionService(goalStore, new MinecraftPlayerStateObserver(),
                new com.promising.jarvis.core.companion.RecommendationEngine(),
                new NotificationPolicy(java.time.Clock.systemUTC(),
                        new JsonNotificationPreferencesStore(dataDirectory.resolve("notification-preferences.json"))));
        llmAgent = new SingleThreadLlmAgent(new DeepSeekParser(), ContextToolRegistries.defaults());
        applicationService = new DefaultJarvisApplicationService(llmAgent, registry, memoryStore);
    }

    public static JarvisApplicationService applicationService() {
        if (applicationService == null) throw new IllegalStateException("Jarvis runtime is not initialized");
        return applicationService;
    }

    public static MemoryStore memoryStore() {
        if (memoryStore == null) throw new IllegalStateException("Jarvis runtime is not initialized");
        return memoryStore;
    }

    public static void shutdown() {
        if (llmAgent != null) llmAgent.close();
    }

    public static ProactiveCompanionService proactiveCompanion() {
        if (proactiveCompanion == null) throw new IllegalStateException("Jarvis runtime is not initialized");
        return proactiveCompanion;
    }

    public static CompanionGoalService goalService() {
        if (goalService == null) throw new IllegalStateException("Jarvis runtime is not initialized");
        return goalService;
    }
}
