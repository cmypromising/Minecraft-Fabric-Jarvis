# P1 Player State and Resource Observation

## Contract

`PlayerStateObserver` reads Minecraft state on the Minecraft server thread and returns an immutable `PlayerStateSnapshot`. The snapshot is the only state object passed across the Agent boundary.

The first version contains player identity, vitals, experience, location, dimension, difficulty, game mode, and aggregated inventory resources. Inventory entries are keyed by stable item identifiers and carry counts; empty slots are omitted.

## Safety and limits

- Read-only observation only.
- No API token, private prompt, or arbitrary NBT is included.
- Inventory entries are bounded to prevent oversized prompts.
- The LLM receives resource context only when a tool is selected.
