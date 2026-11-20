# Events: API events, internal events, Bukkit listeners

Exemplars: `git show 60fb983d7` (an API event end to end), `git show 886ed1645` (listening to a new Bukkit event).

## New API (plugin) event
1. `API/.../api/events/IslandXEvent.java`:
   - extends `IslandEvent` (or the right base);
   - implements `Cancellable` if the action can be cancelled;
   - javadoc on the constructor and getters;
   - a `@Nullable SuperiorPlayer` when the console can trigger it.
   - Events that aren't about an island extend `Event` with their own `HandlerList` and `super(!Bukkit.isPrimaryThread())`.
2. `core/events/args/PluginEventArgs.java`: `public static class IslandX extends IslandDoActionArgs { public ...; }`.
3. `core/events/plugin/PluginEventType.java`: alphabetical, `ISLAND_X_EVENT = new PluginEventType<PluginEventArgs.IslandX>(IslandXEvent.class) { createBukkitEvent(args) {...} }`.
   - The class argument is only the `disabled-events` key and the debug name. A wrong class, from copying a neighbour, silently breaks only disabling, so double-check it.
   - When the API event has setters, also override `applyBukkitToPluginEvent(bukkitEvent, pluginEvent)` to copy what listeners changed back into the args.
4. `core/events/plugin/PluginEventsFactory.java`: `callIslandXEvent(...)`. Pick the return shape:
   - `!fireEvent(...).isCancelled()` for a plain cancellable action;
   - `PluginEvent<PluginEventArgs.IslandX>` when listeners can change values; callers check `isCancelled()`, then read `event.getArgs()`;
   - `void` for notifications.
   - Add a `CommandSender` overload through `commandSenderToSuperiorPlayer(...)` when admins or the console can trigger it.
5. Fire it from **every** code path that performs the action: commands, menus and API. Put the call in the shared domain method so no path skips it.

Extending an event: add a constructor and getter, keep the old constructor `@Deprecated` and delegating with the old default, add the args field, and update `createBukkitEvent`, the factory and the callers. When replacing an event, fire both the old and the new one.

## Internal events
Use these to notify caches, or to break a dependency on initialisation order.
- Declare `new PluginEventType<Empty>(null)` whose `createBukkitEvent` returns `null`, plus a `PluginEventsFactory.callXEvent()`.
- Subscribe with `plugin.getPluginEventsDispatcher().registerCallback(type, this::onX)`, or from a static `registerListeners(PluginEventsDispatcher)`.
- Existing ones: `SETTINGS_UPDATE_EVENT`, `COMMANDS_UPDATE_EVENT`, `SPAWN_UPDATE_EVENT`, `WORLD_PROVIDER_UPDATE_EVENT`.
- Use one instead of reading another manager's state in a constructor.
- Plugin-event callbacks are never cleared on reload, so register them once, from static or long-lived code. A listener instance that reload re-creates would add one more callback per reload.

## Listening to a Bukkit event (core code)
Core listeners aren't Bukkit listeners. `listener/BukkitEventsListener` bridges Bukkit events into game events.
1. Add `GameEventArgs.XEvent` (public fields) in `platform/event/args/GameEventArgs`.
2. Add `GameEventType.X_EVENT = register(GameEventArgs.XEvent.class[, GameEventFlags...])` in `platform/event/GameEventType`.
3. In `BukkitEventsListener`, add `createEventListener(GameEventType.X_EVENT, BukkitXEvent.class, this::createGameEvent)` and a private mapper. Probe optional or Paper-only events with `Class.forName` in try/catch `ClassNotFoundException`, with a fallback.
4. In the listener (`extends AbstractGameEventListener`):
   - subscribe with `registerCallback(GameEventType.X_EVENT, GameEventPriority.NORMAL, this::onX)`;
   - the handler takes `GameEvent<GameEventArgs.XEvent> e`, reads `e.getArgs()`, and cancels with `e.setCancelled()`.
5. A **new listener class** is created in `listener/BukkitListeners.registerListeners()`; put a config-gated one inside an `if`. This runs at startup and after every reload.

- The bridge attaches a Bukkit listener only for types that have callbacks when it is built. Register callbacks at construction; one registered later does nothing until the next reload. Don't register callbacks for disabled features: the event then costs nothing.
- **One physical change can arrive through several events** (`EntityChangeBlockEvent` + `ProjectileHitEvent`, Paper's `BlockDestroyEvent` + others). Before recording or counting, check that no other listener already handles it; double counting was a major bug class.
- `GameEventFlags.INVENTORY_EVENT` and `GENERIC_WORLD_EVENT` currently have the same value. Don't rely on telling them apart.
- Built-in modules (`module/*`), `Missions/*` and `Hooks/*` use plain Bukkit `@EventHandler(priority = ..., ignoreCancelled = true)` listeners.
