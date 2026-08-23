package com.promising.jarvis.core.context;

import net.minecraft.server.command.ServerCommandSource;

/** A request-selected, bounded piece of optional LLM context. */
public interface ContextComponent {
    String id();
    boolean supports(String request);
    String collect(ServerCommandSource source);
}
