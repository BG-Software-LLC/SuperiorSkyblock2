# Initialization order

Something reads state before it exists. Startup NPEs and settings that "never take effect" usually come from this.

## Root causes seen
- **Static plugin or settings access too early.** A static initializer or field initializer reads `SuperiorSkyblockPlugin.getPlugin()` or `plugin.getSettings()` before `onLoad` has set the plugin (callback caches in `static {}` blocks, `SortingTypes`, `UIProvider_Default` as a field initializer).
- **Circular class initialisation.** A static block in an outer class iterated its nested enum, whose constants called back into the outer class.
- **A static singleton listener** grabbed the plugin at class load.
- **Field initializers** read a logger injected later, or passed `this` before the constructor ran.
- **Enum instance fields** read static fields declared after the constants (they are still null at that point).
- **A constructor** read another manager's state that wasn't loaded yet (the spawn island, the worlds provider chosen before addons registered theirs).
- **A constructor used its own data too early:** its own fields before they were set, or data not loaded yet (bank interest started before the balance loaded).
- **Builder order.** A later builder call overwrote an earlier one.

## Fix idioms
- An explicit `registerX(plugin)` called from `SuperiorSkyblockPlugin.onLoad`, after `plugin = this`.
- Constructor injection of the plugin for classes created during bootstrap.
- `LazyReference` for values that can't resolve during static initialisation.
- Internal plugin events instead of reading other managers in a constructor: `SPAWN_UPDATE_EVENT`, `WORLD_PROVIDER_UPDATE_EVENT`, `SETTINGS_UPDATE_EVENT` (`new PluginEventType<Empty>(null)`).
- **Rule:** a class constructed or initialised while the plugin itself is being constructed must not:
  - hold a static `plugin` field;
  - fire plugin events or call `Log` from its constructor;
  - read `getSettings()` in a static or field initializer.

  Compute settings-derived values in an `onSettingsUpdate` callback instead.
