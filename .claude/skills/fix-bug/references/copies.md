# Fix every copy

A recurring reason for a follow-up fix: the first commit missed a copy of the code it changed. Before committing, find every copy.

## Real misses (what to avoid)
- A tick-tracker fix skipped v1_17 and v26_2.
- A player-data helper fix skipped v1_21_9 and v1_21_10, so players teleported to 0,0,0.
- A Paper-ism fix covered only 1.17+.
- `AbstractKeyMap.merge` was fixed but `LazyLoadedKeyMap` wasn't.
- The `Block` overload of the safe-spot check was fixed but the `ChunkSnapshot` overload wasn't.
- The `getBiome` fallback reached v1_19…v26_1 but not v26_2, which was built in parallel. v26_3 was then copied from v26_2, and **both still lack it**.

## Sets that must change together
- **Hand-written NMS classes in every templated module (v1_17 … v26_3):** `NMSAlgorithmsImpl`, `NMSChunksImpl`, `NMSEntitiesImpl`, `NMSTagsImpl`, `NMSWorldImpl`, `utils/NMSUtilsVersioned`, `world/PropertiesMapperVersioned`, `dragon/*Wrapper`/`IslandEntityEnderDragon`, `hologram/EntityHologram`, `world/BlockLevelTicksTracker` (v1_18+), `world/CollectingNeighborUpdaterTracker` (v1_19+).
- **The fully hand-written modules `v1_8_R3`, `v1_12_R1`, `v1_16_R3`**, which aren't covered by template fixes.
- **Twin collections:** `Chunk2ObjectMap`/`IslandPosition2ObjectMap`; `AbstractKeyMap`/`LazyLoadedKeyMap`/`CustomKeyMap`.
- **Player/admin commands:** `CmdPromote`/`CmdDemote`/`CmdAdminPromote`/`CmdAdminDemote`; `CmdShow`/`CmdAdminShow`.
- **Mission-holder logic** in both `SIsland` and `SSuperiorPlayer`.
- **Version-split hooks:** RoseStacker + RoseStacker1_5, EpicSpawners6–9, UltimateStacker/3/4, CraftEngine/CraftEngine26, SkinsRestorer/14/15.
- **Overloads** of the same method (`Block` vs `ChunkSnapshot`, `Location` vs `BlockPosition`).
- **Resources:** every `menus/<name>*.yml` variant; all 13 `lang/*.yml`.

## How to find them
```bash
grep -rn "<distinctive line>" src NMS/*/src NMS/src/main/templates Hooks/*/src
ls NMS/*/src/main/java/com/bgsoftware/superiorskyblock/nms/*/<path>/<File>.java
git log --stat -3 -- <a sibling file>          # did the last fix to a sibling touch every module?
```
If a fix applies to all templated versions and the code is in a template, edit the `.template`. If the code is duplicated across modules and could be shared, mention moving it into a template, but keep the fix itself minimal.
