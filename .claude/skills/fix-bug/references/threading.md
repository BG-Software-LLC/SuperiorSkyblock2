# Wrong thread, races, latches

## Root causes seen
- `CompletableFuture.whenComplete` callbacks run on whichever thread completed the future, often async.
- Chunk-load futures complete off the main thread.
- Third-party plugins call API methods async.
- A refactor moved a statement onto an async path.
- Something became parallel, and shared static scratch state turned into a race: parallel schematic loading exposed a static `HashMap` and a static varint buffer.
- A concurrent map was swapped for a non-thread-safe one in code that a hook calls async.

## Fix idioms (`core/threads/BukkitExecutor`)
- World access in an API entry point that may be called async: `BukkitExecutor.ensureMain(() -> xInternal(...))`.
- Future callbacks that touch Bukkit: `future.whenCompleteAsync(cb, BukkitExecutor.SYNC_EXECUTOR)` (or `ASYNC_EXECUTOR`).
- Heavy work: `BukkitExecutor.ensureAsync(() -> xInternal(...))`.
  - **Never** hand-write `if (Bukkit.isPrimaryThread()) { async(() -> sameMethod()); return; }`. During shutdown (`PREPARE_SHUTDOWN`) every `BukkitExecutor` call runs inline on the caller's thread, so that pattern recursed forever; seven call sites were fixed. `CoreProtectHook` still has the sync-direction version.
  - Code that must never run inline at shutdown uses the Bukkit scheduler directly (`HologramMenuWrapper`).
- Collect data on the main thread, process it async, apply the result on the main thread.
- Shared state:
  - `Synchronized.of(x)` with `read/readAndGet/write/writeAndGet`; `get()` throws for wrapped Maps and Collections;
  - `ConcurrentHashMap`;
  - `ThreadLocal` for scratch buffers and per-thread capture state;
  - an `AtomicLong` for cross-thread counters.
- `LazyReference` is not thread-safe (`create()` may run twice).
- **Latches and counters:** every path counts down, including success, "chunk doesn't exist" early returns, error callbacks and catch blocks.
  - Decrement in `finally`.
  - Null-guard a latch that exists only on some paths: `latchCountDown() { if (latch != null) latch.countDown(); }`.
  - Each new latch or counter in the chunk-loading code needed a follow-up fix, so enumerate the paths before shipping.
- Inside a delayed task, re-check state (`chunk.isLoaded()`, `player.isOnline()`). Fixed tick delays used to "wait for" another action were replaced by explicit callbacks.
- Scheduled tasks tied to an island are tracked and cancelled on disband.
