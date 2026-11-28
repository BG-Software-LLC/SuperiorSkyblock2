# Island privileges and flags

Since 2026, block and entity privileges are **data-driven** (`git show 5641f77d1`, `6186a4747`), and many hard-coded `IslandPrivileges` constants were deleted. Pick the recipe by kind.

## (a) A privilege for interacting with blocks: no Java constant
1. Add the block to the bundled `src/main/resources/interactables.yml`.
2. That file is still the legacy flat list, so the default block→privilege mapping is in Java: `config/section/InteractablesSection.loadLegacyInternal`, INTERACT unless a branch says otherwise. To use a different privilege, add or extend a branch there (a new privilege gets a `lazyIslandPrivilege("NAME")` constant).
3. If Java code must reference it, add a nullable `CONFIG_X` in `IslandPrivileges`, resolved in `onSettingsUpdate()` like `CONFIG_VAULT_INTERACT`.
4. Enforcement is generic: `RegionManagerServiceImpl` uses `plugin.getSettings().getInteractablesMap()`.

## (b) A privilege for entities (interact / damage / spawn): no Java constant
1. Add or extend a category in the bundled `src/main/resources/entity-categories.yml` (`entities:` + `actions: SPAWN/DAMAGE/INTERACT/SPAWNER_SPAWN/NATURAL_SPAWN`). Class-based groups get a `BuiltinEntityCategory` constant.
2. Privilege and flag names are derived from the category: `<CATEGORY>_DAMAGE`, `<CATEGORY>_SPAWN`, `NATURAL_<CATEGORY>_SPAWN`…. Enforcement is generic through `EntityCategory`.

## (c) Any other privilege (an action or a command)
Exemplar: `git show b45f82dbb`.
1. Add `public static final IslandPrivilege X = register("X");`, alphabetical, in `island/privilege/IslandPrivileges`.
   - Version-gated: `register("X", ServerVersion.isAtLeast(...))`, with `@Nullable`.
   - Command privileges: `register("X", IslandPrivilege.Type.COMMAND)`. These go only to ladder roles and members.
2. Enforce it where the action happens: `RegionManagerServiceImpl`, `ProtectionListener`, or the command through `IPermissibleCommand#getPrivilege`.

## All three also need
- **Default roles** in `config.yml` → `island-roles` (`ladder.<role>.permissions`, `guest.permissions`, `coop.permissions`). Coop is easy to forget.
- **A menu entry in every applicable `menus/permissions*.yml` variant** (`permissions`, `1_12`, `1_16`, `1_17`, `1_19`, `1_20`, `1_21`):
  - the key is the lowercase privilege name;
  - it needs `display-menu`, `permission-enabled`, `permission-disabled`, `role-permission` (the lore uses `{0}`), and `has-access`/`no-access` sounds;
  - entries are alphabetical, and legacy variants use legacy materials;
  - a version-gated privilege goes only in the variants for versions that have it; supporting a new version may need a new `permissions1_XX.yml`.
- No lang changes.
- Loading code must not drop privilege names it doesn't know: an addon may register them later. `IslandPrivilege.getByName` throws `NullPointerException` for unknown names, so catch it, then register the name or keep it.

## Island flags
- Add `register("X")` in `island/flag/IslandFlags`; `registerFlags()` is the holder.
- Enforce it in the relevant listener.
- Add an entry to every `menus/settings*.yml` variant (not the permissions menus).
- Per-category spawn flags come from `entity-categories.yml`.
