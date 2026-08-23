package com.promising.jarvis.core.awareness;

@FunctionalInterface
public interface PlayerEventListener {
    void onEvent(PlayerEvent event);
}
