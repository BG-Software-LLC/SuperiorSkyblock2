# NMS (version-specific) code

Exemplars:
- `git show 08603a3a6`: a facade method implemented in the template plus the legacy modules.
- `git show ab06643b0`: hand-written copies converted into a feature-gated template.

## Layout
- The plugin calls version-specific code through facades in `src/main/java/com/bgsoftware/superiorskyblock/nms/` (`NMSAlgorithms`, `NMSChunks`, `NMSEntities`, `NMSWorld`, `NMSPlayers`, `NMSTags`, `NMSHolograms`, `NMSDragonFight`, plus the optional `NMSDialogs`/`NMSHologramMenus`). NMSLoader picks the module at runtime.
- Templates: `NMS/src/main/templates/**/*.template` (37 files).
  - `generateNmsSources` (`NMS/build.gradle`) fills `${KEY}` placeholders from `NMS/<module>/properties` and writes to `build/generated/sources/nms/`; it runs before `compileJava`.
  - Values can be class names or whole expressions (`RESET_FLYING_TICKS=this.serverPlayer.connection.resetFlyingTicks()`).
  - Common keys:
    - `NMS_VERSION`: the package version, e.g. `v1_21_7`;
    - `CRAFTBUKKIT_PACKAGE`: versioned before 1.20.5, unversioned after;
    - `COMMON_NMS_PACKAGE`: shared code of a version family, e.g. `v1_20_3` code reused by later modules;
    - classes that moved between versions: `REGISTRY_CLASS`, `CHUNK_STATUS_CLASS`, `SERVER_LEVEL_RANDOM_TYPE`, `ABSTRACT_MINECART_SUBPACKAGE`, `STAR_LIGHT_INTERFACE_CLASS`, `ENTITY_TYPE_VALUES_CLASS`….
- **Where to edit:**
  - A change for all versions goes in the `.template`, plus the three legacy modules if they have the code.
  - A change for one version goes in that module's hand-written `src/main/java/`. If it should spread to other versions, add a placeholder and a `properties` entry.
  - A new version: `minecraft-version.md`.
- **Feature gates:** a template whose first line is `//<generate-if:KEY>` is generated only where `properties` set `KEY=true`:
  - `DIALOGS_SUPPORT` (v1_21_7+);
  - `TRIALS_SUPPORT` (v1_21+);
  - `HOLOGRAM_MENUS_SUPPORT` and `SCULK_SENSOR_SUPPORT` (v1_20_3+).

  Use a gate instead of copying a class into the modules that support a feature.
- **Templated modules** (with `properties`): v1_17 … v26_3, 15 modules.
- **Fully hand-written modules** (no `properties`; compiled against Spigot jars): `v1_8_R3`, `v1_12_R1`, `v1_16_R3`. They need their own copy of everything.
- **Classes still hand-written in every templated module.** A change to one must be copied to each:

| Class (`NMS/<v>/src/main/java/com/bgsoftware/superiorskyblock/nms/<v>/`) | Modules |
|---|---|
| `NMSAlgorithmsImpl`, `NMSChunksImpl`, `NMSEntitiesImpl`, `NMSTagsImpl`, `NMSWorldImpl` | all 15 |
| `dragon/EndDragonFightWrapper`, `dragon/IslandEntityEnderDragon`, `hologram/EntityHologram` | all 15 |
| `utils/NMSUtilsVersioned`, `world/PropertiesMapperVersioned` | all 15 |
| `world/BlockLevelTicksTracker` | v1_18+ |
| `world/CollectingNeighborUpdaterTracker` | v1_19+ |
| `world/BlockServerTickListTracker` | v1_17 |
| `vibration/IslandSculkSensorBlockEntity` | v1_17–v1_19 |

## Adding a capability
1. Add the method to the facade.
   - If only some versions can do it: a `default` no-op (`// Does not exist.`), overridden in the template or the newer modules.
   - Otherwise: abstract, implemented in the template (with `protected abstract` hooks for per-version differences) **and** in the three legacy modules.
   - Never use legacy stubs that throw `UnsupportedOperationException` when shared code calls the method unconditionally.
2. When template code doesn't compile on some version, in order of preference:
   1. a `${PLACEHOLDER}` added to the `properties` of every module that generates that template (all 15 for an ungated one);
   2. a static helper in each module's `NMSUtilsVersioned`, called from the template;
   3. the method moved out of the template into each `*Impl`.
3. Optional handlers load with `try { Optional.of(nmsLoader.loadNMSHandler(X.class)) } catch (NMSLoadException e) { Optional.empty() }`, and callers do `ifPresent`.

## Pitfalls
- Modules compile against Paper, so Paper-only fields and methods compile and then throw on plain Spigot or forks. Fixes, in order of preference:
  1. a vanilla/CraftBukkit equivalent;
  2. `ReflectField`/`ReflectMethod` (`com.bgsoftware.common.reflection`) with an `isValid()` fallback, typed loosely and trying alternate modifiers for forks;
  3. `try { paperOnly(); } catch (Throwable t) { fallback(); }`;
  4. a Paper and a Spigot strategy object chosen once.
- A `ReflectMethod` pointed at the wrong owner class is silently invalid. Verify `isValid()` on every version.
- Don't cast Bukkit objects to `Craft*` when another plugin may pass its own implementation.
- When merging copies into a template, diff every copy for version-specific strings first.
- An IDE formatter may mangle `.template` files; it doesn't recognise them as Java.
- Copies drift. ac2692495's `getBiome` fallback never reached v26_2, and v26_3 was copied from v26_2, so both still lack it. List the copies (`ls NMS/*/src/main/java/com/bgsoftware/superiorskyblock/nms/*/<path>`) and check each.
