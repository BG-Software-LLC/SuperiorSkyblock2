# Unloaded/lazy worlds, chunk loads, multi-world assumptions

Island worlds may be unloaded or created lazily (custom `WorldsProvider`s, SlimeWorldManager/ASP, `LazyWorldsProvider`).

## Root causes seen
- `location.getWorld()` is null.
- A call silently loads or creates a world: `getIslandsWorld(island, dim)`, `island.getCenter(dim)`, `getIslandHome(dim).getWorld()`.
- `block.getChunk()` in a delayed task loaded chunks forever.
- A cached `World`/`Chunk` of an unloaded world still looked valid, then threw.
- An island lookup refactor dropped the final `isInside` check.
- A world-info lookup ran before the null check on the island.

## Fix idioms
- To check or compare without loading:
  - `plugin.getGrid().getIslandsWorldInfo(island, dimension)` (name only) + `Bukkit.getWorld(info.getName()) != null`;
  - `island.getCenterPosition()`;
  - `LazyWorldLocation` from `ObjectsPools.LAZY_LOCATION`.
- When a real `World` is needed, split the method:
  ```java
  IslandWorlds.accessIslandWorldAsync(island, dimension /* or location */, loadWorld, result -> result
          .ifRight(Throwable::printStackTrace)
          .ifLeft(world -> doXWorldLoaded(..., world)));
  ```
  Pass `loadWorld=false` for maintenance or cosmetic passes that should skip unloaded worlds.
- Sync getters (`getIslandHome`, `getVisitorsLocation`, `IslandWarp#getLocation()`) throw `IllegalStateException` when the world isn't loaded (`IslandWorlds.setWorldToLocation`/`ensureWorldLoaded`). Use the `getLocation(LazyWorldLocation)` overloads or the async path.
- `LazyWorldLocation.clone(true)` keeps laziness; plain `clone()` doesn't. Clone before handing a location to an async callback.
- In delayed tasks, guard with `LazyWorldLocation.isWorldLoaded(world)` and `world.isChunkLoaded(x, z)` before touching blocks.
- Island lookups use the world **name** when custom world support is active (`hasCustomWorldsSupport()`), and always end with `island.isInside(...)`.
- Null-check nullable inputs *before* deriving anything from them.

## Multi-world / dimension assumptions
- Packets for one world sent to players in another (biome refresh kicked players): filter recipients by world (`IslandWorldsPlayersStrategy`).
- Per-chunk state keyed only by chunk x/z collides across dimensions: key it by world (`Chunk2ObjectMap`), and update *every* lookup of the map.
- Dimensions are configurable: use `Dimension.getByName`, `Dimensions.fromEnvironment` (nullable) and `getDefaultWorldDimension()`, never NORMAL/NETHER/THE_END constants. Portal routing was the riskiest area after dimensions became custom: check disabled worlds, non-island worlds, the spawn island and missing worlds.
