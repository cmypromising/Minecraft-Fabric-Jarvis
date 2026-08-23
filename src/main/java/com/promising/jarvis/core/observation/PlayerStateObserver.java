package com.promising.jarvis.core.observation;

import net.minecraft.server.command.ServerCommandSource;

import java.util.Optional;

/** Main-thread observation boundary for player state. */
public interface PlayerStateObserver {
    Optional<PlayerStateSnapshot> observe(ServerCommandSource source);
}
