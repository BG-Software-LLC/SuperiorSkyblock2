# API changes, Island/SuperiorPlayer methods, persistence

Exemplars:
- `git show 60fb983d7`: a new Island method with Debug, DB bridge and event.
- `git show 4c8541c7f`: an existing method gains a parameter (deprecate and delegate).

## Compatibility rules
- **Never** remove or rename a public API class, method or enum constant. Never change a parameter list or return type, even for a same-named method. Never change what an existing method observably does.
- To extend: add the new method, then mark the old one `@Deprecated` with javadoc `@deprecated See {@link #newMethod(...)}`, and have it delegate to the new one with the old default. It must keep working.
  ```java
  @Deprecated
  Biome getBiome();                 // old: delegates to getBiome(defaultDimension)
  Biome getBiome(Dimension dimension);
  ```
- **How you add a method depends on whether the interface has a `Delegate*` class.**
  - **Interfaces with a `Delegate*` class.** Third parties extend the `Delegate*` class, which wraps a `handle` and forwards every call, so a new method may be abstract.
    - In the same commit, add the forwarding override to the `Delegate*` class (`return this.handle.newMethod(...);`) and implement the method in every concrete plugin class.
    - The pairs: `Island`/`DelegateIsland`, `SuperiorPlayer`/`DelegateSuperiorPlayer`, `IslandBank`, `IslandWarp`, `WarpCategory`, `IslandChest`, `IslandPreview`, `PermissionNode`, `BankTransaction`, `PersistentDataContainer`, the factories (`Islands/Players/Banks/DatabaseBridgeFactory`), the algorithms (`PlayerTeleport`, `IslandCreation`, `IslandCalculation`, `IslandBlocksTracker`, `IslandEntitiesTracker`), and the containers (`IslandsContainer`, `PlayersContainer`).
  - **Interfaces only the plugin implements** (every `api/handlers/*Manager`, `SettingsManager`, `api/service/*`): new abstract methods are fine.
  - **Interfaces without a `Delegate*` class that third parties implement**: every `api/hooks/*Provider` (`WorldsProvider`, `LazyWorldsProvider`, `MenusProvider`, `EconomyProvider`, …) and `hooks/listener/I*Listener`.
    - A new method must be **`default`**, so existing implementations still load.
    - The default must work for an old implementation. Either the new method calls the old abstract one (`WorldsProvider.getIslandsWorld(Island, Environment)` delegates to the `Dimension` overload), or it throws `UnsupportedOperationException` **and every plugin call site catches it**. Once the catch was forgotten, and the plugin crashed with providers that didn't implement the method.
  - **Abstract classes third parties extend** (`PluginModule`, `Mission`): add concrete, non-abstract methods only.
  - Never turn an old provider method into a default that calls a *new abstract* one: old implementations then fail with `AbstractMethodError`. The repo did this to `WorldsProvider` and `LazyWorldsProvider`, and added abstract methods to `MenusProvider` several times; don't use those as precedent.
- Javadoc on every new API method (`@param`, `@return`; "Check whether…" phrasing).
- Unreleased API (added since the last "API version N" commit) may still be moved.
- New value types: an API interface with a nested `Builder` and a `static newBuilder()` that delegates to `SuperiorSkyblockAPI.getFactory()`, implemented in `FactoriesManagerImpl` plus `*Impl` classes.

## New `Island` method: four classes in one commit
1. `API/src/main/java/com/bgsoftware/superiorskyblock/api/island/Island.java`: the method with javadoc.
2. `API/.../api/island/DelegateIsland.java`: `return this.handle.x(...);`.
3. `src/main/java/com/bgsoftware/superiorskyblock/island/SIsland.java`:
   ```java
   @Override
   public void setX(Foo foo) {
       Preconditions.checkNotNull(foo, "foo parameter cannot be null.");
       Log.debug(Debug.SET_X, owner.getName(), foo);   // new Debug constant; logged before the no-op check
       if (Objects.equals(this.foo, foo)) return;
       this.foo = foo;
       IslandsDatabaseBridge.saveX(this);               // core/database/bridge/
       plugin.getMenus().refreshX(this);                // only if a menu shows it
   }
   ```
   Fire the API event (see `events.md`) before mutating. A getter that computes something logs with `Log.debugResult`.
4. `island/SpawnIsland.java`: `// Do nothing.` or a neutral constant. It never throws.

## New `SuperiorPlayer` method
`API/.../api/wrappers/SuperiorPlayer.java` (note: `wrappers`) + `API/.../api/player/DelegateSuperiorPlayer.java` + `player/SSuperiorPlayer.java` + `player/SuperiorNPCPlayer.java`. The NPC returns a neutral value or does nothing; it throws `UnsupportedOperationException` only when the call makes no sense for an NPC.

## Persistence
- Static helpers in `core/database/bridge/IslandsDatabaseBridge` / `PlayersDatabaseBridge`, wrapped in `runOperationIfRunning(...)`, with column pairs from `ObjectsPools.DB_COLUMN_BATCH`.
- Pick the right table: `updateIslandValue` (`islands`, filter `uuid`) or `updateIslandSettingsValue` (`islands_settings`, filter `island`). Sizes, limits and multipliers live in `islands_settings`. Writing one to the wrong table was a real bug.
- A new table keyed by island must also be cleared in `deleteIsland`.
- Schema or data changes for existing databases: a `core/database/loader/sql/upgrade/vN/DatabaseUpgrade_VN implements Runnable` singleton, appended to `SQLDatabase.DATABASE_UPGRADES`. Editing `CREATE TABLE` only affects new databases.
- Loaders must not drop unknown privileges or flags: an addon may register them later.
