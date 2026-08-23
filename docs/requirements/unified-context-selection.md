# Unified Dynamic Context Selection

## Problem

`PlayerContext.asPromptText()` is always injected and `worldInfo()` is injected separately. Location, game mode, and difficulty can therefore be duplicated, while unrelated requests receive unnecessary player data.

## Goal

Use one component selector for all optional player and world context. The application service should receive one already-deduplicated prompt snapshot.

## Acceptance criteria

1. `CommandContext` contains one final optional context string instead of separate prompt fragments.
2. Player status is selected dynamically like world information.
3. Location, game mode, and difficulty are not duplicated.
4. Unrelated requests receive no optional player/world context.
5. Relevant requests receive only the matching component output.
6. The total optional context remains bounded.
7. Existing memory, command execution, tests, and build behavior remain intact.
