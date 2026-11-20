# Supporting a new Minecraft version

Exemplars:
- `git show a73a075dd`: 26.3.
- `git show f85e4ae9e`: 26.2.
- `git show c05af6207 27add4c40`: 1.21.11 on Paper, then Spigot.
- `git show cd4ebe6b4`: a patch release folded into an existing module.

Read `nms.md` first: templates, gates, hand-written classes.

## New major module (26.x style)
1. Copy the previous module to `NMS/vX/`. Before copying, check that the previous module has every recent fix: `git log --stat -5 -- NMS/<previous-1>` vs `NMS/<previous>`. A copy inherits any fix its source missed.
   - `build.gradle`: paperweight userdev `paperDevBundle(...)`, the Java toolchain (25 for 26.x), `compileOnly` `:NMS:Spigot-1_20_3`/`:NMS:Paper-1_20_3`/`:API`/the root project, and the `nms.compile_vX` opt-out guard;
   - `properties`;
   - the hand-written classes from the table in `nms.md`, with the package renamed.
2. `settings.gradle`: `include 'NMS:vX'`. `gradle.properties`: `nms.compile_vX=true` (forgotten once).
3. Add a constant to the `ServerVersion` enum.
4. Bump `com.bgsoftware.common.nmsloader:NMSLoader` in the root `build.gradle`.
5. Fix the API drift with the tools in `nms.md` (placeholders in every affected `properties`, `NMSUtilsVersioned` helpers, per-version methods).
6. Spigot support:
   - 1.x modules need `reobfJar`; this has been a separate later commit before.
   - 26.x modules set `reobfArtifactConfiguration = MOJANG_PRODUCTION`.
7. Content follow-ups for new blocks and mobs: protections (`RegionManagerServiceImpl`, `interactables.yml`, `entity-categories.yml`), `heads.yml` textures, `Materials`, counts-menu mappings, and missing interactions on other islands.

## Patch versions (1.21.x style)
They don't get their own `ServerVersion` constant or `gradle.properties` flag; they reuse the family flag (`nms.compile_v1_21`). Depending on compatibility:
- bump NMSLoader only (1.21.8);
- swap the dev bundle in the existing module and fold API differences into `ReflectField`/`ReflectMethod` fallbacks;
- or add a module named after the key NMSLoader assigns (the module `v1_21_10` serves 1.21.11).

Dropping the previous patch's module is fine when the server jars are interchangeable.

## Risk
Build-system changes that ride along with a version bump (Gradle, shadow, paperweight, relocation) caused the longest regression chain of the period. Make sure every supported version family still loads, including 1.8, 1.12, 1.16 and 1.17–1.21.
