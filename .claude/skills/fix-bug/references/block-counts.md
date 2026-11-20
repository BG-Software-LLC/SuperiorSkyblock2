# Block/entity count tracking (island worth, level, limits)

The second biggest bug class. Counting goes through `WorldRecordService.recordBlockPlace/recordBlockBreak(key, location, amount, [oldState], flags)`, fed by `listener/BlockChangesListener` and the Bukkit bridge `listener/BukkitEventsListener`.

## Root causes seen
- **Double counting.** One physical change reaches the plugin through two events:
  - `VehicleDestroyEvent` + `EntityRemoveFromWorldEvent`;
  - Paper's `BlockDestroyEvent` + others;
  - `EntityChangeBlockEvent` + `ProjectileHitEvent`;
  - a dragon egg (falling block + BlockFromTo).
- **Missed changes.** An event isn't listened to: `BlockFadeEvent`, golems/withers built from blocks, growth of chorus, bamboo or eyeblossom, a cactus broken by a neighbour.
- **Wrong key.**
  - `getBlockCountAsBigInteger` merges sub-types (spawner types). Limit checks need `getExactBlockCountAsBigInteger`.
  - Crop age is part of the key on 1.13+, so pass the *old* `BlockState` when a block changes.
  - Global vs specific keys in block values: store raw keys and normalise only on lookup.
- **Order of operations.**
  - Limits checked after the change was applied.
  - A spawner type changed by a spawn egg.
  - Off-hand placements skipping the limit check.
- **Chunk unload.** Recording despawns when chunks unload corrupted counts.

## Fix idioms
- Compute the old key with `Keys.of(block)`. Return early when `old.equals(new)` or the key is `ConstantKeys.AIR`.
- Pick **one** event source: Paper's event first, with the Bukkit fallback inside `catch (ClassNotFoundException)`.
- When one event records several changes, intermediate calls pass flags `0` and only the last passes `REGULAR_RECORD_FLAGS`, so the counts are saved once.
- Prefer deleting a redundant listener over layering de-duplication on top. Special-case patches for one mechanic at a time were later replaced by the general NMS tick tracker.
- The tick trackers (`world/BlockLevelTicksTracker`, `world/CollectingNeighborUpdaterTracker`) are **hand-written in every NMS module** (v1_18+/v1_19+). Every past fix to them needed a second round because a module was skipped. Patch all of them, and filter events that don't record block changes (`GameEvent.doesNotRecordBlockChange()`).
- Count entities in unloaded chunks from NBT (the key is `"Entities"`, not `"entities"`). Old-DataVersion NBT needs the DataFixer.
- Check `entity.isDead()` before counting a spawn: another plugin or a limit may already have removed it.
