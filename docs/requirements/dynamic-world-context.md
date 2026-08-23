# Dynamic World Context

## Goal

Provide request-relevant Minecraft world facts to the LLM so answers rely on current game state instead of guesses.

## Scope

- Add an extensible local `ContextTool` contract and registry.
- Add initial read-only tools for time/weather, location/dimension, difficulty/game mode, and player status.
- Let the ReAct LLM Agent select tools only when the request needs a live fact.
- Keep world inspection on the Minecraft thread and cap tool iterations.
- Do not eagerly inject a world snapshot when no tool is requested.

## Acceptance criteria

1. A weather/time request can call only the time/weather tool.
2. A location/dimension request can call the location tool.
3. A difficulty/game-mode request can call the world-rules tool.
4. An unrelated request does not require a context tool.
5. Tool calls are read-only, main-thread safe, and bounded by the task's ReAct limit.
6. Existing player context, memory, `/nlp`, and command safety behavior remain intact.
