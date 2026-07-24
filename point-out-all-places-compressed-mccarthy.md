# API Module — Bukkit/Spigot/Paper Usage Report

Scope: `API/src/main/java` (SuperiorSkyblockAPI, published separately, backwards-compat mandatory).

**Totals**: 198 files, 396 `org.bukkit.*` imports. Zero `io.papermc.*`, zero `org.spigotmc.*`. One `net.md_5.bungee.api.chat` usage.

## 1. Events (`api/events/`, ~90 classes)
- ALL extend `org.bukkit.event.Event` — directly, or via `IslandEvent` base (or `IslandEnterEvent`/`IslandLeaveEvent` intermediates).
- Most also `implements Cancellable`.
- All carry `HandlerList` boilerplate + call `Bukkit.getPluginManager().callEvent(...)` in constructors.
- Unavoidable: Bukkit's event bus only dispatches `Event` subclasses.

## 2. Core domain interfaces
`Island`, `SuperiorPlayer`, `IslandWarp`, `IslandChest`, managers (`GridManager`, `KeysManager`, `StackedBlocksManager`) mix wrapped and raw Bukkit types:
- Raw everywhere, no wrapper: `Location`, `World`, `Chunk`, `Block`, `Biome`, `ItemStack`, `EntityType`, `PotionEffectType`, `CommandSender`.
- `SuperiorPlayer` wraps `Player`/`OfflinePlayer` identity (`asPlayer()`, `asOfflinePlayer()`) but still leaks `Location`, `World`, `Block`, `BukkitTask` in own signatures (`getLocation()`, `teleport(Location)`, `getTeleportTask(): BukkitTask`).

## 3. Menu subsystem (`api/menu/*`)
- Callbacks take raw `InventoryClickEvent` / `InventoryCloseEvent`.
- `MenuView` implements `InventoryHolder` directly.

## 4. Hook SPIs (`api/hooks/*`)
`AFKProvider`, `PermissionsProvider`, `VanishProvider`, `EntitiesProvider`, `ChunksProvider`, `WorldsProvider` — raw `Player`/`Entity`/`World`/`Chunk` params. Intentional: adapters to external Bukkit-based plugins.

## 5. Coordinate wrappers (`api/wrappers/`)
`BlockPosition`, `BlockOffset`, `WorldPosition` — reduce but don't eliminate Bukkit dependency; still convert to/from `Location`/`World` internally.

## 6. Non-Bukkit Spigot dependency
`MessagesService` imports `net.md_5.bungee.api.chat.BaseComponent` / `TextComponent` — only Bungee-chat usage in the whole module.

## Bottom line
Only `SuperiorPlayer` and `Key` do real wrapping, and both still leak raw Bukkit types. Everything else — events, warps, chests, managers, hooks, menus, missions, upgrades — uses `org.bukkit.*` directly in public signatures, not confined to an internal/impl layer.
