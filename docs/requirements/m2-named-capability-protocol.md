# M2 Named Capability Protocol

## Background

The command platform already has a `CapabilityRegistry`, but model responses are still routed only by numeric `type` values. This makes the protocol difficult to extend and couples every new capability to the original response categories.

## Goal

Introduce a stable, named capability identifier while keeping `type` as a backward-compatible fallback.

## Scope

- Add an optional `capability` field to the model response.
- Route known names `minecraft.command` and `minecraft.information`.
- Fall back to the existing `type` values when `capability` is absent.
- Reject unknown capability names without executing a command.
- Update the system prompt and automated tests.

## Non-goals

- No tool arguments or multi-step plans in this iteration.
- No removal of the legacy `type` field.
- No new Minecraft gameplay capability.

## Acceptance criteria

1. A response with `capability=minecraft.command` reaches the command capability.
2. A response with `capability=minecraft.information` reaches the information capability.
3. Legacy `type=1/2/3` responses remain supported.
4. Unknown capability names are rejected safely.
5. LLM instructions require strict JSON with the named capability field.
6. `gradlew test`, `gradlew build`, and `git diff --check` pass.

## Risk and rollback

The change is additive. Roll back the feature commit to restore numeric-only routing.
