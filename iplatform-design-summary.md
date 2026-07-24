# IPlatform: Decoupling `core/` from Bukkit

## Context

`core/` (391 files) currently has 165 files (42%) directly importing `org.bukkit.*` — static `Bukkit.*` calls, `extends BukkitRunnable`/`Location`, `implements InventoryHolder`, raw `ItemStack`/`Material`/`World` usage. This blocks ever supporting a non-Bukkit server platform and spreads platform coupling deep into supposedly-generic logic (scheduling, world resolution, event firing, GUI rendering).

The user already started scaffolding this: `platform/IPlatform.java` exists as an empty stub interface (`notifyGameEvent(...)`, zero implementations, zero call sites) and `platform/event/*` (GameEventType, GameEventsDispatcher, GameEventPriority) is already Bukkit-agnostic at the type level, feeding off raw Bukkit `@EventHandler`s in `listener/BukkitEventsListener.java`. This plan builds `IPlatform` out from that stub into a full facade with sub-managers, covers the entire full scope requested (including the ~150-file GUI/menu subsystem and the necessary additive API change), and lands it in 4 independently-shippable phases.

Chosen scope/design decisions (confirmed with user):
- **Full purge**, including GUI/menu subsystem, not just scheduler/event/world plumbing.
- **Sub-manager interfaces** under `IPlatform` (matches this codebase's existing Manager pattern), not one flat interface.
- **Build on the existing `GameEventsDispatcher`** system rather than replacing it.

## Package layout

```
platform/
  IPlatform.java              # top-level facade — keep at existing path, keep existing notifyGameEvent method
  scheduler/  IScheduler.java, ITask.java
  world/      IWorldManager.java, IWorld.java
  server/     IServerManager.java
  ui/         IUIManager.java, IInventoryView.java, IClickType.java, InventoryKind.java
  event/                       # UNCHANGED — already Bukkit-agnostic (GameEventType, GameEventsDispatcher, ...)

platform/bukkit/               # NEW — sole Bukkit-impl package, sits above NMS facades
  BukkitPlatform.java
  scheduler/BukkitScheduler.java
  world/BukkitWorldManager.java
  server/BukkitServerManager.java
  ui/BukkitUIManager.java
  item/ItemBuilder.java        # relocated from core/itemstack/ (Phase 4)
```

No new Gradle subproject/module split — package-level separation only, enforced by a grep-based build check (see Verification). Module-splitting is a valid future follow-up once `core/` is clean, not part of this effort.

## Sub-manager designs

**`IScheduler`** (replaces `core/threads/BukkitExecutor`, `core/task/CalcTask extends BukkitRunnable`):
```java
public interface IScheduler {
    boolean isMainThread();
    @Nullable ITask runSync(Runnable task);
    @Nullable ITask runSync(Runnable task, long delayTicks);
    @Nullable ITask runAsync(Runnable task);
    @Nullable ITask runAsync(Runnable task, long delayTicks);
    @Nullable ITask runSyncTimer(Runnable task, long delayTicks, long periodTicks);
    @Nullable ITask runAsyncTimer(Runnable task, long delayTicks, long periodTicks);
    ITask ensureMain(Runnable task);
    ITask ensureAsync(Runnable task);
    <T> NestedTask<T> createTask();
    void prepareShutdown();
    void shutdown();
}
public interface ITask { void cancel(); boolean isCancelled(); }
```
`BukkitExecutor` is kept as a thin static shim delegating to `plugin.getPlatform().getScheduler()` during migration (it has ~116 call sites outside `core/` too) rather than deleted in one shot; deleted only once all call sites migrate.

**`IWorldManager`/`IWorld`** (replaces scattered `Bukkit.getWorld(name)` in `LazyWorldLocation`, `ChunkPosition`, `IslandWorlds`, `IslandWorldsPlayersStrategy`, `SBlockPosition`, `SWorldPosition`):
```java
public interface IWorldManager {
    @Nullable IWorld getWorld(WorldInfo worldInfo);
    @Nullable IWorld getWorld(String worldName);
    void loadWorldAsync(WorldInfo worldInfo, Consumer<IWorld> callback);
    boolean isChunkLoaded(WorldInfo worldInfo, int chunkX, int chunkZ);
}
public interface IWorld {
    String getName();
    Dimension getEnvironment();
    List<SuperiorPlayer> getPlayers();
    boolean isChunkLoaded(int chunkX, int chunkZ);
    void loadChunkAsync(int chunkX, int chunkZ, Runnable callback);
    org.bukkit.World asBukkitWorld();   // deliberate, explicit escape hatch
}
```
**Design tradeoff (deliberate, not full purge):** `Island`/`SuperiorPlayer` and the rest of the *published API* return raw `Location`/`World`/`Chunk`/`Block` everywhere (confirmed — e.g. `Island.getCenter()`). Introducing `IWorld`/`ILocation` types that fully replace Bukkit types inside `core/`'s resolution classes would just create wrap/unwrap churn at every one of the hundreds of call sites handing a position to an API method — without the API boundary itself changing, that's not real decoupling. So `IWorldManager` decouples the *resolution* layer (no more scattered `Bukkit.getWorld()`), and `IWorld.asBukkitWorld()` is the single, greppable, explicit seam back to the unchanged API surface. This turns "165 files with scattered Bukkit calls" into "N call sites using an explicit, intentional escape hatch" — honest partial decoupling, not fake full decoupling.

**`IServerManager`** (replaces `Bukkit.shutdown()`, `Bukkit.getOnlinePlayers()`, `Bukkit.getConsoleSender()`, `Bukkit.dispatchCommand()`):
```java
public interface IServerManager {
    void shutdown();
    int getOnlinePlayersCount();
    List<SuperiorPlayer> getOnlinePlayers();
    void dispatchConsoleCommand(String command);
    boolean isPlayerOnline(UUID uuid);
}
```

**Event firing** — no new sub-manager; extend `IPlatform` directly (it's already the single cross-cutting entry point every `GameEventType` capture funnels through, and there's exactly one companion method needed):
```java
public interface IPlatform {
    IScheduler getScheduler();
    IWorldManager getWorldManager();
    IServerManager getServerManager();
    IUIManager getUIManager();
    <Args extends IEventArgs> void notifyGameEvent(GameEvent<Args> gameEvent, GameEventPriority priority); // existing stub, now implemented
    boolean fireBukkitEvent(org.bukkit.event.Event event, boolean cancellable); // NEW
}
```
`fireBukkitEvent` replaces the single `Bukkit.getPluginManager().callEvent(...)` call in `core/events/plugin/PluginEventsDispatcher.fireEvent` — this is the cleanest seam in the codebase (1 call site). `BukkitPlatform.fireBukkitEvent` internally preserves the existing `api.platform.IEventsDispatcher` override point (already-published Folia-style extensibility hook, set via `plugin.setEventsDispatcher(...)`) exactly as `BukkitEventsListener` does today, then falls back to `Bukkit.getPluginManager().callEvent(...)`. `PluginEventType`'s ~100 `createBukkitEvent` overrides and `PluginEventArgs`'s ~90 Bukkit-typed DTOs are **not** touched — they mirror the already-published, Bukkit-typed `api.events.*` classes; changing those is a separate, much larger API-breaking effort and explicitly out of scope.

`notifyGameEvent` finally gets a real implementation: `BukkitEventsListener`'s existing two call sites (`plugin.getGameEventsDispatcher().onGameEvent(gameEvent, priority)` at lines 862 and 928) get rewired through `plugin.getPlatform().notifyGameEvent(...)`, with `BukkitPlatform.notifyGameEvent` calling `getGameEventsDispatcher().onGameEvent(...)` internally.

**`IUIManager`** (replaces `UIProvider.createInventory`, direct `player.openInventory`/`closeInventory` in `AbstractMenuView`, raw event reads in `MenusListener`):
```java
public interface IUIManager {
    IInventoryView createInventory(Object platformHolderToken, int size, String title);
    IInventoryView createInventory(Object platformHolderToken, InventoryKind kind, String title);
    void openInventory(SuperiorPlayer player, IInventoryView view);
    void closeInventory(SuperiorPlayer player);
    boolean isViewingInventory(SuperiorPlayer player, IInventoryView view);
}
public interface IInventoryView {
    int getSize();
    void setItem(int slot, IItemStack item);
    @Nullable IItemStack getItem(int slot);
    List<IItemStack> getContents();
}
```
Builds on the **existing** `api.menu.button.click.ButtonClickContext<V>`/`ButtonClickContextImpl` (already converts both `InventoryClickEvent` and `GameEventArgs.DialogClickEvent` into one context type) rather than inventing a parallel type — extended so `getClickType()` returns a new small `IClickType` enum instead of raw `org.bukkit.event.inventory.ClickType`. `GameEventArgs.InventoryClickEvent`/`InventoryCloseEvent` gain **additive** derived fields (`rawSlot`, `clickType`, `whoClicked: SuperiorPlayer`, `clickedInventoryHolder: Object`) populated by `BukkitEventsListener` at capture time, alongside the existing raw `bukkitEvent` field (kept, not removed, so nothing else reading it mid-migration breaks). `MenusListener` is rewritten to use only the new fields.

`core/itemstack/ItemBuilder.java` (~30 fluent methods, inherently version/render-specific, already partially routes through `NMSAlgorithms`/`UIProvider`) is **relocated wholesale** to `platform/bukkit/item/ItemBuilder.java`, not abstracted behind a new `IItemBuilder` interface — there's no second implementation to design for, and it would be speculative generality. A minimal `IItemStack` opaque-handle type (with `asBukkitItemStack()` escape hatch, same pattern as `IWorld`) covers the few places `IInventoryView.setItem/getItem` need to pass an item without importing `org.bukkit.inventory.ItemStack`.

`core/key/Keys.of(Material|EntityType|Block|ItemStack)` (25 files, already Bukkit-agnostic past its static-factory input boundary) is **left as-is** — a well-contained, already-isolated input boundary; wrapping it behind `IPlatform` adds an indirection step without buying additional decoupling.

## How `core/` obtains `IPlatform`

**Instance getter on `SuperiorSkyblockPlugin`**, matching the codebase's dominant existing convention (`plugin.getProviders()`, `plugin.getNMSAlgorithms()`, `plugin.getPluginEventsDispatcher()`, `plugin.getGameEventsDispatcher()` — all instance fields, ~10x more common than `BukkitExecutor`'s static-singleton-with-`init()` pattern):
```java
private final IPlatform platform = new BukkitPlatform(this);
public IPlatform getPlatform() { return platform; }
```
Consumed the same way every other manager already is: `private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin(); ... plugin.getPlatform().getScheduler().runSync(...)`.

## Migration phasing (4 independently-mergeable phases, smallest blast radius first)

1. **Scheduler + Server** — add `IScheduler`/`ITask`/`IServerManager`/`BukkitPlatform` skeleton, wire `SuperiorSkyblockPlugin.getPlatform()`, turn `BukkitExecutor` into a delegating shim (zero other-file changes needed), migrate `CalcTask` off `BukkitRunnable`, migrate `ManagerLoadException.handle()`/`StatsPlayersCounter`. Representative files: `core/threads/BukkitExecutor.java`, `core/task/CalcTask.java`, `core/errors/ManagerLoadException.java`.
2. **World/position resolution** — add `IWorldManager`/`IWorld`/`BukkitWorldManager`, migrate `LazyWorldLocation`, `ChunkPosition`, `IslandWorlds`, `IslandWorldsPlayersStrategy`, `SBlockPosition`, `SWorldPosition` to resolve through it, using `asBukkitWorld()` at each handoff to an `Island`/API caller. Contained to these 6 known classes.
3. **Event bus firing** — add `IPlatform.fireBukkitEvent(...)`, change the single `Bukkit.getPluginManager().callEvent(...)` line in `PluginEventsDispatcher.fireEvent`, rewire `BukkitEventsListener`'s 2 `onGameEvent` call sites through `notifyGameEvent`. Sequenced after Phase 2 so `BukkitPlatform`'s shape is stable first.
4. **UI/inventory + items** (largest, last) — add `IUIManager`/`IInventoryView`/`IClickType`, extend `GameEventArgs` inventory event DTOs additively, rewrite `MenusListener` off raw `.bukkitEvent` reads, relocate `ItemBuilder` to `platform/bukkit/item/`, route `RegularInventoryMenuLayoutImpl`/`AbstractMenuView` open/close through `IUIManager`, land the additive `MenuView` API change (below). Representative files: `listener/MenusListener.java`, `core/menu/view/AbstractMenuView.java`, `core/menu/button/click/ButtonClickContextImpl.java`, `core/itemstack/ItemBuilder.java`.

## API module change (additive, backwards-compatible)

`api/menu/view/MenuView.java` currently `extends org.bukkit.inventory.InventoryHolder` directly — cannot be removed per CLAUDE.md's API rules (third-party plugins do `inventory.getHolder() instanceof MenuView`, same pattern `MenusListener` itself uses). Fix: add a new marker supertype, keep `MenuView`'s existing hierarchy unchanged:
```java
public interface IMenuHandle {
    SuperiorPlayer getInventoryViewer();
    Menu<?, ?> getMenu();
}
public interface MenuView<V extends MenuView<V, A>, A extends ViewArgs>
        extends InventoryHolder, IMenuHandle {   // gains IMenuHandle, everything else unchanged
    // ... existing methods, unchanged
}
```
No other published API leakage requires changing — `api.platform.IEventsDispatcher` (Bukkit-typed, published) stays as-is per the "build on existing" decision; `WorldInfo`/`Dimension`/`Key`/`SuperiorPlayer` are already Bukkit-agnostic and become the core-side vocabulary as-is.

## Explicit non-goals (recorded so they aren't rediscovered mid-implementation)

- `api/events/*` (~90 published event classes) and `PluginEventArgs`/`PluginEventType`'s Bukkit-typed fields — untouched.
- `Island`/`SuperiorPlayer`'s raw `Location`/`World`/`Chunk`/`Block` API methods — untouched; `IWorld.asBukkitWorld()` is the deliberate seam.
- `core/key/Keys.of(...)` static-factory boundary — untouched.
- `NMSAlgorithms`/`NMSDialogs`/`NMSWorld`/etc. — untouched, orthogonal cross-*version* (not cross-platform) facades that `BukkitPlatform` sits above.
- `api/hooks/*` provider SPIs — untouched, intentional Bukkit adapters to external plugins.
- `ItemBuilder` is relocated, not abstracted behind a generic interface — no second implementation being designed for.
- No new Gradle subproject for `platform/bukkit/` in this effort.

## Verification (no automated test suite exists — per CLAUDE.md, manual + structural)

1. **Grep-based import-boundary Gradle task**, same mechanism as the existing `checkDebug` task: fail build if `core/**/*.java` (excluding relocated/allowlisted files) imports `org.bukkit.*`. Track the count down from the current baseline (165/391 files) phase by phase; bypass via `-x checkImportBoundary` like `-x checkDebug`.
2. **Compile-per-phase**: `gradle build -x checkDebug` after each phase — most of this refactor is type-directed, so compilation catches signature mismatches immediately.
3. **Manual smoke test on a running Paper/Spigot server** after each phase: Phase 1 → island recalculation task still fires, shutdown still clean; Phase 2 → island teleport/load across dimensions (normal/nether/end); Phase 3 → a plugin event listener (e.g. `IslandCreateEvent`) still fires and is cancellable; Phase 4 → open every menu category (island menu, admin menu, biome picker, stacked-blocks deposit menu) and click through.
4. **Escape-hatch audit** after Phase 4: grep for `.asBukkitWorld()`/`.asBukkitItemStack()` call sites as the honest final decoupling metric (not "zero Bukkit imports," since the escape hatches are deliberate).
