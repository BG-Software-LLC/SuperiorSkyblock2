# CLAUDE.md

SuperiorSkyblock2 is a Skyblock gamemode plugin for Bukkit/Spigot/Paper by BG Software, licensed GPLv3. The current version is 2026.3, with API version 18.

## Before you change code
Invoke the matching project skill first, even for small changes. The skills hold the repo's rules, recipes, code idioms and checklists. Each `SKILL.md` points to small reference files; read only the ones your task needs.
- **`implement-feature`**: adding or extending anything (config, messages, commands, menus, privileges, placeholders, API, events, hooks, NMS, new Minecraft versions…).
- **`fix-bug`**: any bug, error, exploit, GitHub issue or "X doesn't work" report.

Always:
- Never break the public API in `API/`: third-party plugins compile against it.
- Use Java 8 in `API/` and the main plugin. Newer Java is allowed only inside NMS and hook modules.
- Don't leave `Bukkit.broadcastMessage` calls: the `checkDebug` build task fails on them.

## Build
```bash
gradle build                # all modules → /target
gradle :API:build           # API only
gradle build -x checkDebug  # skip the debug-statement check
```
- Gradle 9.4.1. NMS modules use their own toolchains: Java 21 for 1.17–1.21.x, Java 25 for 26.x.
- `gradle.properties` switches modules off to speed up builds (`nms.compile_v1_21=false`, `hook.compile_<name>=false`).
- There are no tests or linters; changes are verified on a live server.

## Layout
- `API/`: the public API, published as `com.bgsoftware:SuperiorSkyblockAPI` (entry point `SuperiorSkyblockAPI.getSuperiorSkyblock()`).
- `src/main/java/com/bgsoftware/superiorskyblock/`: the plugin.
  - `SuperiorSkyblockPlugin` is the entry point. `reloadPlugin` handles both startup and `/is admin reload`.
  - `island/`: `SIsland`, `SpawnIsland`, privileges, flags, upgrades, warps, bank, cache.
  - `player/`: `SSuperiorPlayer`, `SuperiorNPCPlayer`.
  - `config/`: settings.
  - `commands/`: player and admin commands.
  - `listener/`: listeners.
  - `core/`: utilities, object pools, logging, threads, menus, messages, events, database.
  - `service/`, `external/` (providers for other plugins), `module/` (built-in modules), `world/`.
  - `nms/`: facades over version-specific code.
  - `platform/`: the platform abstraction and the game-event types.
- `NMS/`: one module per Minecraft version (`v1_8_R3` … `v26_3`). Shared code lives in `NMS/src/main/templates/` as `.template` files.
- `Hooks/`: one module per third-party plugin integration.
- `Missions/`: the built-in mission types.
- `src/main/resources/`: `config.yml`, `lang/` (13 languages), version-suffixed `menus/`, `interactables.yml`, `entity-categories.yml`, schematics, block values.

## Architecture
- **Managers** (`Manager` with `loadData()`): grid/islands, players, roles, upgrades, missions, schematics, block values, menus, modules.
  - Each owns a **container** of cached objects.
  - **Database bridges** persist them (`DatabaseBridge`; SQL over HikariCP, SQLite by default).
- **`Island`** is the central model: members, roles, privileges, flags, upgrades, warps, bank, block counts. **`SuperiorPlayer`** wraps online and offline players.
- **Services** (`IService`, looked up through `plugin.getServices()`): placeholders, holograms, messages, region protection, world records (block counts), portals, boss bars, dragon battles, stacked blocks.
- **Events**: two dispatchers.
  - Plugin events: `PluginEventsFactory` runs internal callbacks, then fires the public Bukkit API event.
  - Game events: `BukkitEventsListener` bridges Bukkit events to the core listeners.
- **Hooks**: `ProvidersManagerImpl` loads integrations (Vault, PlaceholderAPI, LuckPerms, stackers, custom-block plugins…) at runtime and falls back when a plugin is absent.
- **Modules**: the built-in Generators, Missions, Bank and Upgrades, plus external module JARs in `plugins/SuperiorSkyblock2/modules/`.
- **NMS**: the plugin calls facades (`NMSAlgorithms`, `NMSChunks`, `NMSWorld`, …). Each version module implements them, and NMSLoader picks the module at runtime.
