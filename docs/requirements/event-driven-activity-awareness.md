# Event-driven State Awareness and Player Activity Recognition

## Architecture

```text
Minecraft server thread
  └─ PlayerAwarenessService
       ├─ PlayerStateObserver
       ├─ PlayerStateChangeDetector
       └─ PlayerEventBus
             └─ PlayerActivityTracker
                    └─ player.activity ContextTool
```

State is sampled every 10 server ticks as a low-cost fallback, but downstream consumers receive semantic events only when meaningful fields change. The event bus is synchronous and server-thread confined; no event listener may perform network I/O or call the LLM directly.

## Event contract

Events include lifecycle, dimension, position, health, food, resource, death, respawn, and game-rule changes. Each event carries the previous/current immutable snapshot, evidence, player UUID, and timestamp.

## Activity contract

The tracker exposes activity, confidence, evidence, and observation time. Current deterministic classifications are:

- dimension change → exploration
- significant movement → travel
- resource increase → mining/collection tendency
- resource decrease → building/consumption tendency
- health loss, death, respawn → combat/danger tendency
- food change → inventory/survival management
- no meaningful event for two minutes → idle

The result is an inference, not a fact. The LLM must treat confidence and evidence as signals and request authoritative context tools when necessary.

## Extension rules

New Minecraft hooks should publish `PlayerEvent`; they should not modify the activity tracker directly. New activity types require evidence and confidence rules plus tests for false-positive boundaries. Event handling must remain bounded and must not turn every tick into an LLM request.
