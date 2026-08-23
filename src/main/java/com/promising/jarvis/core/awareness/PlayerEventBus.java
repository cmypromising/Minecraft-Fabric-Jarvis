package com.promising.jarvis.core.awareness;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-process event bus. Events are delivered synchronously on the server thread. */
public final class PlayerEventBus {
    private final List<PlayerEventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(PlayerEventListener listener) { listeners.add(listener); }
    public void publish(PlayerEvent event) { listeners.forEach(listener -> listener.onEvent(event)); }
}
