# Reload doesn't refresh state

## What `/is admin reload` does
`SuperiorSkyblockPlugin.reloadPlugin(PluginReloadReason.COMMAND)` runs, in order:
1. Unregister every Bukkit listener (`HandlerList.unregisterAll`) and clear the *game*-event callbacks.
2. Reload providers, then settings (a new `SettingsContainer`, then `SETTINGS_UPDATE_EVENT`), commands and module lifecycles.
3. Reload block values, upgrades, roles and messages, then schematics, menus and missions.
4. Re-create every listener in `BukkitListeners.registerListeners()`.

It does **not** reload players, islands, the database or stacked blocks. Anything holding an object that reload replaced goes stale.

## Fix idioms
- **Long-lived object captured a setting:** read through `Value.syncedSupplied(() -> plugin.getSettings()...)`.
- **Static cache derived from settings:**
  - reset it in a static `onSettingsUpdate()` subscribed to `PluginEventType.SETTINGS_UPDATE_EVENT`;
  - subscribe from a static `registerListeners(PluginEventsDispatcher)`, added to `PluginEventsDispatcher.registerDefaultListeners()`, or from an explicit `registerX(plugin)` in `onLoad`.
- **Plugin-event callbacks are never cleared.** A past reload fix cleared them and broke every cache; that was reverted. Two consequences:
  - register them once, from static or long-lived code;
  - never register one from an object that reload re-creates (a listener instance), or callbacks pile up. `FeaturesListener` (event-commands) and `IslandFlagsListener` (`SPAWN_UPDATE_EVENT`) still do this today, so event-commands run one extra time per reload.
- **Cache ids or labels, not objects reload re-creates:** command labels; role ids + `WeakReference<PlayerRole>` resolved through `getPlayerRoleFromId`.
- **Containers keep old entries:** give them `clearX()` and call it at the start of `loadData` (`clearSchematics`, `clearRoles`, `clearUpgrades`, `clearCommands`). Split startup-only from reloadable loading with `PluginReloadReason`.
- **Hook/provider Bukkit listeners register every time they are constructed.** A `static boolean registered` guard loses them after the first reload; that bug was fixed across about 20 hooks. Only one-shot registrations into SuperiorSkyblock's own registries (key parsers, custom-block providers) keep a once-guard.
- **Module commands and listeners** must be queried again on reload, and disabled modules' commands removed.
- **Config-gated listeners** may decide at registration time, because registration runs again after the settings reload.
- **Order inside reload matters:** settings before commands, providers first.
- **Custom collection `clear()`** must reset every field (an `ArrayMap` kept a stale capacity).
