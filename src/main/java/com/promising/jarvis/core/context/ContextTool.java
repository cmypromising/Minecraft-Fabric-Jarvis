package com.promising.jarvis.core.context;

import net.minecraft.server.command.ServerCommandSource;

/** MCP-style read-only context tool exposed to the ReAct LLM agent. */
public interface ContextTool {
    String name();
    String description();
    String execute(ServerCommandSource source, String arguments);
}
