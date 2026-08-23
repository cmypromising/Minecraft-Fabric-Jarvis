# Dynamic World Context

## Goal

Provide request-relevant Minecraft world facts to the LLM so answers rely on current game state instead of guesses.

## Scope

- Add an extensible `WorldInfoComponent` contract.
- Add initial components for time/weather, location/dimension, and difficulty/game mode.
- Select components from the player's request before the asynchronous LLM call.
- Keep world inspection on the Minecraft thread and cap injected context length.
- Inject no extra world information when no component matches.

## Acceptance criteria

1. A weather/time request receives only time/weather context.
2. A location/dimension request receives location context.
3. A difficulty/game-mode request receives difficulty context.
4. An unrelated request receives no additional world context.
5. Context is bounded and component output cannot exceed the configured budget.
6. Existing player context, memory, `/nlp`, and command safety behavior remain intact.
