package com.promising.jarvis.core.context;

import com.promising.jarvis.core.command.CommandRequest;
import net.minecraft.server.command.ServerCommandSource;
import com.promising.jarvis.core.world.WorldInfoSelector;

/** Execution context shared by all intelligent command-line capabilities. */
public record CommandContext(CommandRequest request, ServerCommandSource source, PlayerContext player, String worldInfo) {
    public static CommandContext from(CommandRequest request, ServerCommandSource source) {
        String worldInfo = WorldInfoSelector.defaults().collect(request.text(), source);
        return new CommandContext(request, source, PlayerContext.from(source), worldInfo);
    }
}
