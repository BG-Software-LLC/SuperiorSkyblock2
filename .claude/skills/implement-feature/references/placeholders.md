# Placeholders

Exemplars: `git show 5315d7884` (simple), `git show 45186441a` (parametrised; it also shows the renames you shouldn't copy).

All placeholders are in `service/placeholders/PlaceholdersServiceImpl`.

- **Simple placeholder:** `.put("snake_name", (island, superiorPlayer) -> ...)`, alphabetical, in the `PLAYER_PARSES` map builder or in the `// Island Placeholders` section of `ISLAND_PARSES`. The `superior_` / `superior_island_` prefix is implied.
- **Placeholder with a parameter:** `private static final Pattern X_PLACEHOLDER_PATTERN = Pattern.compile("island_x_(.+?)")`, alphabetical among the constants, plus a matcher branch that returns `Optional`.
  - Put the branch before any broader pattern with the same prefix.
  - Make sure the new pattern doesn't also match existing simple keys: `coop_(.+)` swallowed `coop_limit`/`coop_list`/`coop_size`. Anchor it, e.g. `coop_(\\d+)`.
  - Use non-greedy groups: a greedy `(.+)_(.+)` broke the `value_format_N` sub-keys.
- Suffix conventions: `_format` (fancy), `_raw`, `_int`. Booleans use `Formatters.BOOLEAN_FORMATTER.format(b, superiorPlayer.getUserLocale())`.
- **Never rename or remove a placeholder.** Server owners use them on scoreboards and in other plugins.
  - For island placeholders, keep the old key in the `// Deprecated Island Placeholders` section via `legacyPlaceholder("superior_island_<old>", "superior_island_<new>", parser)`. It keeps working and warns once.
  - Player placeholders have no such helper, so keep the old key as a plain alias.
- Placeholders run often. Don't load worlds in them: use `island.getCenterPosition()` and `WorldInfo`, not `getCenter(dimension)`. Don't allocate heavily.
