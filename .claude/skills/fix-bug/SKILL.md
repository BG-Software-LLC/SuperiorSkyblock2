---
name: fix-bug
description: How to find and fix bugs in the SuperiorSkyblock2 plugin the way this repo does it. Covers fix scope and commit wording, a symptom-to-cause triage table, the recurring bug classes with their proven fix idioms, how to instrument bugs you can't reproduce, and a regression checklist. Use it whenever you work on a bug, error, exception or stack trace (NPE, IllegalStateException, async error), a GitHub issue report, a dupe or exploit, players able to do things on other islands (protection bypass), wrong island worth/level or block/entity counts, something that breaks after /is admin reload, version-specific (1.8/1.12/1.21/26.x, Spigot vs Paper) breakage, a hook conflict with another plugin, or any "X doesn't work" report in this repo, even when the fix looks like a one-liner.
---

# Fixing a bug in SuperiorSkyblock2

There are no automated tests. Fixes are confirmed by the reporter on a live server, and a large share of this repo's history is second attempts at fixes that missed a case. This skill exists to get it right the first time.

## What a fix looks like here
- **Root cause, smallest change.** The median fix is 2 files and 19 lines (1 file and 12 lines outside `NMS/` and resources). Half touch a single file. A fix grows only when:
  - the same edit must be repeated in every NMS module or resource copy;
  - or the root cause really spans several files (threading, lazy worlds, reload).

  Don't reorganise surrounding code: refactors bundled into fixes caused regressions.
- **Fix it in the layer that owns the rule.** If a menu shows wrong data because the model is wrong, fix the model. If a command and a menu (or a player and an admin command) disagree, move the rule into the domain method (`SIsland`, `SSuperiorPlayer`) or a shared helper and call it from both.
- **One symptom per commit.** A report listing several holes gets one commit per hole, all under the same issue number.
- **Don't change the API to fix a bug.** That covers signatures *and* observable behaviour of `API/` methods; third-party plugins depend on both. If callers need different behaviour, add a method (see the `implement-feature` skill, `references/api.md`).
- **Commit subject** (85% of fixes carry the issue number):
  - `Fixed <what the user sees> (#issue)` or `Fix <symptom> (#issue)`;
  - `Do not <behaviour> [as it causes …] (#issue)` when the fix removes a behaviour;
  - `Potential fix for <symptom> (#issue)` only when the cause isn't confirmed;
  - `Fixed errors due to recent commit` for your own regression.

## Workflow
1. **Read the area's history first:** `git log --oneline -15 -- <file>` and `git log --grep '#<issue>'`. Many areas have earlier fixes that covered only some cases, and they show the shape of the right fix.
   - Issue and PR numbers share one sequence, so `#NNNN` in a subject may be a different PR. `git show` a hit before treating it as related.
2. **Classify the bug** with the triage table below and open **only** the matching reference file(s).
3. **Confirm the bug exists in the code before fixing it.** Trace the real path: Bukkit event → `BukkitEventsListener` → listener → `RegionManagerServiceImpl`/domain method → privilege or setting.
   - If the path already handles the case, the report is usually caused by configuration or environment:
     - default role permissions in `config.yml` → `island-roles` (coops get more than guests);
     - permissions the island changed;
     - bypass (`/is admin bypass`, `superior.admin.bypass.*`, OP);
     - a server version or fork whose event behaviour differs;
     - another plugin acting first. A cancelled event never reaches our callbacks, but a plugin that un-cancels it or acts on its own can still cause the problem.
   - In that case, ship diagnostics instead of a speculative fix (`references/debugging.md`) and ask for the config or version details.
   - To see what vanilla or Paper itself does, read the decompiled server in the Gradle cache: `~/.gradle/caches/paperweight-userdev/v2/work/applyDevBundlePatches_*/output.jar` (`unzip -p <jar> <path/Class>.class | javap …`, or the matching sources jar if present).
4. **Find the root cause, not just the line that threw.** Ask:
   - Can this run on another thread?
   - Can the world be unloaded?
   - Did `/is admin reload` replace this object?
   - Is this a pooled object used after release?
   - Does another event also handle this change?
   - Does this run before something was initialised?
   - Does it differ on 1.8/1.12, the newest version, or Spigot vs Paper?
5. **Can't reproduce or confirm it?** Ship diagnostics, not guesses (`references/debugging.md`).
6. **Fix every copy** of the code you changed (`references/copies.md`): every NMS module including the newest and v1_8_R3/v1_12_R1/v1_16_R3, twin classes and commands, overloads, menu and lang variants.
7. **Run the checklist** below. For code idioms (logging, threading helpers, pooled objects, collections), see `../implement-feature/references/code-style.md`. Keep each file's existing line endings: many files in this working tree are CRLF while git stores LF.

## Triage table

| Symptom | Likely class | Read |
|---|---|---|
| Players can break, place, use, open, ride, shear or damage things on other islands | Protection bypass | `references/protection.md` |
| Wrong island worth/level, block limits bypassed or wrong, counts doubled or missing | Block/entity tracking | `references/block-counts.md` |
| Works until `/is admin reload`; stale config; duplicated actions after reload | Reload | `references/reload.md` |
| "Asynchronous …" errors, CME, random NPEs, hangs on shutdown, hangs waiting for chunks | Threading | `references/threading.md` |
| Errors with SlimeWorldManager/ASP/custom world providers; null worlds; unwanted world or chunk loads; wrong world or dimension | Lazy worlds, multi-world | `references/worlds.md` |
| Values change "by themselves", locations shifted, errors only under load | Pooled-object misuse | `references/pooled-objects.md` |
| NPE or nulls at startup; a value that "never takes effect" | Initialization order | `references/init-order.md` |
| Works on Paper but not Spigot or a fork; breaks on 1.8/1.12 or the newest version; NoSuchMethod/FieldError | Version/platform | `references/versions.md` |
| Fixed before but still happens in some version, command or menu | Missed copy | `references/copies.md` |
| Wrong value/table/UUID, inverted condition, config or DB load errors, broken tab-complete, menu buttons acting wrong, empty or duplicated messages, custom map errors, `-1`/`-2` limit confusion | Other recurring classes | `references/pitfalls.md` |

## Guard idioms

```java
@Nullable                                        // content newer than 1.8: resolve by name, compare with ==
private static final EntityType HAPPY_GHAST = EnumHelper.getEnum(EntityType.class, "HAPPY_GHAST");

if (island == null || !plugin.getGrid().isIslandsWorld(world)) return;          // early return

BukkitExecutor.ensureMain(() -> doXInternal(...));                              // world access from maybe-async API
future.whenCompleteAsync((r, e) -> {...}, BukkitExecutor.SYNC_EXECUTOR);        // future callbacks touching Bukkit

if (FIELD.isValid()) {...} else {/* fallback */}                                // ReflectField: versions/forks differ

try { provider.newOptionalMethod(); } catch (UnsupportedOperationException ignored) {}

Log.warnFromFile("config.yml", "Invalid value ", v, ", skipping...");           // bad data: warn and skip, don't throw

if (limit >= 0 && count >= limit) ...           // -1 = unlimited, -2 = synced (IslandUpgradeConstants)
```
Prefer explicit checks to swallowing exceptions: `catch (ClassCastException)` and `catch (NullPointerException)` used as control flow were later replaced because they didn't actually catch the bugs they targeted.

## Regression checklist
- [ ] The root cause is fixed in the layer that owns the rule, and every entry point goes through it.
- [ ] Every copy is fixed: all NMS modules (newest + legacy), twins, overloads, variants, version-split hooks.
- [ ] No API signature or observable API behaviour changed.
- [ ] Threads: no world access off the main thread; latches and counters count down on every path.
- [ ] Worlds: nothing loads a world or chunk as a side effect; null worlds are handled; nothing is keyed by chunk or dimension alone.
- [ ] Pooled objects don't escape their try block; no caller-owned `Location` is mutated.
- [ ] The fix still holds after `/is admin reload`.
- [ ] Works on 1.8/1.12 and plain Spigot, not only the newest Paper.
- [ ] Old configs, schematics and DB rows still load; bad entries are warned about and skipped.
- [ ] Probes you ship only log (never throw); no `Bukkit.broadcastMessage` left (`checkDebug` fails the build).
