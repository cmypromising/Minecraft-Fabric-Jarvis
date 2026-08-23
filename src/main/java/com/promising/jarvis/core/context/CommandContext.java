package com.promising.jarvis.core.context;

import com.promising.jarvis.core.command.CommandRequest;
import net.minecraft.server.command.ServerCommandSource;

/** Execution context shared by all intelligent command-line capabilities. */
public record CommandContext(CommandRequest request, ServerCommandSource source, PlayerContext player, String selectedContext) {
    public static CommandContext from(CommandRequest request, ServerCommandSource source) {
        // ReAct tools are selected lazily by the LLM agent; no world snapshot is eagerly injected here.
        return new CommandContext(request, source, PlayerContext.from(source), "");
    }
}
