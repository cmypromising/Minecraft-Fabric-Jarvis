# NLP Command Help

## Goal

Make the intelligent command-line entry point discoverable for players by adding `/nlp` and `/nlp -help`.

## Acceptance criteria

1. `/nlp <request>` submits the request through the existing application service.
2. `/nlp -help` prints clear usage, examples, memory commands, and permission notes.
3. `/nlp` without an argument prints the same help.
4. Existing `/nl <request>` remains functional.
5. Console execution remains rejected.
6. Tests, build, and diff checks pass.
