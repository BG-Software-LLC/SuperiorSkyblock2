# Code style and project idioms

The house style and the project's own helpers. Prefer these over raw Bukkit/Java equivalents: they encode lessons from past bugs.

## Formatting and naming
- 4-space indent, same-line braces. A single-statement `if`/`for` may have no braces; follow the file you are in.
- Comments explain *why* at non-obvious places. Workarounds cite the issue URL (`// Patch for https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/2521`).
- Split helpers are named `fooInternal(...)` (thread hop, try/catch or validation wrapper around a public method).
- Class names:
  - commands: `CmdX` / `CmdAdminX`;
  - implementations: `XImpl` / `SIsland` / `SSuperiorPlayer`;
  - providers: `XProvider_Default` / `XProvider_<Plugin>`;
  - also `XListener`, `MenuX`, `XButton`.
- Utility classes have a private constructor. Case conversion always uses `Locale.ENGLISH`.

## Nullability and validation
- Use `@Nullable` from `com.bgsoftware.common.annotations.Nullable`, not `javax`. Put it on fields, parameters and returns, on its own line above version-optional constants.
- Public and API entry points validate first: `Preconditions.checkNotNull(x, "x parameter cannot be null.")` and `Preconditions.checkArgument(cond, "Message.")`.
- Return early (`if (island == null) return;`). Use explicit null or type checks, not `catch (NullPointerException | ClassCastException)` as control flow.

## Plugin handle and services
- Runtime classes (commands, listeners, menus, helpers) use `private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();`.
- Classes built during bootstrap get the plugin through their constructor instead: manager field initialisers, providers created in a constructor, anything initialised before `onLoad`. A static `getPlugin()` there is `null`.
- Never read `plugin.getSettings()` in a static or field initializer.
- Services are looked up lazily:
  ```java
  private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
      @Override
      protected PlaceholdersService create() {
          return plugin.getServices().getService(PlaceholdersService.class);
      }
  };
  ```
  `LazyReference` also defers anything that can't resolve during static init. It is not thread-safe: `create()` may run twice.

## Logging (`core/logging/Log`)
- Arguments are varargs that get **concatenated**, not format strings: `Log.warn("Schematic ", name, " is outside the island, skipping...")`.
- Errors: `Log.error(throwable, "An unexpected error occurred while <doing X>:")`.
- Bad config values: `Log.warnFromFile("config.yml", "Invalid value ", v, ", using ", def, " instead.")`, then skip and continue. Don't throw.
- Debug tracing:
  - Add a constant to `core/logging/Debug.java`, ASCII-sorted; `SHOW_STACKTRACE` and `PROFILER` stay last.
  - Call `Log.debug(Debug.X, args...)` at the start of a mutator and `Log.debugResult(Debug.X, "<stage>", value)` at outcomes.
  - Every new `SIsland`/`SSuperiorPlayer` setter gets its own constant.
  - Server owners turn tracing on with `/is admin debug <filter>`.
- Debug code logs and never throws.

## Threading (`core/threads/BukkitExecutor`)
- `sync(r[, ticks])`, `async(r[, ticks])`, `timer`, `asyncTimer`.
- `ensureMain(r)` runs `r` inline when already on the main thread, otherwise schedules it. Wrap any API entry point that touches the world and may be called async.
- `ensureAsync(r)` is the mirror. Never hand-write `if (Bukkit.isPrimaryThread()) { async(() -> sameMethod()); return; }`: during shutdown every `BukkitExecutor` call runs inline, so that recursed forever. Split into `x()` + `xInternal()` and call `ensureAsync(() -> xInternal())`.
- Run `CompletableFuture` callbacks that touch Bukkit with `future.whenCompleteAsync(cb, BukkitExecutor.SYNC_EXECUTOR)`. A plain `whenComplete` runs on the completing (async) thread.
- Shared mutable state:
  - `Synchronized.of(x)` with `read/readAndGet/write/writeAndGet`; its `get()` throws for wrapped `Map`s and `Collection`s;
  - `ThreadLocal` for scratch buffers;
  - `StampedLock` only for proven hot paths.

## Pooled objects (`core/ObjectsPool`, `core/ObjectsPools`)
Many paths run per tick or per event (move, block place/break, chunk load), so short-lived allocations add up. Bukkit's no-arg `getLocation()` on `Player`/`Entity`/`Block` allocates every call. Use the overload that fills an existing `Location` taken from the pool; the plugin's own types have it too (`SuperiorPlayer#getLocation(Location)`, `IslandWarp#getLocation(Location)`).
```java
Island island;
try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
    island = plugin.getGrid().getIslandAt(player.getLocation(wrapper.getHandle()));
}

try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
    databaseBridge.insertObject("players_missions",
            pool.obtain().withNameAndValue("player", uuid.toString()),
            pool.obtain().withNameAndValue("name", missionName));
}
```
- **A pooled object must not escape its try block.** Don't store it, return it, hand it to an async or scheduled task, or read it from event args after the block; once released, another caller overwrites it. Pull out what you need (the island, block coordinates, the world) inside the block, or `clone()` the `Location` to keep it.
- Never call `add`/`setX`/`setWorld` on a `Location` you don't own (a field, or one a caller passed in).
- Release exactly once and never use the object after `close()`; the pool has no double-release guard.
- `LAZY_LOCATION` is for worlds that may be unloaded. NMS code uses `NMSUtils.BLOCK_POS_POOL`.
- **Self-pooling classes** implement `ObjectsPool.Releasable` + `AutoCloseable` with a private static `POOL`: `ChunkPosition`, `ButtonClickContextImpl`, `WorldGeneratorOptionsImpl`, `SuperiorNPCPlayer`.
  - `ChunkPosition.of(...)` is pooled by default. Use it in try-with-resources, and store a `copy()` (or use the `fromPool = false` overload) when the position is kept, e.g. as a map key.
  - `ChunksProvider.loadChunk`/`WorldReader.prepareChunk` take ownership of their argument and release it, so don't release it yourself too.
- A pool for a new wrapped type: `ObjectsPools.createNewPool(() -> new Foo(...))` (as `NMSUtils.java.template` does for `BlockPos.MutableBlockPos`).

## Collections and helpers
- Maps and sets keyed by an `Enumerable` (privileges, flags, upgrades, dimensions) use `EnumerateMap<K,V>` / `EnumerateSet<K>` from `core.collections`.
  - They are array-backed by `ordinal()` (O(1), no hashing) and grow when new values are registered.
  - Construct them with every known value: `new EnumerateMap<>(IslandPrivilege.values())`.
- **`LinkedList` vs `ArrayList`:**
  - `LinkedList` is the codebase default for lists that are only appended to and then iterated, or used as a queue: tab-completion results, collected results, batches to process.
  - `ArrayList` is for lists read by index (`get(i)`, `set(i, ...)`, binary search, sort-then-index: top-islands lists, menu pages and slots, pattern rows) or whose size is known up front (`new ArrayList<>(size)`).
  - Never call `get(i)` in a loop on a `LinkedList`: that is O(n²).
- Block and entity keys: `Keys.of(...)`, `KeyMaps.create…`, `KeySets.create…`.
- Chunk-keyed maps: `Chunk2ObjectMap`, keyed by world as well as chunk.
- Filtered copies: `SequentialListBuilder<T>().filter(...).build(collection[, mapper])`.
- Return `map.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(map)`. Wrap config-derived collections as unmodifiable.
- **Hot paths** (listeners, island getters, per-tick code) avoid allocation:
  - no streams, which the maintainer keeps replacing with loops (streams are fine in commands, tab-complete and config loading);
  - hoist repeated getters into local variables;
  - use empty-array constants and non-vararg overloads.
- Text and formatting: `Text.isBlank`, `Formatters.NUMBER_FORMATTER / FANCY_NUMBER_FORMATTER / CAPITALIZED_FORMATTER / BOOLEAN_FORMATTER / COMMA_FORMATTER / COLOR_FORMATTER / DATE_FORMATTER`. Prefer JDK APIs over Guava internals, whose versions vary between servers.

## Version-optional game content
Never reference a `Material`/`EntityType`/enum constant that older servers lack. Resolve it by name:
```java
@Nullable
private static final Material TRIAL_SPAWNER = EnumHelper.getEnum(Material.class, "TRIAL_SPAWNER");
```
- Compare with `==`; on old servers the field is `null` and never matches. Register listeners or privileges only when the constant is non-null.
- Prefer a supertype or a name pattern (`instanceof AbstractHorse`, `name.endsWith("_CHEST")`) over a list of types; lists miss variants.
- Detect a capability by probing it (`Class.forName` in try/catch, `ReflectField.isValid()`, `X.class.isAssignableFrom(Y.class)`). Use `ServerVersion.isAtLeast(ServerVersion.v1_x)` when the version really is the condition.
