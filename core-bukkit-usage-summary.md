# Bukkit Usage in Core Module — Summary

`src/main/java/com/bgsoftware/superiorskyblock/core/` — 169 files import `org.bukkit.*`. No `org.spigotmc.*`/`org.papermc.*` compile-time imports; one reflective Paper probe.

## Tight coupling (extends/implements Bukkit types, or drives scheduler/event/plugin-manager directly)

- `core/task/CalcTask.java` — `extends BukkitRunnable`
- `core/menu/impl/internal/StackedBlocksDepositMenu.java` — `implements InventoryHolder`
- `core/LazyWorldLocation.java` — `extends Location`
- `core/threads/BukkitExecutor.java` — wraps `Bukkit.getScheduler()`, the whole scheduling abstraction
- `core/events/plugin/{PluginEventsDispatcher,PluginEventsFactory,PluginEventType}.java` + `core/events/args/PluginEventArgs.java` — custom events extend Bukkit `Event`/`Cancellable`, fired via `Bukkit.getPluginManager().callEvent(...)`
- `core/errors/ManagerLoadException.java` — calls `Bukkit.shutdown()` directly on fatal load error
- `core/menu/**` (~90 files) — entire GUI framework on `Inventory`/`InventoryHolder`, `event.inventory.*`, `ItemStack`/`ItemMeta`
- `core/itemstack/ItemBuilder.java` — deep ItemStack/ItemMeta builder
- `core/key/**` — wraps `Material`/`EntityType` as identifiers

## Direct static `Bukkit.*` calls

| Call | Files |
|---|---|
| `Bukkit.getWorld(...)` | `ChunkPosition`, `IslandWorlds` (x4), `IslandWorldsPlayersStrategy` (x2), `LazyWorldLocation` (x2), `SBlockPosition`, `SWorldPosition` |
| `Bukkit.shutdown()` | `ManagerLoadException` |
| `Bukkit.getPluginManager().callEvent(...)` | `PluginEventsDispatcher` |
| `Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ...)` | `MenuActions`, `MenuCommandsImpl`, `BiomePagedObjectButton`, `IslandCreationButton`, `IslandPrivilegePagedObjectButton` (x2), `TopIslandsSelfIslandButton` (x2) |
| `Bukkit.getOnlinePlayers()` | `StatsClient`, `StatsPlayersCounter`, `CalcTask` (x3) |
| `Bukkit.getScheduler()....` | `BukkitExecutor` (runTaskTimer/Async/Later/LaterAsync/cancelTasks) |

## Moderate coupling (Bukkit types as plain data, no extends/implements)

`core/database/**` (ItemStack/PotionEffectType/World — legacy upgrade+deserialize code), `core/serialization/**`, `core/stackedblocks/**`, `core/schematic/**`, `core/values/BlockValuesManagerImpl.java`, `core/messages/**`, root `core/` position/world classes (`SBlockPosition`, `SWorldPosition`, `IslandWorlds`, etc.), `core/collections/Location2ObjectMap.java`, `core/factory/FactoriesManagerImpl.java`, `core/formatting/**`.

## Trivial

- `StatsClient` / `StatsPlayersCounter` — only `Bukkit.getOnlinePlayers().size()`
- `core/logging/Log.java` — no Bukkit import; gets logger via plugin instance, not static `Bukkit.getLogger()`

## Paper-specific (reflective, no hard dependency)

- `core/io/loader/FilesLookupFactory.java` — `Class.forName("io.papermc.paper.pluginremap.PluginRemapper")`, safe fallback if absent.

## Not found in core/

No `implements Listener`, `ConfigurationSerializable`, `extends JavaPlugin`, `CommandExecutor`/`TabCompleter`.
