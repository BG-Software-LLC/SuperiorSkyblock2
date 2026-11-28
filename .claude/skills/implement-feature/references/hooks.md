# Hooks (integrations with other plugins)

Exemplars:
- `git show 7d76ad79f`: a general hook (Plan).
- `git show 80b2c029c`: a provider (SmartSpawner).
- `git show 6ea9785e8 285a8d483`: a custom-blocks hook, plus the registration it forgot.
- `git show 7e70777d8`: a new major version of a plugin, as a sibling module.

## Steps
1. **`Hooks/<Name>/build.gradle`**, copied from a recent hook, not an old one:
   - `group 'Hooks:<Name>'` and repositories;
   - `compileOnly` the target plugin, a Spigot API jar, `project(":API")` and the root project;
   - the opt-out guard: `if (project.hasProperty('hook.compile_<name>') && !Boolean.valueOf(project.findProperty('hook.compile_<name>').toString())) { project.tasks.all { task -> task.enabled = false } }`.
   - A dependency built for Java 25 needs toolchain 25 **and** `options.release = 21`, or Paper's remapper fails on older servers.
2. **`settings.gradle`**: `include 'Hooks:<Name>'`. **`gradle.properties`**: `hook.compile_<name>=true` (tracked despite `.gitignore`).
3. **Code** in `Hooks/<Name>/src/main/java/com/bgsoftware/superiorskyblock/external/...`:
   - a hook class with `public static void register(SuperiorSkyblockPlugin plugin)`;
   - or a provider `<kind>/<Kind>Provider_<Name>` with a `(SuperiorSkyblockPlugin)` constructor, implementing the API or internal provider interface.
4. **Register it in `external/ProvidersManagerImpl`**, or it never loads. Classes are loaded by name string:
   - `if (canRegisterHook("<Plugin>")) registerHook("<Name>Hook");`
   - or `createInstance("<kind>.<Kind>Provider_<Name>")`.
   - Renaming a class means updating these strings.
   - Plugins that load their data late get a delay (`BukkitExecutor.sync(..., 5L)`).
   - "Which provider" options use an `'AUTO'` chain, where the order of the `if`s is the AUTO priority. Warn and fall back on unknown values.
5. **`plugin.yml`**: a `class-depends` entry, alphabetical, for the target plugin.

## Rules
- **Register Bukkit listeners every time the hook is constructed.** Reload calls `HandlerList.unregisterAll(plugin)`, so a `static boolean registered` guard loses its listeners after the first reload. Only one-shot registrations into SuperiorSkyblock's own registries (key parsers via `registerKeyParser`, custom-block providers) keep a static once-guard.
- An incompatible new major version of a plugin gets a sibling module (`EpicSpawners9`, `UltimateStacker4`, `RoseStacker1_5`, `CraftEngine26`). `ProvidersManagerImpl` picks it by version string or a `Class.forName` probe. A fix to one sibling usually belongs in the others too.
- Hooks import plugin internals (`ProtectionHelper`, `IslandPrivileges`, `BaseSchematic`…). When you change an internal signature or add an abstract method to an internal base class, grep `Hooks/`.
- Wrap hook initialisation in `catch (Throwable t) { Log.error(t, "..."); }`, so a broken third-party version can't stop the plugin from starting.
- A hook that adds GUIs (barrels, minions…) needs its own protection listener, e.g. `@EventHandler(priority = NORMAL, ignoreCancelled = true)` + `ProtectionHelper`. Check `isX` before calling `getX`.
- Custom-block plugins:
  - implement `CustomKeyParser` with `Keys.of("PREFIX", ID, KeyIndicator.CUSTOM)`;
  - record placements with `WorldRecordService.recordBlockPlace/Break`;
  - optionally implement `ICustomBlocksProvider` for worth calculation.
