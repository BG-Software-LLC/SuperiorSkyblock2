# Other recurring bug classes

## Copy-paste, identity and variable mix-ups
These compile fine and pass a quick read:
- a method called twice where the second call should have been a different one (`refreshBankLogs` vs `refreshIslandBank`);
- the wrong event class in a `PluginEventType` constant;
- a migration reading the wrong key;
- a copy-pasted guard condition;
- a getter returning a sibling's value (the visit warm-up returned the home warm-up);
- `map.remove(wrongVariable)`, which compiles because `Map.remove(Object)` takes anything;
- a player UUID used as an island UUID after a search-and-replace;
- a value written to the wrong DB table (`islands` vs `islands_settings`);
- `getIsland() == other.getIsland()` treating two island-less players as teammates (`null == null`); write `island != null && island == other.getIsland()`;
- comparing a field with itself after assigning it (capture the old value first);
- an inverted boolean after extracting a helper;
- `==`/`!=` flipped during a mass refactor; `git log -S'<code>'` finds when a line changed.

When you copy a neighbour, re-check every identifier. When you change an identity comparison, check the null / no-island case.

## Config and data loading
- Missing or wrongly-typed sections: `isConfigurationSection`/`isInt`/`isList` before `get…`.
- References to things that no longer exist (removed mission items, unknown biomes or entities): `if (x == null) continue;` + `Log.warnFromFile`, or a fallback chain ending in a safe default.
- Unknown privileges or flags from the DB: catch the `NullPointerException` from `getByName`, then register or keep the name. Never drop it.
- Serializer and deserializer must agree (`","` vs `", "`, field counts), and must keep accepting old formats. `LocationSerializer` still never parses pitch (an off-by-one length check).
- Don't transform values in a constructor that deserialisation also calls: warps moved +0.5 on every restart. Normalise at the point of creation.
- `CREATE TABLE` fixes don't reach existing databases; use `DatabaseUpgrade_VN`.

## Custom collections and sentinel values
- A custom `AbstractMap` must override every `Map` default it relies on (`merge` went through a fallback `get` and caused "Duplicate key" errors) and keep `size` consistent in `computeIfAbsent`, `iterator().remove()` and `clear()`.
- Limits: `IslandUpgradeConstants.NO_LIMIT_VALUE = -1`, `SYNCED_VALUE = -2`. Check `limit >= 0` before comparing. Never write the sentinel where a removal is meant.
- Sorting on live, changing values breaks `Comparator` contracts: snapshot the values before sorting.

## Commands and tab-complete
- Off-by-one argument index: `args.length == N` completes `args[N-1]`.
- Completing in `tabComplete` instead of `adminTabComplete` on admin island/player commands.
- A missing `return` in a `switch` (still present in `CmdAdminData`).
- Ignoring `isTabCompleteHideVanished`.
- Usage, max args and execute disagreeing.
- An invalid sub-argument should reply with `Message.COMMAND_USAGE` and stop, not fall into a default branch.
- Aliases, disabled commands and reload interact; changing alias handling broke unregister and lookup on the same day. The canonical label is `getAliases().get(0)`.
- A static cache shared between senders must not hold per-sender, permission-filtered data (the help-page cache does today).

## Menus and messages
- Menus:
  - a view registered before its early returns leaks; register only after opening succeeds;
  - builders mutated in `build()` (which runs once per slot), or getters with side effects, make every button act like the first;
  - clicks on empty slots must not trigger buttons;
  - a later builder call can overwrite an earlier one.
- Messages:
  - "optimising" component code lost colours and click events; keep the legacy text round-trip;
  - joining messages into `Message.CUSTOM` loses their component types; send them one by one;
  - don't run PlaceholderAPI over user-supplied arguments;
  - blank lang entries must not send empty lines.
- Placeholders: a broad pattern swallowing simple keys (`coop_(.+)` breaks `coop_limit`); greedy regex groups.

## High-risk areas
Read the original refactor's diff around any code you fix here. These areas produced fix-the-fix chains:
- the event bridge (`BukkitEventsListener`);
- pooled objects;
- DB bridges after the `DBColumn` refactor;
- `WorldPosition` serialisation;
- NMS templates vs hand-written copies;
- menu builders after the dialogs refactor;
- portal routing;
- island lookup and position packing;
- config-format migrations;
- schematic caching, which is disabled with a hard `|| true` because of a dupe.
