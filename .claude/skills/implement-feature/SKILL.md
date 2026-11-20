---
name: implement-feature
description: How to add or extend functionality in the SuperiorSkyblock2 plugin the way this repo does it. Covers the files each kind of change touches, the API backwards-compatibility rules, code idioms, and the checks that past features got wrong. Use it whenever you are about to add or change a config option, message or lang key, command, menu or menu button, island privilege or flag, placeholder, API method, API or internal event, Bukkit listener, extensible Enumerable constant, plugin hook/integration, NMS (version-specific) code, support for a new Minecraft version, or a language. Also use it for "small" additions such as "add an option to…", "make X configurable" or "support plugin Y", and when a bug fix needs any of these pieces.
---

# Implementing a feature in SuperiorSkyblock2

There are no automated tests. Fixes are confirmed by users on live servers, and most follow-up commits in this repo's history come from a step a feature forgot. This skill is the checklist that prevents that. It is distilled from two years of commits; the reference files hold the details.

## Workflow

1. **Classify the change.** A feature usually combines several kinds, e.g. a command + messages + a permission. Open **only** the reference files the table below lists for those kinds.
2. **Read `references/code-style.md`** once per session, unless you already know the repo's idioms (logging, threading, pooled `Location`s, collections, version-optional constants).
3. **Copy the nearest sibling.** Find an existing feature of the same kind and mirror its shape. `git show <exemplar>` from the table shows a complete one.
   - After copying, re-check every identifier: copy-paste slips (wrong event class, wrong config key, wrong message) are a recurring bug source here.
   - Exemplars older than Feb 2025 use the removed `CallbacksBus`/`EventsBus`, and ones older than Oct 2025 predate NMS templates. Where they disagree with the references, follow the references.
4. **Changing or gating existing behaviour** (a cooldown, cost, restriction or toggle on something that already exists)? Find the single funnel the action goes through, usually a method in `SIsland`/`SSuperiorPlayer` or a shared helper (`IslandUtils`, `IslandChat`, `EntityTeleports`, `CommandArguments`), and add the logic there, so commands, menus and API calls all get it.
   - Check which thread reaches it: chat events are async, commands are on the main thread.
   - See "Where logic lives" in `references/commands.md` and the threading section of `references/code-style.md`.
5. **Implement** following the rules below.
6. **Run the checklist** at the bottom before you report done.

| You are adding… | Read | Exemplar commit |
|---|---|---|
| A `config.yml` option, a config migration, a section in its own file | `references/config.md` | 64670a5be, 605793182 |
| A cooldown, restriction, cost or toggle on an existing action | step 4 above + `references/commands.md` | e11cf78a7, 71204b12a |
| A message / lang key, or a new language | `references/messages.md` | 42fa8f1ae, c59bf2e11 |
| A player or admin command, or tab-completion | `references/commands.md` | e5344b280, 3a8abdc3a |
| A menu, menu button or menu item option | `references/menus.md` | e5e729868, 08473f315 |
| An island privilege or flag | `references/privileges.md` | b45f82dbb |
| A placeholder | `references/placeholders.md` | 5315d7884 |
| A method on `Island`/`SuperiorPlayer`/any API interface, or DB persistence | `references/api.md` | 60fb983d7, 4c8541c7f |
| An API event, an internal event, or a listener for a Bukkit event | `references/events.md` | 60fb983d7, 886ed1645 |
| An extensible constant type (an Enumerable) | `references/enumerable.md` | e5344b280 |
| An integration with another plugin | `references/hooks.md` | 7d76ad79f, 80b2c029c |
| Version-specific (NMS) code | `references/nms.md` | 08603a3a6 |
| Support for a new Minecraft version | `references/minecraft-version.md` | a73a075dd |

## Rules (why they matter)

- **Never break `API/`.** Third-party plugins compile against it, and a break only shows up as runtime errors in their plugin.
  - Don't remove, rename, or change a signature, return type or observable behaviour.
  - Add a new method instead, and make the old one `@Deprecated` and delegating.
  - **Interface with a `Delegate*` class** (`Island`, `SuperiorPlayer`, factories, algorithms, containers…): a new method may be abstract, but you must add the forwarding override to the `Delegate*` class too.
  - **Interface without one** (`api/hooks/*Provider`): new methods must be `default`, so existing third-party implementations still load. See `references/api.md`.
  - Past commits broke this several times; don't use them as precedent.
- **User-facing names are also API.** Config keys, placeholders, permissions, command aliases, lang keys, message argument order and `Debug` names. Keep the old name working with a migration or alias.
- **New options default to the old behaviour**, so existing servers behave the same until an admin opts in.
- **Java 8 in `API/` and the main plugin.** Use anonymous classes; no `var`, records, switch expressions or pattern `instanceof`. Newer syntax is fine inside NMS and hook modules that have their own toolchain.
- **Change every copy.** The repo has intentional copies that one commit must update together:
  - all 13 `lang/*.yml` files: a missing key means the message silently isn't sent in that language;
  - every version-suffixed `menus/<name>*.yml`;
  - every NMS module that has the code: templates, hand-written per-version classes, and the legacy `v1_8_R3`/`v1_12_R1`/`v1_16_R3`;
  - `SIsland`+`SpawnIsland`+`DelegateIsland`;
  - player/admin command twins.
- **Keep registries in order:** `Message` and lang keys, `Debug`, privileges, command maps, `plugin.yml` permissions, placeholder maps, `PluginEventType`, `interactables.yml`, menu entries. They are mostly alphabetical, but match each file's local neighbours: `Message.java` and the lang files don't always sort the same way.
- **Preserve line endings.** In this working tree many files are checked out with CRLF while git stores LF. Keep a file's existing endings, so the diff shows only your change and not every line.
- **Keep the change scoped.** Every mass refactor in this repo's history caused a chain of regressions. Add what the feature needs; don't reorganise neighbouring code.
- **Don't bump** `SuperiorSkyblockAPI.getAPIVersion()` or the `build.gradle` version; release commits do that.
- **Commit subject:** `Adds <feature> (#issue)` / `Added <feature> (#issue)`, or `Adds support to <MC version>` / `Added support for <Plugin>`. Leave the body empty unless it's needed.

## Done checklist

- [ ] Every copy listed above was updated.
- [ ] New commands have a `plugin.yml` node, alphabetical, with a description, under the `superior.island.*`/`superior.admin.*` children.
- [ ] New hooks are registered in `ProvidersManagerImpl` and listed in `settings.gradle`/`gradle.properties`.
- [ ] API changes are additive. Old methods still work and delegate. Every new method on an interface with a `Delegate*` class has a forwarding override there; every new method on a `hooks/*Provider` is `default`. Every new API method has javadoc.
- [ ] `/is admin reload` picks up new options:
  - caches derived from settings refresh on `SETTINGS_UPDATE_EVENT`;
  - no plugin-event callback is registered from an object that reload re-creates;
  - hook listeners register every time they are constructed.
- [ ] It works on 1.8/1.12 (legacy materials, sounds and events) and on plain Spigot, not only Paper.
- [ ] Nothing loads a world or chunk as a side effect: island worlds may be unloaded or lazy. Use `getIslandsWorldInfo`/`IslandWorlds.accessIslandWorldAsync`, not `getIslandsWorld`/`getCenter(dim)`.
- [ ] Nothing assumes a single world or the three vanilla dimensions: per-chunk/per-player state is keyed by world, and dimensions come from config.
- [ ] Code reachable from async threads touches no world state off the main thread and shares no mutable statics.
- [ ] Defaults keep existing behaviour. Nothing user-facing was renamed without an alias or migration. No migration deletes or overwrites user files.
- [ ] No `Bukkit.broadcastMessage` debug lines (the `checkDebug` build task fails on them).

If the work is really a bug fix, also use the `fix-bug` skill.
