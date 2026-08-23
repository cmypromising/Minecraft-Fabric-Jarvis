package com.promising.jarvis.core.world;

import net.minecraft.server.command.ServerCommandSource;

/** Selectively exposes one category of current world facts to the LLM. */
public interface WorldInfoComponent {
    String id();
    boolean supports(String request);
    String collect(ServerCommandSource source);
}
