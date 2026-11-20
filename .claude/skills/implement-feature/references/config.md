# Config options and migrations

Exemplars:
- `git show 64670a5be`: a simple top-level enum option.
- `git show 605793182`: a migration, a deprecated getter, a reload listener and `IGNORED_SECTIONS`.
- `git show 5641f77d1`: a section moved to its own file.

## New top-level key: 5 files
1. `src/main/resources/config.yml`: a kebab-case key near related keys, with a `#` comment block above it stating the semantics, allowed values and edge values ("Set to -1 to disable."). Quote provider choices: `'AUTO'`.
2. `config/SettingsContainer.java`: `public final <type> camelCase;`, assigned in the constructor with `config.getX("full.dotted.path", <same default as config.yml>)`.
   - Use the **full dotted path** (`worlds.sea-level-height`, not `sea-level-height`).
   - Enums: parse in try/catch, and on failure `Log.warnFromFile("config.yml", ..., ", using X instead.")`.
   - Lists: warn about bad entries, skip them, wrap the result as unmodifiable.
   - Prefer typed checks (`isConfigurationSection`, `isInt`, `isList`) to `contains`.
3. `config/section/GlobalSection.java`: a getter returning `getContainer().field`.
4. `config/SettingsManagerImpl.java`: an `@Override` delegating to `this.global`.
5. `API/.../api/config/SettingsManager.java`: the getter, with javadoc ending in `Config-path: <path>`.

**A key inside an existing nested block** touches 4 files: steps 1, 2 and 5 (the method goes in the nested interface, e.g. `SettingsManager.IslandNames`) and an `@Override` in the matching `config/section/<Block>Section`. `SettingsManagerImpl` doesn't change. A brand-new block gets `XSection extends SettingsContainerHolder implements SettingsManager.X`, registered in `SettingsManagerImpl`.

Before the container is built, `syncWithConfig` fills missing keys from the bundled `config.yml`. The Java default is a fallback, but keys in `IGNORED_SECTIONS` rely on it, so keep it equal to the yml default. Changing a default means changing both.

## Reload
`/is admin reload` rebuilds `SettingsContainer`. Code that reads `plugin.getSettings().getX()` when it needs the value follows reloads automatically. These don't:
- **Static caches derived from settings.** Add a static `registerListeners(PluginEventsDispatcher)` that subscribes `PluginEventType.SETTINGS_UPDATE_EVENT` to a static `onSettingsUpdate()` that rebuilds the cache.
  - Add it to `PluginEventsDispatcher.registerDefaultListeners()`, or call it from an explicit `registerX(plugin)` in `onLoad`.
  - Plugin-event callbacks are never cleared, so never register them from an object that reload re-creates, such as a listener instance; they pile up, one more per reload.
- **Long-lived objects that captured a value.** Read through `Value.syncedSupplied(() -> plugin.getSettings()...)`.
- **Listeners gated by an option.** Gate them inside `listener/BukkitListeners.registerListeners()`, which runs again after every reload.

## Renaming, moving or retyping a key
- Add a block at the **end** of `SettingsManagerImpl.convertData(YamlConfiguration cfg)`:
  ```java
  if (cfg.isInt("old")) { cfg.set("new", convert(cfg.getInt("old"))); cfg.set("old", null); forceSave = true; }
  ```
- If only the name changes, keep the API method and point it at the new key. If the shape changes, add a new getter and keep the old one `@Deprecated`, computed from the new data.
- When the meaning of a default changes, migrate existing configs so they keep the old behaviour.
- Free-form map sections (where users invent the keys) go in `SettingsManagerImpl.IGNORED_SECTIONS`, as the full dotted path (matched as a substring). Otherwise the updater re-adds the default keys.
- Moving a section into its own file:
  - `convertX(plugin, cfg)`, called in `loadData` between `convertData` and `syncWithConfig`, writes the new file with `CommentedConfiguration` and removes the old key;
  - a new `XSection`;
  - a `saveResource` call when the file is missing.
- **Never delete or overwrite a user's file in a migration.** A refactor once deleted the main `config.yml`.
- Format migrations caused most config regressions. Guard every load path with `isConfigurationSection` before `getConfigurationSection(...).getKeys(...)`.

## Built-in modules (bank, generators, missions, upgrades)
- Config lives in `src/main/resources/modules/<name>/config.yml` and is parsed into the module's immutable inner `Configuration` class. Read it with `BuiltinModules.X.getConfiguration()`.
- Migrations go in `BuiltinModule#onConfigCreate(plugin, config, firstTime)` (return `true` to save). Free-form sections go in `getIgnoredSections()`.
- Module-only commands belong to the module (`getSuperiorAdminCommands`), so disabling the module removes them.
