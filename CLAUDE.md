# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

**Build the entire project:**
```bash
gradle build
```
This compiles all modules (API, NMS versions, Hooks, Missions) and creates fat JARs with shadow JAR plugin. Artifacts are placed in `/target`.

**Build API only:**
```bash
gradle :API:build
```

**Build without debug checks:**
```bash
gradle build -x checkDebug
```

**Clean build artifacts:**
```bash
gradle clean
```

Note: The gradlew wrapper (gradle-wrapper.properties) specifies Gradle 9.4.1. The project uses Java 8 as baseline, with support up to Java 21 for newer NMS versions.

## Code Quality

**Debug check (automatic on build):**
The `checkDebug` task scans for `Bukkit.broadcastMessage` debug statements and fails the build if found. This is part of the standard build process. To bypass (not recommended): `gradle build -x checkDebug`.

**No automated linting:** The project has no static analysis configured (no spotbugs, checkstyle, etc.). Code review and manual inspection are the primary quality gates.

## Testing

**No test suite:** This codebase has no automated tests. Validation is manual and would require a running Spigot/Paper server.

## High-Level Architecture

SuperiorSkyblock2 is a Bukkit/Spigot/Paper plugin providing a complete Skyblock gamemode implementation. The codebase uses a **Manager pattern** with a clean API/implementation separation.

### Core Plugin Entry Point

`SuperiorSkyblockPlugin` (extends `JavaPlugin`, implements `SuperiorSkyblock` interface) initializes and orchestrates all systems:
- Loads configuration, database, NMS handlers
- Initializes managers for grids, players, islands, roles, upgrades, missions
- Registers event listeners and commands
- Bootstraps the module system and services

### Manager System

All major systems extend `Manager` (abstract base with `loadData()` contract):

- **GridManager**: Island creation, placement, and global island tracking. Uses `IslandBuilderImpl` (builder pattern) to construct islands with all dependencies.
- **PlayersManager**: Maps Bukkit Players to `SuperiorPlayer` wrappers. Handles offline players and NPC support via metadata checking.
- **RolesManager**: Defines island member roles and their permission hierarchies.
- **UpgradesManager**: Manages upgradeable island properties (size, team limits, bank limits, etc.) with level-based costing.
- **MissionsManager**: Tracks island missions and mission holders. Integrated with mission modules.
- **SchematicManager**: Loads island schematics from YAML files for initial island generation.
- **BlockValuesManager**: Maintains block worth calculations for island level scoring.
- **ModulesManager**: Dynamically loads and manages plugin modules (builtin: Generators, Missions, Bank, Upgrades; external: custom JAR modules).

Each manager owns a **Container** (IslandsContainer, PlayersContainer, etc.) that stores and retrieves cached objects, with database bridges handling persistence.

### Island System

`Island` objects represent player islands and are the central domain model:
- **Island hierarchy**: Owner, members (with roles and privileges), co-op players
- **Island properties**: Upgrades, flags, warps, bank (money storage), settings, ratings
- **Island data**: Block counts (cached), world locations, schematics, permissions
- **Island operations**: Invite/kick, promote/demote, disband, recalculate worth

Related packages:
- `island.builder`: `IslandBuilderImpl` - fluent island construction from database/scratch
- `island.privilege`: Permission nodes and role-based privilege checking
- `island.flag`: Toggleable island behavior flags
- `island.upgrade`: Upgrade leveling system with costs and effects
- `island.cache`: Caches block counts, levels, worth to avoid recalculation
- `island.chunk`: Island chunk ownership tracking
- `island.warp`: Island warp management
- `island.bank`: Island money storage with transactions

### Player System

`SuperiorPlayer` wrappers (subclass `SSuperiorPlayer`) wrap Bukkit Players and offline data:
- Permission checks for island actions
- Personal statistics and missions
- Island membership state
- Chat and respawn preferences

Players use builders (`SuperiorPlayerBuilderImpl`) for construction. NPC players handled separately via `SuperiorNPCPlayer` for compatibility with NPC plugins.

### Database Abstraction

`DatabaseBridge` interface abstracts persistence:
- **Modes**: Read-only, read-write, write-only (specified per database instance)
- **Operations**: `insertObject()`, `updateObject()`, `deleteObject()`, `getObject()`
- **Bridges**: Dedicated bridge classes (GridDatabaseBridge, IslandsDatabaseBridge, PlayersDatabaseBridge) handle specific entity persistence
- **SQL implementation**: `SQLDatabaseBridge` backed by HikariCP connection pooling

ObjectPools (ObjectsPools) provide pooled reuse of DBColumn and Batch objects for allocation efficiency.

### NMS (Native Minecraft Server) Abstraction

Supports 18 Minecraft versions (v1_8_R3 through v26_2, including Paper variants):

- **Strategy**: Each version gets its own Gradle subproject under `/NMS/v*/`
- **Code generation**: Templates in `/NMS/src/main/templates/` are processed with version-specific properties to generate implementation classes (AbstractNMSAlgorithms, AbstractNMSChunks, etc.)
- **Version registration**: NMSLoader (external common library) dynamically selects correct NMS handler at runtime based on server version
- **NMS facades** (in `/src/main/java/com/bgsoftware/superiorskyblock/nms/`): 
  - NMSAlgorithms, NMSChunks, NMSEntities, NMSWorld, NMSPlayers, NMSTags, NMSHolograms, NMSDragonFight
  - These route to version-specific implementations and hide Minecraft internals

Version-specific modules use PaperWeight (for 1.17+) or Spigot mappings for compilation. Conditional compilation controlled via `gradle.properties` (e.g., `nms.compile_v1_21=true`).

### NMS Template System

Shared NMS code is written once as `.template` files and stamped into each version's source tree at build time, avoiding copy-paste across 18+ versions.

**How it works:**

1. **Templates** (`NMS/src/main/templates/**/*.template`): Java source files with `${KEY}` placeholders for anything that differs between Minecraft versions — package names, class paths, method signatures, etc.
2. **Properties files** (`NMS/<version>/properties`): Each NMS subproject contains a plain-text `properties` file with key=value pairs that fill in the placeholders for that version.
3. **`generateNmsSources` task** (`NMS/build.gradle`): Defined on every NMS subproject. At build time it:
   - Reads the subproject's `properties` file
   - Iterates all `*.template` files under `NMS/src/main/templates/`
   - Replaces every `${KEY}` occurrence with the matching value
   - Writes the result to `$buildDir/generated/sources/nms/` (stripping the `.template` extension, preserving directory structure)
4. **Source set wiring**: Generated sources are added to the `main` Java source set alongside hand-written sources in `src/main/java/`. `compileJava` depends on `generateNmsSources`, so generation always runs first.

**Common template variables** (defined per version in `properties`):
- `NMS_VERSION` — version identifier used in package names (e.g. `v1_21_7`)
- `CRAFTBUKKIT_PACKAGE` — CraftBukkit package, versioned pre-1.20.5, unversioned after
- `COMMON_NMS_PACKAGE` — base NMS package shared by a version family (e.g. `v1_20_3` code reused by `v1_21_7`)
- `REGISTRY_CLASS`, `CHUNK_STATUS_CLASS`, `SERVER_LEVEL_RANDOM_TYPE` — NMS class paths that moved between versions
- `ABSTRACT_MINECART_SUBPACKAGE`, `STAR_LIGHT_INTERFACE_CLASS`, etc. — other version-specific class references

**When to edit:**
- To fix a bug that affects all NMS versions: edit the `.template` file.
- To fix a bug that is version-specific: edit the hand-written source in that subproject's `src/main/java/`, and if it should propagate, add a new placeholder + property entry.
- To add a new NMS version: create a new subproject directory with a `properties` file, a `build.gradle`, and a `src/main/java/` for any hand-written overrides.

### External Integrations (Hooks)

40+ optional plugin hooks in `/Hooks` use a **provider pattern**:
- Each hook detects the target plugin's presence at runtime
- Implements a provider interface (e.g., EconomyProvider for Vault)
- ProvidersManager dynamically loads compatible providers
- Seamless fallback if plugin is absent

Examples: PlaceholderAPI, Vault economy, LuckPerms permissions, WorldEdit, Slimefun, various spawner plugins.

### Module System

**Builtin modules** (loaded automatically):
- **Generators**: Custom block generators (e.g., cobblestone with custom rates)
- **Missions**: Repeatable island challenges with rewards
- **Bank**: Island-wide money storage separate from player accounts
- **Upgrades**: Island stat upgrades with leveling

External modules can be placed in `/plugins/SuperiorSkyblock2/modules/` as JAR files. ModulesManager dynamically loads and registers them via reflection.

### Service Layer

ServicesHandler registers and retrieves internal services that other components depend on:
- PlaceholdersService, HologramsService, DragonBattleService, BossBarsService
- MessagesService, PortalsService, RegionManagerService, StackedBlocksService, WorldRecordService

Services implement `IService` interface and are registered with Bukkit's ServiceManager (ServicePriority.Normal).

### Event System

Custom plugin events (in `core.events.plugin.PluginEventsFactory`) abstract Bukkit/Paper events:
- PluginEventsDispatcher fires domain events (IslandJoinEvent, IslandUpgradeEvent, etc.)
- EventCallbacks allow plugins to hook into lifecycle events
- Platform abstraction: GameEventsDispatcher bridges to Bukkit events
- Event args passed through PluginEventArgs utility

### Key Design Patterns Used

1. **Manager**: Base Manager + concrete implementations (GridManagerImpl, PlayersManagerImpl, etc.)
2. **Builder**: IslandBuilder, PlayerBuilder, SchematicBuilder, ItemBuilder for complex object construction
3. **Factory**: DefaultIslandsFactory, DefaultPlayersFactory, etc. for object creation
4. **Strategy**: IslandCreationAlgorithm implementations for pluggable generation logic
5. **Bridge**: DatabaseBridge abstraction layer for persistence
6. **Template Method**: NMS abstract classes with template generation for version-specific code
7. **Container**: IslandsContainer, PlayersContainer for data storage and retrieval
8. **Service Locator**: ServicesHandler for central service registry
9. **Observer**: Custom event system with callback registrations
10. **Adapter**: Provider wrappers for external plugin integration (PlaceholderAPI, Vault, etc.)

## Module Structure

### Root Build

`build.gradle` orchestrates subprojects:
- Configures shadow JAR relocation (BStats, common libraries)
- Defines collectArtifacts task (final JAR placement to `/target`)
- Includes subproject builds in shadowJar (NMS modules embedded in nms/ subdirectory, mission modules in modules/missions/)

### API Module

Published separately as `com.bgsoftware:SuperiorSkyblockAPI` to Maven repo (https://repo.bg-software.com/repository/api/).

291 Java files defining:
- Core interfaces: SuperiorSkyblock, Island, SuperiorPlayer, GridManager, etc.
- Event types: 30+ plugin-specific events extending Bukkit events
- Data layer: DatabaseBridge, DatabaseFilter abstractions
- Handlers: Interface contracts for all managers
- Enums and domain objects

**Backwards compatibility is mandatory.** Third-party plugins depend on this API, and breaking changes silently break those plugins at runtime. Rules:

- **Never remove or rename** an existing API method, class, or enum constant.
- **Never change a method signature** (parameter types, parameter count, return type).
- **Never change observable behaviour** of an existing API method — if the semantics must change, add a new method instead.
- **Extend, don't replace**: add new methods alongside the old ones. Have the old method delegate to the new one so existing callers continue to work unchanged.
- **`@Deprecated` is not removal** — mark old methods `@Deprecated` when a better alternative exists, but keep them functional and delegating.

Example pattern when behaviour needs to change:

```java
// Old method — keep it, delegate to new
@Deprecated
Island getIsland(UUID uuid);

// New method with improved/extended behaviour
Island getIsland(UUID uuid, boolean includeArchived);
```

**Prefer `Enumerable` classes over Java `enum` for extensible constants.** Java enums are sealed — third-party plugins cannot add new values. When a concept needs to be extendable through the API (new privileges, new flags, new types), use the Enumerable pattern instead.

The pattern (`IslandPrivilege`, `IslandFlag`, etc.):
- The class implements `com.bgsoftware.superiorskyblock.api.objects.Enumerable` (provides `ordinal()` for efficient array-based lookups).
- A private static `Map<String, T>` holds all registered instances; a private `static int ordinalCounter` assigns sequential ordinals.
- The constructor is **private** — callers cannot instantiate directly.
- A public static `register(String name, ...)` method creates and stores a new instance, enforcing uniqueness.
- Public static `values()` and `getByName(String)` mirror the standard enum API.

```java
public class IslandPrivilege implements Enumerable {
    private static final Map<String, IslandPrivilege> registry = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    private IslandPrivilege(String name) {
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.ordinal = ordinalCounter++;
    }

    @Override public int ordinal() { return ordinal; }
    public String getName() { return name; }

    public static void register(String name) {
        name = name.toUpperCase(Locale.ENGLISH);
        Preconditions.checkState(!registry.containsKey(name), "Already registered: " + name);
        registry.put(name, new IslandPrivilege(name));
    }

    public static IslandPrivilege getByName(String name) { /* lookup */ return registry.get(name.toUpperCase(Locale.ENGLISH)); }
    public static Collection<IslandPrivilege> values() { return registry.values(); }
}
```

Use this pattern (not `enum`) whenever you add a new extensible constant type to the API.

**Use `EnumerateMap` and `EnumerateSet` for collections keyed by `Enumerable` types.** Both classes live in `core.collections` and replace `HashMap`/`HashSet` when the key is an `Enumerable`. They back storage with a plain array indexed by `ordinal()`, giving O(1) get/contains with no hashing overhead.

- `EnumerateMap<K extends Enumerable, V>` — array-backed map; `get(key)` reads `values[key.ordinal()]` directly.
- `EnumerateSet<K extends Enumerable>` — array-backed set backed by `boolean[]`; `contains(key)` reads `values[key.ordinal()]` directly.
- Both auto-expand their backing array when a newly registered `Enumerable` has a higher ordinal than the current capacity.
- Construct with the full `Collection<K>` of known instances so the array is pre-sized correctly: `new EnumerateMap<>(IslandPrivilege.values())`.

Use these instead of `HashMap`/`HashSet` anywhere the key is an `Enumerable` (privileges, flags, upgrades, dimensions, etc.).

### Implementation (Main Plugin)

798 Java files under `/src/main/java/com/bgsoftware/superiorskyblock/`:

Key packages:
- `config/`: SettingsManagerImpl, configuration loaders, section-based config structure
- `core/`: Manager base, utilities (ObjectsPools, LazyReference, Counter, etc.), error handling
- `database/`: SQL bridge implementation, DBColumn, result mapping
- `island/`: Island implementation, builders, privilege system, upgrades, warps, cache
- `player/`: Player wrappers, permissions, respawn logic, inventory management
- `commands/`: Command system with admin/player command hierarchy
- `listener/`: Event listeners for Bukkit events
- `external/`: Provider implementations for external plugins
- `service/`: Internal service implementations
- `module/`: Module system and builtin module implementations
- `world/`: World generation, chunk management, schematics
- `nms/`: NMS facades routing to version-specific implementations

## Important Configuration Notes

- **gradle.properties**: Controls which NMS and hook modules compile. Set to `false` to skip expensive version compilations (e.g., `nms.compile_v1_21=false`).
- **plugin.yml**: Contains 50+ permission nodes for commands; API version 1.13+; soft dependencies on 25+ common plugins
- Database configuration: SQLite by default, but supports any JDBC database via config
- Block values: Loaded from YAML, updatable at runtime
- Schematics: Binary format stored in `/src/main/resources/schematics/`

## Project Metadata

- **Current version**: 2026.1 (active development)
- **License**: GNU GPL v3.0
- **Author**: Ome_R (BG Software)
- **Project type**: Bukkit/Spigot/Paper plugin (Java)
- **Target audience**: Skyblock server administrators and developers

## API Availability

The plugin is designed as an API-first plugin. Other plugins should depend on `SuperiorSkyblockAPI` (not the implementation JAR) to avoid breaking changes. API methods are stable and documented; implementation changes occur without API modifications.

Access the plugin instance via: `SuperiorSkyblockAPI.getSuperiorSkyblock()` after checking plugin availability.

