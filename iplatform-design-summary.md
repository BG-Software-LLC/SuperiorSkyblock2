# IPlatform: Decoupling `core/` from Bukkit

## Context

`core/` (391 files) currently has 165 files (42%) directly importing `org.bukkit.*` — static `Bukkit.*` calls, `extends BukkitRunnable`/`Location`, `implements InventoryHolder`, raw `ItemStack`/`Material`/`World` usage. This blocks ever supporting a non-Bukkit server platform and spreads platform coupling deep into supposedly-generic logic (scheduling, world resolution, event firing, GUI rendering, block/item identity).

A second, related coupling exists alongside raw Bukkit imports: 9 `NMS*` facade interfaces (`NMSAlgorithms`, `NMSChunks`, `NMSDialogs`, `NMSDragonFight`, `NMSEntities`, `NMSHolograms`, `NMSPlayers`, `NMSTags`, `NMSWorld`, generated per-version via the NMS Template System) are called directly via `plugin.getNMSXxx()` from ~25 files outside `nms/` — `core/`, `island/`, `listener/`, `world/`, `service/`, `tag/`, `commands/`, `module/`. These are cross-*version* facades, not cross-*platform* ones (their signatures are already Bukkit-typed, e.g. `NMSWorld.setBlock(Location, int)`), so this isn't the same "imports `org.bukkit.*`" problem — it's an access-path problem: every one of those ~25 files reaches around `IPlatform` straight to a version-specific singleton getter. **Rule: nothing outside `platform/bukkit/` calls `plugin.getNMSXxx()` directly.** Each `NMS*` facade becomes an internal implementation detail composed *by* one `BukkitPlatform` sub-manager, and `core/`/`island/`/`listener/`/etc. call the sub-manager instead. The `NMS*` interfaces themselves, their per-version implementations, and the template/properties generation system are untouched — only who is allowed to call them changes.

The user already started scaffolding this: `platform/IPlatform.java` exists as an empty stub interface (`notifyGameEvent(...)`, zero implementations, zero call sites) and `platform/event/*` (GameEventType, GameEventsDispatcher, GameEventPriority) is already Bukkit-agnostic at the type level, feeding off raw Bukkit `@EventHandler`s in `listener/BukkitEventsListener.java`. This plan builds `IPlatform` out from that stub into a full facade with sub-managers, covers the entire full scope requested (including the ~150-file GUI/menu subsystem, block/item identity, and the necessary additive API change), and lands it in 5 independently-shippable phases.

Chosen scope/design decisions (confirmed with user):
- **Full purge**, including GUI/menu subsystem and block/item identity, not just scheduler/event/world plumbing.
- **Sub-manager interfaces** under `IPlatform` (matches this codebase's existing Manager pattern), not one flat interface. This now also covers event firing (`IEventsManager`) — no method lives directly on `IPlatform` itself.
- **Build on the existing `GameEventsDispatcher`** system rather than replacing it.
- **Minimize wrapper types.** Where an internal object is only ever produced by one platform sub-manager and handed back into another platform sub-manager call, it is returned as opaque `Object` (a "handle") instead of a purpose-built interface. Operations on that handle live as methods on the owning manager, not on the object itself. This eliminated `ITask`, `IWorld`, `IInventoryView`, and `IItemStack` as interfaces entirely — see per-manager sections below.
- **Return types audited against real call sites, not assumed.** Every method below that returns a value was checked against actual usage in the current codebase; methods whose return value is never consulted anywhere return `void`.
- **Duplicated Bukkit enums stay plain enums**, not `I`-prefixed interfaces (`ClickType`, not `IClickType`) — these are fixed, finite, version-independent vocabularies, unlike `IslandPrivilege`/`IslandFlag`, which use the API's `Enumerable` pattern because third-party plugins must be able to register new values.
- **No direct `NMS*` facade calls outside `platform/bukkit/`.** Every `plugin.getNMSXxx()` call site outside `nms/` and `platform/bukkit/` moves onto the `IPlatform` sub-manager that owns that concern; the sub-manager's Bukkit implementation composes the existing `NMS*` facade internally. See "NMS facade redistribution" below.

## Package layout

```
platform/
  IPlatform.java              # top-level facade — keep at existing path
  scheduler/  IScheduler.java                        # ITask removed — see below
  world/      IWorldManager.java                     # IWorld removed — see below
  server/     IServerManager.java
  ui/         IUIManager.java, InventoryKind.java, ClickType.java   # IInventoryView/IItemStack removed
  registry/   IBlocksRegistry.java, IItemsRegistry.java             # NEW — replaces Material
  tags/       ITagsManager.java                       # NEW — replaces NMSTags (generic NBT access)
  event/      IEventsManager.java                    # NEW — event methods moved off IPlatform
              GameEventType.java, GameEventsDispatcher.java, ...    # unchanged

platform/bukkit/               # sole Bukkit-impl package, sits above NMS facades — only package allowed to call plugin.getNMSXxx()
  BukkitPlatform.java
  scheduler/BukkitScheduler.java
  world/BukkitWorldManager.java        # composes NMSWorld, NMSChunks, NMSDragonFight, NMSEntities
  server/BukkitServerManager.java      # composes NMSAlgorithms (server-stats subset), NMSPlayers (identity subset)
  ui/BukkitUIManager.java              # composes NMSDialogs, NMSAlgorithms (createMenuInventoryHolder), NMSPlayers (HUD subset)
  registry/BukkitBlocksRegistry.java, BukkitItemsRegistry.java      # composes NMSAlgorithms (block-data/item-meta subsets)
  tags/BukkitTagsManager.java          # NEW — composes NMSTags
  event/BukkitEventsManager.java                     # NEW
  item/ItemBuilder.java        # relocated from core/itemstack/ (Phase 4)
```

`NMSHolograms` needs no redistribution — its only callers today are `SuperiorSkyblockPlugin` (bootstrap) and `service/hologram/HologramsServiceImpl`, i.e. it's already fully isolated behind the existing `HologramsService`. Not part of this effort.

No new Gradle subproject/module split — package-level separation only, enforced by a grep-based build check (see Verification). Module-splitting is a valid future follow-up once `core/` is clean, not part of this effort.

## Sub-manager designs

### `IScheduler`

Replaces `core/threads/BukkitExecutor`, `core/task/CalcTask extends BukkitRunnable`, `service/bossbar/BossBarTask extends BukkitRunnable`.

**Return-value audit:** grepped every `BukkitExecutor.sync/async/ensureMain/ensureAsync(...)` call site (109 total). None assign the result with `=`, but many pass it straight into a `registerTask(...)`-style helper that stores it and later calls `.cancel()` — e.g. `SIsland.registerTask` (invite revoke timeout, bank interest task, `activeTasks` set), `SSuperiorPlayer`/`SuperiorNPCPlayer` teleport-task cancellation, `RegionManagerServiceImpl` cancelling a teleport warmup on move. So the task handle **is** used — `runSync`/`runAsync`/`ensureMain`/`ensureAsync` keep non-void returns. `BukkitExecutor.asyncTimer`/`timer` are currently `void` and their single call site (`MissionsManagerImpl`, periodic mission save) never needs to cancel — but `CalcTask` and `BossBarTask`, both in scope for this migration, self-cancel their own repeating timer today via a captured `BukkitTask`. Once they no longer `extend BukkitRunnable`, they need that handle from the scheduler itself, so `runSyncTimer`/`runAsyncTimer` also return a handle. Checked `ITask.isCancelled()` (the previous design's second method): zero call sites anywhere in the codebase query a task's cancelled state — only `event.isCancelled()` (an unrelated Bukkit event, not a task) shows up in a search for `.isCancelled()`. Dropped.

Given the "return an opaque handle, put behavior on the manager" rule, `ITask` is not worth keeping as a one-method (`cancel()`) wrapper interface — replaced by `Object` handles plus `IScheduler.cancelTask(...)`, which also lets `cancelTask` absorb the null check every current call site duplicates (`if (task != null) task.cancel();`).

```java
public interface IScheduler {
    boolean isMainThread();
    @Nullable Object runSync(Runnable task);
    @Nullable Object runSync(Runnable task, long delayTicks);
    @Nullable Object runAsync(Runnable task);
    @Nullable Object runAsync(Runnable task, long delayTicks);
    @Nullable Object runSyncTimer(Runnable task, long delayTicks, long periodTicks);
    @Nullable Object runAsyncTimer(Runnable task, long delayTicks, long periodTicks);
    @Nullable Object ensureMain(Runnable task);   // was non-nullable in the original draft — wrong, BukkitExecutor.ensureMain returns null on 2 branches
    @Nullable Object ensureAsync(Runnable task);
    void cancelTask(@Nullable Object task);       // replaces ITask.cancel(); no-ops on null
    <T> NestedTask<T> createTask();
    void prepareShutdown();
    void shutdown();
}
```

`BukkitExecutor` is kept as a thin static shim delegating to `plugin.getPlatform().getScheduler()` during migration (it has ~116 call sites outside `core/` too) rather than deleted in one shot; deleted only once all call sites migrate. `BukkitScheduler`'s handles are just the raw `org.bukkit.scheduler.BukkitTask` boxed as `Object` — zero wrapper allocation.

### `IWorldManager`

Replaces scattered `Bukkit.getWorld(name)` in `LazyWorldLocation`, `ChunkPosition`, `IslandWorlds`, `IslandWorldsPlayersStrategy`, `SBlockPosition`, `SWorldPosition`.

**Design tradeoff (deliberate, not full purge):** `Island`/`SuperiorPlayer` and the rest of the *published API* return raw `Location`/`World`/`Chunk`/`Block` everywhere (confirmed — e.g. `Island.getCenter()`). Since the API boundary isn't changing, a typed `IWorld` interface would just be a second name for "the object you're about to unwrap and hand to an API method" — exactly the wrapper-for-every-object problem, and every one of `IWorld`'s methods (`getName`, `getEnvironment`, `getPlayers`, `isChunkLoaded`, `loadChunkAsync`) is called on an object obtained from, and eventually handed back to, `IWorldManager`/an API method. So `IWorld` is dropped: `getWorld(...)` returns an opaque `Object` world handle, and every operation that used to be an `IWorld` instance method moves onto `IWorldManager`, keyed by that handle. `asBukkitWorld(Object)` stays as an explicit method (not a bare cast at each call site) so the escape hatch remains the single, greppable seam the original design valued.

```java
public interface IWorldManager {
    @Nullable Object getWorld(WorldInfo worldInfo);       // opaque world handle
    @Nullable Object getWorld(String worldName);
    void loadWorldAsync(WorldInfo worldInfo, Consumer<Object> callback);
    boolean isChunkLoaded(WorldInfo worldInfo, int chunkX, int chunkZ);   // resolve-and-check fast path
    boolean isChunkLoaded(Object world, int chunkX, int chunkZ);          // handle already in hand
    void loadChunkAsync(Object world, int chunkX, int chunkZ, Runnable callback);
    String getWorldName(Object world);
    Dimension getWorldEnvironment(Object world);
    List<SuperiorPlayer> getWorldPlayers(Object world);
    org.bukkit.World asBukkitWorld(Object world);   // deliberate, explicit escape hatch

    // absorbed from NMSWorld (block/physical-world ops — same signatures, new access path)
    boolean canPlayerSuffocate(org.bukkit.block.Block block);
    boolean canPlayerSuffocate(org.bukkit.ChunkSnapshot chunkSnapshot, int x, int y, int z);
    void listenSpawner(org.bukkit.Location location, java.util.function.IntFunction<Integer> delayChangeCallback);
    void setWorldBorder(SuperiorPlayer superiorPlayer, Island island);
    void setBlock(org.bukkit.Location location, int combinedId);
    boolean isWaterLogged(org.bukkit.block.Block block);
    int getDefaultAmount(org.bukkit.block.Block block);
    int getDefaultAmount(org.bukkit.block.BlockState blockState);
    void placeSign(Island island, org.bukkit.Location location);
    String parseSignLine(String original);                     // absorbed from NMSAlgorithms — always used alongside placeSign
    void playGeneratorSound(org.bukkit.Location location);
    void playBreakAnimation(org.bukkit.block.Block block);
    void playPlaceSound(org.bukkit.Location location);
    int getMinHeight(org.bukkit.World world);
    void removeAntiXray(org.bukkit.World world);
    void setOceanLevel(org.bukkit.World world);
    void listenBlockStateChanges(org.bukkit.World world);
    IslandsGenerator createGenerator(Dimension dimension);
    WorldEditSession createEditSession(org.bukkit.World world);
    WorldEditSession createPartialEditSession(Dimension dimension);
    ChunkReader createChunkReader(org.bukkit.Chunk chunk);

    // absorbed from NMSChunks (chunk ops)
    void setBiome(List<ChunkPosition> chunkPositions, org.bukkit.block.Biome biome, Collection<SuperiorPlayer> playersToUpdate);
    void deleteChunks(Island island, List<ChunkPosition> chunkPositions, @Nullable Runnable onFinish);
    java.util.concurrent.CompletableFuture<List<CalculatedChunk.Blocks>> calculateChunks(List<ChunkPosition> chunkPositions, Synchronized<Chunk2ObjectMap<CalculatedChunk.Blocks>> unloadedChunksCache);
    java.util.concurrent.CompletableFuture<List<CalculatedChunk.Entities>> calculateChunkEntities(Collection<ChunkPosition> chunkPositions);
    void injectChunkSections(org.bukkit.Chunk chunk);
    boolean isChunkEmpty(org.bukkit.Chunk chunk);
    @Nullable org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition);
    void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop);
    void updateCropsTicker(List<ChunkPosition> chunkPositions, double newCropGrowthMultiplier);
    List<org.bukkit.Location> getBlockEntities(org.bukkit.Chunk chunk);

    // absorbed from NMSDragonFight (end-dimension mechanic, scoped per world)
    void prepareEndWorld(org.bukkit.World bukkitWorld);
    void startDragonBattle(Island island, org.bukkit.Location location);
    void removeDragonBattle(Island island, Dimension dimension);
    void awardTheEndAchievement(org.bukkit.entity.Player player);

    // absorbed from NMSEntities (entity/world interaction rules)
    boolean isAnimalFood(org.bukkit.inventory.ItemStack itemStack, org.bukkit.entity.Animals animals);
    boolean isMinecartFuel(org.bukkit.inventory.ItemStack itemStack, org.bukkit.entity.minecart.PoweredMinecart minecart);
    int getPortalTicks(org.bukkit.entity.Entity entity);

    // absorbed from NMSAlgorithms (biome vocabulary — queried alongside setBiome/getWorldEnvironment)
    org.bukkit.block.Biome getBiome(String biomeName);
    NMSAlgorithms.EnumBridge<org.bukkit.block.Biome> getBiomeBridge();
}
```

`BukkitWorldManager`'s world/chunk handles are the raw `org.bukkit.World` boxed as `Object`; internally it composes `NMSWorld`, `NMSChunks`, `NMSDragonFight`, and `NMSEntities` — the real call sites currently reaching around `IPlatform` for these are `ChunksListener`, `SIsland`, `GridManagerImpl`, `service/portals/PortalsManagerServiceImpl`, `service/region/RegionManagerServiceImpl`, `listener/PortalsListener`, `module/upgrades/type/UpgradeTypeEntityLimits`, `listener/BlockChangesListener`, `island/IslandUtils`, `world/WorldGenerator`, `world/schematic/*`. `service/dragon/DragonBattleServiceImpl` already only talks to `NMSDragonFight` (properly isolated today) and simply gets repointed at `getWorldManager()` like everything else — no behavior change there, just access path.

This makes `IWorldManager` the largest sub-manager by a wide margin — deliberate, not scope creep: it mirrors how much of the *existing* 165-file Bukkit coupling in `core/` is already about world/block/chunk/entity simulation, and the codebase already tolerates large single managers (`GridManagerImpl` itself). Splitting world/chunk/dragon-fight/entity-rules into four separate one-or-two-method sub-managers would be the wrapper-proliferation problem in a different shape.

### `IServerManager`

Replaces `Bukkit.shutdown()`, `Bukkit.getOnlinePlayers()`, `Bukkit.getConsoleSender()`, `Bukkit.dispatchCommand()`. Also absorbs the server-stats/registration subset of `NMSAlgorithms` and the per-player-identity subset of `NMSPlayers` — neither is world, UI, or scheduling, and both are naturally "facts/actions about a player or the server," the same granularity as the existing `isPlayerOnline`/`getOnlinePlayers` methods. Real callers reaching around `IPlatform` for these today: `commands/CommandTabCompletes`, `commands/CommandsManagerImpl`, `core/profiler/ProfilerSession` (`getCurrentTps`), `service/region/RegionManagerServiceImpl`, `player/inventory/ClearActions`, `listener/PlayersListener`.

```java
public interface IServerManager {
    void shutdown();
    int getOnlinePlayersCount();
    List<SuperiorPlayer> getOnlinePlayers();
    void dispatchConsoleCommand(String command);
    boolean isPlayerOnline(UUID uuid);

    // absorbed from NMSAlgorithms (server-level, not per-world/per-item)
    void registerCommand(org.bukkit.command.defaults.BukkitCommand command);
    double getCurrentTps();
    int getDataVersion();
    int getMaxWorldSize();

    // absorbed from NMSPlayers (per-player identity/session, not HUD rendering — see IUIManager)
    void setSkinTexture(SuperiorPlayer superiorPlayer);
    void setSkinTexture(SuperiorPlayer superiorPlayer, com.mojang.authlib.properties.Property property);
    boolean wasThrownByPlayer(org.bukkit.entity.Item item, SuperiorPlayer superiorPlayer);
    Object createOfflinePlayerData(org.bukkit.OfflinePlayer offlinePlayer);   // opaque handle — internal NMS type, never re-typed by callers
    @Nullable java.util.Locale getPlayerLocale(SuperiorPlayer superiorPlayer);
}
```

`createOfflinePlayerData` returns `Object` rather than the internal `nms.player.OfflinePlayerData` type for the same reason `ITask`/world/inventory handles do: callers only ever pass it back into other `NMSPlayers`-adjacent calls, never inspect it directly.

### `IEventsManager` (moved off `IPlatform`)

The original draft put event firing directly on `IPlatform` on the theory that there was "exactly one companion method needed." With `IUIManager`/`IWorldManager`/etc. all being real sub-managers, keeping 1–2 methods bare on the facade is the odd one out — moved into its own manager under `platform/event/`, alongside the already-Bukkit-agnostic `GameEventType`/`GameEventsDispatcher`/`GameEventPriority`.

```java
public interface IEventsManager {
    <Args extends IEventArgs> void notifyGameEvent(GameEvent<Args> gameEvent, GameEventPriority priority);
    void fireBukkitEvent(org.bukkit.event.Event event, boolean cancellable);   // was `boolean` in the original draft
}
```

`fireBukkitEvent`'s return type was audited too: the sole real call site, `PluginEventsDispatcher.fireEvent`, calls `Bukkit.getPluginManager().callEvent(bukkitEvent)` (itself `void` in the Bukkit API) and then re-reads cancellation straight off the same `bukkitEvent` reference it already holds (`((Cancellable) bukkitEvent).isCancelled()`, then `type.applyBukkitToPluginEvent(bukkitEvent, event)`) — Java passes the event by reference, so nothing needs a returned boolean. `void` is correct.

`fireBukkitEvent` replaces the single `Bukkit.getPluginManager().callEvent(...)` call in `core/events/plugin/PluginEventsDispatcher.fireEvent` — this is the cleanest seam in the codebase (1 call site). `BukkitEventsManager.fireBukkitEvent` internally preserves the existing `api.platform.IEventsDispatcher` override point (already-published Folia-style extensibility hook, set via `plugin.setEventsDispatcher(...)`) exactly as `BukkitEventsListener` does today, then falls back to `Bukkit.getPluginManager().callEvent(...)`. `PluginEventType`'s ~100 `createBukkitEvent` overrides and `PluginEventArgs`'s ~90 Bukkit-typed DTOs are **not** touched — they mirror the already-published, Bukkit-typed `api.events.*` classes; changing those is a separate, much larger API-breaking effort and explicitly out of scope.

`notifyGameEvent` finally gets a real implementation: `BukkitEventsListener`'s existing two call sites (`plugin.getGameEventsDispatcher().onGameEvent(gameEvent, priority)` at lines 862 and 928) get rewired through `plugin.getPlatform().getEventsManager().notifyGameEvent(...)`, with `BukkitEventsManager.notifyGameEvent` calling `getGameEventsDispatcher().onGameEvent(...)` internally.

### `IUIManager`

Replaces `UIProvider.createInventory`, direct `player.openInventory`/`closeInventory` in `AbstractMenuView`, raw event reads in `MenusListener`.

Same handle-over-wrapper reasoning as `IWorldManager`: the previous draft's `IInventoryView` (`getSize`/`setItem`/`getItem`/`getContents`) and `IItemStack` (a one-method opaque type whose only job was `asBukkitItemStack()`) are both objects created by one manager call and immediately handed to another — `createInventory(...)` → `openInventory(...)`/`setItem(...)`. Both are dropped; `IUIManager` returns and accepts `Object` handles directly, with all read/write operations as manager methods. Item handles from `ItemBuilder` (still relocated wholesale, still not abstracted behind a second implementation — see non-goals) are just the raw `org.bukkit.inventory.ItemStack` it already produces, boxed as `Object`; `asBukkitItemStack` is the one explicit, greppable escape hatch back to it.

```java
public interface IUIManager {
    Object createInventory(Object platformHolderToken, int size, String title);          // opaque inventory handle
    Object createInventory(Object platformHolderToken, InventoryKind kind, String title);
    void openInventory(SuperiorPlayer player, Object inventoryView);
    void closeInventory(SuperiorPlayer player);
    boolean isViewingInventory(SuperiorPlayer player, Object inventoryView);
    int getInventorySize(Object inventoryView);
    void setInventoryItem(Object inventoryView, int slot, @Nullable Object item);
    @Nullable Object getInventoryItem(Object inventoryView, int slot);
    List<Object> getInventoryContents(Object inventoryView);
    org.bukkit.inventory.ItemStack asBukkitItemStack(@Nullable Object item);   // deliberate, explicit escape hatch

    // absorbed from NMSDialogs (dialog UI — same domain as inventory menus)
    void openDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog);
    void closeDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog);

    // absorbed from NMSPlayers (HUD rendering, not player identity — see IServerManager)
    void sendActionBar(SuperiorPlayer superiorPlayer, String message);
    void sendTitle(SuperiorPlayer superiorPlayer, String title, String subtitle, int fadeIn, int duration, int fadeOut);
    BossBar createBossBar(SuperiorPlayer superiorPlayer, String message, BossBar.Color color, BossBar.Style style, double ticksToRun);
}
```

`createInventory(...)`'s `platformHolderToken: Object` parameter is exactly what `NMSAlgorithms.createMenuInventoryHolder(InventoryType, InventoryHolder, String)` already produces today — `BukkitUIManager.createInventory` composes that call internally, confirming the handle design rather than inventing a new one. `NMSDialogs`'s real callers reaching around `IPlatform` today: `core/menu/MenuCommandsImpl`, `core/menu/layout/RegularDialogMenuLayoutImpl`, `core/menu/view/AbstractMenuView` (already in scope for Phase 4 regardless), `listener/BukkitEventsListener`. `NMSPlayers.sendActionBar`/`sendTitle`/`createBossBar`'s real callers are `external/ui/UIProvider_Default` and `external/bossbar/BossBarProvider_Default` — both HUD-rendering providers, confirming this is a UI concern rather than a per-player-identity one. `createBossBar` keeps returning the already-published, already-Bukkit-agnostic `api.service.bossbar.BossBar` type — it isn't a new wrapper, so the handle-over-wrapper rule doesn't apply to it.

Builds on the **existing** `api.menu.button.click.ButtonClickContext<V>`/`ButtonClickContextImpl` (already converts both `InventoryClickEvent` and `GameEventArgs.DialogClickEvent` into one context type) rather than inventing a parallel type — extended so `getClickType()` returns a new `ClickType` enum. This is a **plain enum**, duplicated from `org.bukkit.event.inventory.ClickType`, not an `IClickType` interface: it's a closed, version-independent set of click kinds with no third-party extensibility need, unlike `IslandPrivilege`/`IslandFlag`'s `Enumerable` pattern. `GameEventArgs.InventoryClickEvent`/`InventoryCloseEvent` gain **additive** derived fields (`rawSlot`, `clickType: ClickType`, `whoClicked: SuperiorPlayer`, `clickedInventoryHolder: Object`) populated by `BukkitEventsListener` at capture time, alongside the existing raw `bukkitEvent` field (kept, not removed, so nothing else reading it mid-migration breaks). `MenusListener` is rewritten to use only the new fields.

`core/itemstack/ItemBuilder.java` (~30 fluent methods, inherently version/render-specific, already partially routes through `NMSAlgorithms`/`UIProvider`) is **relocated wholesale** to `platform/bukkit/item/ItemBuilder.java`, not abstracted behind a new `IItemBuilder` interface — there's no second implementation to design for, and it would be speculative generality.

`core/key/Keys.of(Material|EntityType|Block|ItemStack)` (25 files, already Bukkit-agnostic past its static-factory input boundary) is **left as-is** — see `IBlocksRegistry`/`IItemsRegistry` below for why this isn't the same seam.

### `IBlocksRegistry` / `IItemsRegistry` (NEW)

Replaces `org.bukkit.Material` as the vocabulary `platform/` and new/migrating call sites use for block and item identity — 23 files in `core/` and 56 across the plugin currently import `org.bukkit.Material` directly.

```java
public interface IBlocksRegistry {
    @Nullable Object getBlockType(String key);     // opaque BlockType handle
    String getBlockTypeKey(Object blockType);
    boolean isValidBlockType(String key);
    boolean isSolid(Object blockType);
    boolean isAir(Object blockType);
    Collection<Object> getAllBlockTypes();
    org.bukkit.Material asBukkitMaterial(Object blockType);   // deliberate, explicit escape hatch

    // absorbed from NMSAlgorithms (block-data-value subset — already Keys.java/MaterialKey.java's dependency)
    int getCombinedId(org.bukkit.Location location);
    int getCombinedId(Object blockType, byte data);
    java.util.Optional<String> getTileEntityIdFromCombinedId(int combinedId);
    Key getBlockKey(int combinedId);
    short getBlockDataValue(org.bukkit.block.BlockState blockState);
    short getBlockDataValue(org.bukkit.block.Block block);
    short getMaxBlockDataValue(Object blockType);
    Key getMinecartBlock(org.bukkit.entity.Minecart minecart);
    Key getFallingBlockType(org.bukkit.entity.FallingBlock fallingBlock);
}
public interface IItemsRegistry {
    @Nullable Object getItemType(String key);      // opaque ItemType handle
    String getItemTypeKey(Object itemType);
    boolean isValidItemType(String key);
    int getMaxStackSize(Object itemType);
    Collection<Object> getAllItemTypes();
    org.bukkit.Material asBukkitMaterial(Object itemType);    // deliberate, explicit escape hatch

    // absorbed from NMSAlgorithms (item-meta mutation subset — already ItemBuilder's dependency)
    int compareMaterials(Object itemTypeA, Object itemTypeB);
    void setCustomModel(org.bukkit.inventory.meta.ItemMeta itemMeta, int customModel);
    void setItemModel(org.bukkit.inventory.meta.ItemMeta itemMeta, String itemModel);
    void setRarity(org.bukkit.inventory.meta.ItemMeta itemMeta, String rarity) throws IllegalArgumentException;
    void setTrim(org.bukkit.inventory.meta.ItemMeta itemMeta, String trimMaterial, String trimPattern) throws IllegalArgumentException;
    void setHideTooltip(org.bukkit.inventory.meta.ItemMeta itemMeta);
    void addPotion(org.bukkit.inventory.meta.PotionMeta potionMeta, org.bukkit.potion.PotionEffect potionEffect);
    String getMinecraftKey(org.bukkit.inventory.ItemStack itemStack);
    void makeItemGlow(org.bukkit.inventory.meta.ItemMeta itemMeta);
}
```

`BukkitBlocksRegistry`/`BukkitItemsRegistry` handles are the boxed `Material` enum constant itself — zero wrapper allocation, same handle pattern as `IWorldManager`/`IUIManager`. Both compose `NMSAlgorithms` internally for the absorbed methods; real callers reaching around `IPlatform` for these today: `core/key/Keys.java`, `core/key/types/MaterialKey.java`, `core/itemstack/ItemBuilder.java`, `core/menu/impl/MenuBiomes.java`, `core/menu/impl/MenuCounts.java`, `island/IslandUtils.java`, `island/SIsland.java`, `listener/BlockChangesListener.java`, `mission/MissionsManagerImpl.java`.

### `ITagsManager` (NEW)

Replaces `NMSTags` — a generic NBT/compound-tag read-write facade, not block- or item-specific. Its 17 real callers outside `nms/` are the plugin's own NBT type system (`tag/CompoundTag`, `tag/ListTag`, `tag/StringTag`, `tag/IntTag`, `tag/LongTag`, `tag/FloatTag`, `tag/DoubleTag`, `tag/ByteTag`, `tag/ShortTag`, `tag/ByteArrayTag`, `tag/IntArrayTag` — each concrete `Tag` subtype calls the matching `NMSTags.getNBT*Value`/`setNBTCompoundTagValue` method) plus `core/serialization/impl/ItemStack2TagSerializer`, `world/schematic/impl/SuperiorSchematicDeserializer`, `core/schematic/SchematicEntity`, `core/itemstack/ItemSkulls`, `config/SettingsContainer`. Folding this into `IBlocksRegistry`/`IItemsRegistry` would misrepresent it — the `tag/` package's own type system isn't block or item specific, it's generic data storage that blocks, items, *and* entities all happen to use. It gets its own sub-manager instead of being force-fit into an existing one.

```java
public interface ITagsManager {
    void spawnEntity(org.bukkit.entity.EntityType entityType, org.bukkit.Location location, CompoundTag compoundTag);
    Set<String> getNBTCompoundValue(Object object);
    double getNBTDoubleValue(Object object);
    float getNBTFloatValue(Object object);
    int[] getNBTIntArrayValue(Object object);
    int getNBTIntValue(Object object);
    long getNBTLongValue(Object object);
    String getNBTStringValue(Object object);
    void setNBTCompoundTagValue(Object object, String key, Object value);
    int getNBTTagListSize(Object object);
}
```

The `Object object` parameters are pre-existing — `NMSTags` already takes a raw NBT-compound handle typed as `Object` (there's no shared Bukkit NBT type to reference), so this one didn't need any handle-vs-wrapper redesign; it's relocated as-is.

**Deliberately not touched by this addition:** `core/key`'s existing Material coupling goes much deeper than a lookup call — `MaterialKey`, `MaterialKeyMap`, `MaterialKeySet`, and `core/Materials.java` (the legacy-name-mapping helper) all use `EnumMap<Material, ...>`/`EnumSet<Material>` static caches built by iterating `Material.values()` at class-init time, not a resolve-by-string-key pattern. Rewiring that onto the registries is a real, separate migration (the same honest-scoping call as `IWorld`'s `asBukkitWorld()`), not something this addition claims to finish. `Keys.of(Material)` stays the accepted, already-isolated input boundary for now; `IBlocksRegistry`/`IItemsRegistry` are the seam *new* platform code should use instead of importing `Material` fresh.

## `IPlatform` facade (full shape)

```java
public interface IPlatform {
    IScheduler getScheduler();
    IWorldManager getWorldManager();
    IServerManager getServerManager();
    IUIManager getUIManager();
    IEventsManager getEventsManager();
    IBlocksRegistry getBlocksRegistry();
    IItemsRegistry getItemsRegistry();
    ITagsManager getTagsManager();
}
```

## How `core/` obtains `IPlatform`

**Instance getter on `SuperiorSkyblockPlugin`**, matching the codebase's dominant existing convention (`plugin.getProviders()`, `plugin.getPluginEventsDispatcher()`, `plugin.getGameEventsDispatcher()` — all instance fields, ~10x more common than `BukkitExecutor`'s static-singleton-with-`init()` pattern). Note `plugin.getNMSAlgorithms()`/etc. are deliberately *not* cited as a convention to follow here anymore — those getters still exist on `SuperiorSkyblockPlugin` (nothing about `NMSLoader`'s version-selection wiring changes), but per the new rule only `platform/bukkit/*` classes are allowed to call them; `getPlatform()` is what `core/` calls instead:
```java
private final IPlatform platform = new BukkitPlatform(this);
public IPlatform getPlatform() { return platform; }
```
Consumed the same way every other manager already is: `private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin(); ... plugin.getPlatform().getScheduler().runSync(...)`.

## Migration phasing (6 independently-mergeable phases, smallest blast radius first)

Each phase now also redistributes the `NMS*` facade methods that belong to the sub-manager it lands, so `core/` never gains a *new* `plugin.getNMSXxx()` call site in a phase it's already touching.

1. **Scheduler + Server** — add `IScheduler`/`IServerManager`/`BukkitPlatform` skeleton, wire `SuperiorSkyblockPlugin.getPlatform()`, turn `BukkitExecutor` into a delegating shim (zero other-file changes needed), migrate `CalcTask` and `BossBarTask` off `BukkitRunnable` onto `runAsyncTimer`/`runSyncTimer` + `cancelTask`, migrate `ManagerLoadException.handle()`/`StatsPlayersCounter`. Also lands `IServerManager`'s absorbed `NMSAlgorithms` (registerCommand/getCurrentTps/getDataVersion/getMaxWorldSize) and `NMSPlayers` (setSkinTexture/wasThrownByPlayer/createOfflinePlayerData/getPlayerLocale) methods, repointing `commands/CommandTabCompletes`, `commands/CommandsManagerImpl`, `core/profiler/ProfilerSession`, `player/inventory/ClearActions`, `listener/PlayersListener`. Representative files: `core/threads/BukkitExecutor.java`, `core/task/CalcTask.java`, `service/bossbar/BossBarTask.java`, `core/errors/ManagerLoadException.java`.
2. **World/position resolution** — add `IWorldManager`/`BukkitWorldManager`, migrate `LazyWorldLocation`, `ChunkPosition`, `IslandWorlds`, `IslandWorldsPlayersStrategy`, `SBlockPosition`, `SWorldPosition` to resolve through it, using `asBukkitWorld()` at each handoff to an `Island`/API caller. Also the largest single NMS redistribution: `NMSWorld`, `NMSChunks`, `NMSDragonFight`, `NMSEntities`, and `NMSAlgorithms`'s biome/sign-line/combinedId(Location) methods all move onto `IWorldManager` in this same phase, repointing `ChunksListener`, `SIsland`, `GridManagerImpl`, `service/portals/PortalsManagerServiceImpl`, `service/region/RegionManagerServiceImpl`, `service/dragon/DragonBattleServiceImpl` (already isolated — just repointed), `listener/PortalsListener`, `module/upgrades/type/UpgradeTypeEntityLimits`, `listener/BlockChangesListener`, `island/IslandUtils`, `world/WorldGenerator`, `world/schematic/*`.
3. **Event bus firing** — add `IEventsManager`, change the single `Bukkit.getPluginManager().callEvent(...)` line in `PluginEventsDispatcher.fireEvent`, rewire `BukkitEventsListener`'s 2 `onGameEvent` call sites through `getEventsManager().notifyGameEvent(...)`. Sequenced after Phase 2 so `BukkitPlatform`'s shape is stable first. No NMS redistribution here — `BukkitEventsListener` itself stays on `NMSAlgorithms`'s internal wiring (`getGenericGameCreator()`, etc.), see non-goals.
4. **UI/inventory + items** (largest) — add `IUIManager`, extend `GameEventArgs` inventory event DTOs additively, rewrite `MenusListener` off raw `.bukkitEvent` reads, relocate `ItemBuilder` to `platform/bukkit/item/`, route `RegularInventoryMenuLayoutImpl`/`AbstractMenuView` open/close through `IUIManager`, land the additive `MenuView` API change (below). Also lands `NMSDialogs` (openDialog/closeDialog) and `NMSPlayers`'s HUD subset (sendActionBar/sendTitle/createBossBar) on `IUIManager`, repointing `core/menu/MenuCommandsImpl`, `core/menu/layout/RegularDialogMenuLayoutImpl`, `external/ui/UIProvider_Default`, `external/bossbar/BossBarProvider_Default`. Representative files: `listener/MenusListener.java`, `core/menu/view/AbstractMenuView.java`, `core/menu/button/click/ButtonClickContextImpl.java`, `core/itemstack/ItemBuilder.java`.
5. **Block/item identity** — add `IBlocksRegistry`/`IItemsRegistry` + `BukkitPlatform` wiring, including the `NMSAlgorithms` block-data (getCombinedId/getBlockDataValue/getBlockKey/getMinecartBlock/getFallingBlockType) and item-meta (setCustomModel/setItemModel/setRarity/setTrim/setHideTooltip/addPotion/getMinecraftKey/makeItemGlow/compareMaterials) subsets, repointing `core/key/Keys.java`, `core/key/types/MaterialKey.java`, `core/itemstack/ItemBuilder.java`, `core/menu/impl/MenuBiomes.java`, `core/menu/impl/MenuCounts.java`. Scope is establishing the seam, not migrating all 56 `Material` call sites: land the registries, point any *new* platform code and the `NMSAlgorithms` call sites above at them. Full `core/key`/`Materials.java` migration is called out above as a separate future effort, same as `IWorld`'s partial-decoupling tradeoff.
6. **Tags/NBT** (last, narrowest but most files) — add `ITagsManager`/`BukkitTagsManager` composing `NMSTags`, repoint all 11 `tag/*.java` concrete `Tag` subtypes plus `core/serialization/impl/ItemStack2TagSerializer`, `world/schematic/impl/SuperiorSchematicDeserializer`, `core/schematic/SchematicEntity`, `core/itemstack/ItemSkulls`, `config/SettingsContainer` off `plugin.getNMSTags()` onto `plugin.getPlatform().getTagsManager()`. Sequenced last because it touches the `tag/` package's own type system (16 files) rather than a handful of known consumers — highest file count, lowest design risk (pure relocation, no signature changes).

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
- `Island`/`SuperiorPlayer`'s raw `Location`/`World`/`Chunk`/`Block` API methods — untouched; `IWorldManager.asBukkitWorld(Object)` is the deliberate seam.
- `core/key/Keys.of(...)` static-factory boundary, and `MaterialKey`/`MaterialKeyMap`/`MaterialKeySet`/`core/Materials.java`'s internal `EnumMap<Material,...>` caches — untouched; `IBlocksRegistry`/`IItemsRegistry` are the seam for new code, not a retrofit of this machinery.
- The `NMS*` interfaces themselves, their per-version implementations, and the NMS Template System that generates them — untouched. Only the *access path* changes (`core/`/etc. call the owning `IPlatform` sub-manager; only `platform/bukkit/*` still calls `plugin.getNMSXxx()` directly).
- `NMSAlgorithms`'s `default` methods consumed only by `listener/BukkitEventsListener` itself (`getClassProcessor()`, `handlePaperChatRenderer()`, `getGenericGameCreator()`, `isSoftExplosion()`, `hideAttributes()`) — untouched. `BukkitEventsListener` is the Bukkit event-capture entry point that *feeds* the platform-agnostic `GameEventType` system in the first place (see Context); it's inherently Bukkit-specific and sits alongside `platform/bukkit/`, not inside `core/`, so the "don't call `NMS*` directly" rule doesn't apply to it.
- `world/BukkitEntities.java` — untouched; it's already an intentionally Bukkit-facing helper (its own name says so), calling `NMSEntities` directly is not the coupling this effort targets.
- `NMSHolograms` — untouched, already fully isolated behind `service/hologram/HologramsServiceImpl`; no `core/` file calls it directly today.
- `api/hooks/*` provider SPIs — untouched, intentional Bukkit adapters to external plugins.
- `ItemBuilder` is relocated, not abstracted behind a generic interface — no second implementation being designed for.
- No purpose-built wrapper interfaces (`ITask`, `IWorld`, `IInventoryView`, `IItemStack`) for objects that are only ever passed from one platform manager call to another — those are `Object` handles with manager-side methods instead. `IWorldManager.asBukkitWorld(Object)`, `IUIManager.asBukkitItemStack(Object)`, `IBlocksRegistry.asBukkitMaterial(Object)`, `IItemsRegistry.asBukkitMaterial(Object)` remain as the explicit, greppable escape hatches for each.
- No new Gradle subproject for `platform/bukkit/` in this effort.

## Verification (no automated test suite exists — per CLAUDE.md, manual + structural)

1. **Grep-based import-boundary Gradle task**, same mechanism as the existing `checkDebug` task: fail build if `core/**/*.java` (excluding relocated/allowlisted files) imports `org.bukkit.*`. Track the count down from the current baseline (165/391 files) phase by phase; bypass via `-x checkImportBoundary` like `-x checkDebug`.
2. **Grep-based NMS-access-boundary check**, same task or a sibling one: fail build if any `.java` file outside `nms/` and `platform/bukkit/` calls `plugin.getNMS` (covers all 9 facades in one pattern), excluding the explicit non-goal carve-outs (`listener/BukkitEventsListener.java`, `world/BukkitEntities.java`). Baseline: ~25 files today (`core/`, `island/`, `listener/`, `world/`, `service/`, `tag/`, `commands/`, `module/`); target zero outside the carve-outs by end of Phase 6.
3. **Compile-per-phase**: `gradle build -x checkDebug` after each phase — most of this refactor is type-directed, so compilation catches signature mismatches immediately.
4. **Manual smoke test on a running Paper/Spigot server** after each phase: Phase 1 → island recalculation task and boss bar countdown still fire and still cancel cleanly, shutdown still clean, TPS/data-version-dependent commands and skin/actionbar/title rendering still work; Phase 2 → island teleport/load across dimensions (normal/nether/end), biome updates, dragon fight start/reset, portal/minecart/animal-food interactions; Phase 3 → a plugin event listener (e.g. `IslandCreateEvent`) still fires and is cancellable; Phase 4 → open every menu category (island menu, admin menu, biome picker, stacked-blocks deposit menu) and click through, open a dialog, trigger a boss bar; Phase 5 → block/item value lookups and generator output still resolve correctly; Phase 6 → island data with NBT-backed items (skulls, custom schematics) still saves/loads correctly.
5. **Escape-hatch audit** after Phase 6: grep for `.asBukkitWorld(`, `.asBukkitItemStack(`, `.asBukkitMaterial(` call sites as the honest final decoupling metric (not "zero Bukkit imports," since the escape hatches are deliberate).
