# Proactive Companion Roadmap

## Product definition

Jarvis is a Minecraft companion that helps a player reach goals through timely advice, current-state awareness, and safe assisted actions. It must not become an unsolicited command executor: every proactive action is observable, bounded, and controlled by player preferences.

## Current baseline

- `AgentTask` provides queued, prioritized, cancellable Agent work.
- `ContextToolRegistry` and the ReAct Agent provide on-demand read-only world context.
- `MemoryStore` provides short-term per-player conversation memory.
- `CapabilityRegistry` and `CommandSafetyPolicy` provide named response routing and command constraints.
- Missing: a stable player-state snapshot, goals, trigger evaluation, notification policy, and proactive lifecycle.

## Sub-requirements

### P1 — Player state and resources

Create a main-thread-safe observation boundary and immutable snapshot for player vitals, location, game rules, and inventory resource counts. Expose resource context through a read-only context tool. Do not send the complete inventory to the LLM unless requested.

Acceptance: snapshots are immutable; inventory is aggregated; observation never runs on the LLM thread; absent players are handled safely; tests cover empty, repeated, and bounded inventories.

### P2 — Companion goals

Represent a player's goal, status, priority, optional target item/count, and guidance preference. Support create, update, pause, complete, and remove without coupling goals to Minecraft commands.

### P3 — Trigger and recommendation engine

Evaluate periodic and state-change observations against goals and built-in rules. Produce recommendation candidates, never direct commands. Include evidence and reason for each candidate.

### P4 — Notification policy

Apply per-player opt-in, cooldown, deduplication, priority, quiet periods, and rate limits. Persist enough state to prevent repeated reminders after restart.

### P5 — Guidance and action loop

Let the LLM turn an accepted recommendation into explanation, question, or safe action. Require confirmation for consequential actions and reuse `CommandSafetyPolicy` for execution.

### P6 — Persistence and operations

Persist goals, preferences, notification history, and schema versions. Add status/clear controls and diagnostic metrics without exposing tokens or private data.

## Delivery order

P1 → P2 → P3 → P4 → P5 → P6. Each increment must pass tests, build, diff checks, and an independent safety review before merging to `dev`.
