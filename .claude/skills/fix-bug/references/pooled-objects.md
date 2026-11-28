# Pooled object misuse

`core/ObjectsPools` hands out reused instances: `LOCATION`, `LAZY_LOCATION`, `DB_COLUMN`/`DB_COLUMN_BATCH`, `ChunkPosition`, NMS `BLOCK_POS_POOL`, click contexts, NPC players. The pooling refactor caused four follow-up bugs, and pooled `ChunkPosition` caused a six-commit chase.

## Root causes seen
- **Use after release.** The handle escaped its try-with-resources into a scheduled task, an async callback, event args read after the block, or a stored field.
- **A copy that was itself pooled.** `ChunkPosition.copy()` used to return a pooled instance; it doesn't anymore.
- **Double release from unclear ownership.** `ChunksProvider.loadChunk` and `WorldReader.prepareChunk` release the position they receive, and callers also released it with try-with-resources. The pool has no double-release guard, so a second release silently corrupts it. Ownership is documented nowhere except a `// Copy pool resource` comment.
- **Mutating a `Location` you don't own.** `createHologram(location.add(0.5, 1, 0.5))` moved a stacked block's stored location.
- **Shared static scratch.** A `static final Location` buffer was used from DB threads.
- **Wrong key type.** `map.remove(location)` instead of `map.remove(locationKey)`. It compiles because `Map.remove(Object)` takes anything.

## Fix idioms
- Use the handle only inside the block. Store `.clone()` for a `Location` or `copy()` for a `ChunkPosition` (not pooled).
- Release exactly once, on every path. Before passing a pooled object, read the callee to see whether it takes ownership.
- Copy into your own pooled handle before calling mutating methods.
- `SuperiorNPCPlayer` from `getSuperiorPlayer(Player)`: listeners call `release()` before returning and never use the object afterwards, because its NPC becomes null. Note that `release()` never refills the pool.
- DB column pairs come from `ObjectsPools.DB_COLUMN_BATCH` in try-with-resources. The SQL bridge copies them immediately; don't keep references.
