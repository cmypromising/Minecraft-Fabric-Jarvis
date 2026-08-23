# M3 Memory Management

## Background

Jarvis now keeps bounded short-term conversation memory per player, but players cannot inspect or clear it. This is a privacy and usability gap.

## Goal

Expose safe player-scoped memory management commands without changing the `/nl` request flow.

## Scope

- Add `/nl-memory clear` to clear the current player's short-term memory.
- Add `/nl-memory status` to report the current player's retained turn count.
- Reject console execution and callers without the existing permission level.
- Keep memory data isolated by player UUID.

## Acceptance criteria

1. A player with permission level 2 can clear their own memory.
2. A player can inspect only their own memory count.
3. Console execution is rejected safely.
4. Clearing one player does not affect another player.
5. Existing `/nl` behavior remains unchanged.
6. Tests, build, and diff checks pass.
