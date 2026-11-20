# Investigating bugs you can't reproduce

"Potential fix" commits are guesses. Only half of them got a real follow-up under the same issue, and several were wrong the first time (a guessed cache eviction, then a revert, then the real fix: a lost sign on negative coordinates). When you can't confirm the cause, ship data-gathering together with, or instead of, the guess.

## Two instrumentation styles
1. **Permanent, filterable trace points.**
   - Add a constant to `core/logging/Debug.java`, ASCII-sorted among the regular constants; `SHOW_STACKTRACE` and `PROFILER` stay last.
   - Call `Log.debug(Debug.X, a, b, ...)` at entry and `Log.debugResult(Debug.X, "<stage>", value)` on each return path. The output is `<Class>::<method> {a} {b}`.
   - A server owner enables it with `/is admin debug <filter>`; `/is admin debug` alone turns it off. It is runtime only, and also written to `plugins/SuperiorSkyblock2/logs/`.
   - Sometimes data has to be restructured first to be loggable (per-chunk entity results).
2. **One-off probes for one report.** An always-on `Log.warn`/`Log.error` with the offending data, or more context in an existing exception or `Preconditions` message (e.g. adding chunk coordinates to "Chunk must be within the island" led straight to the fix).

## Rules
- **Probes log; they never throw.** Throwing access checks on `ChunkPosition` crashed servers and were removed. Two throwing probes are still in the code; don't copy them:
  - `StackedBlocksListener`: `throw new RuntimeException("BlockPlaceEvent with null hand item")`;
  - `SchematicBlock`: `"Detected tile-entity data with no 'id' key."`.
- Log arguments are concatenated varargs, not `{}`/`%s` formats.
- Commit subject: `Add debug messages to <symptom> (#issue)`. The fix commit may remove probes that are no longer needed.
- Read `git log --grep '#<issue>'` first: earlier probes or attempts for the same issue may already exist.
- Make error paths self-explaining instead of swallowing results: return a reason (`TeleportResult`) rather than a bare `false`.
- Prefer finding the mechanism over adding fixed delays or special cases; those band-aids were all later replaced.
