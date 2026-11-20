# Protection bypasses (the most common bug class)

Every new block, entity or interaction path is a possible way to act on someone else's island: projectiles, the off hand, bone meal, shears, leads, vehicles, spawn eggs, new mobs. One report often lists several holes; fix each in its own commit under the same issue number. Subject: `Fixed the ability to <action> on other islands (#issue)`.

## First, confirm the hole
Trace the interaction: which Bukkit event fires (and on which server type), which `ProtectionListener` handler and `RegionManagerServiceImpl` branch handle it, and which privilege it requires.
- If a privilege already guards it, check the reporter's side: the default role lists in `config.yml` → `island-roles` (e.g. coops get LEASH and NAME_ENTITY), island-specific permission changes, bypass mode or OP, or another plugin handling the event.
- If a cancelled event is involved, remember that callbacks skip events cancelled earlier.
- When nothing in the code explains it, ship `Debug` probes (`debugging.md`) rather than a speculative branch.

## Fix ladder, cheapest first
1. **Data only.**
   - Blocks: add the block to `src/main/resources/interactables.yml`. It loads through `config/section/InteractablesSection`, which maps it to INTERACT unless `loadLegacyInternal` says otherwise.
   - Entities: adjust the category in `src/main/resources/entity-categories.yml`.
   - Default files reach new installs only; existing servers just get a "Potentially missing …" warning. If the hole must close for everyone, also fix it in code.
2. **Item- or state-dependent cases.** Add an `else if` branch in `service/region/RegionManagerServiceImpl` (`handleBlockInteract`, `handleEntityInteract`, …) that maps entity/block + item used to an `IslandPrivileges.X`.
   - **The order of branches matters.** A branch after a more generic one never runs (a LECTERN branch after `InventoryHolder`). An early `return InteractionResult.SUCCESS` is itself a bypass.
   - The default should be the INTERACT privilege, not `SUCCESS`.
3. **Missing event path.** Add a private `onX(GameEvent<GameEventArgs.X> e)` in `listener/ProtectionListener`, registered in its `registerListeners()`. It calls the region service, then `if (ProtectionHelper.shouldPreventInteraction(result, superiorPlayer, sendMessage)) e.setCancelled();`. If the Bukkit event isn't bridged yet, bridge it first (`../implement-feature/references/events.md`).
4. **New privilege** when no existing one fits (`../implement-feature/references/privileges.md`).
5. **Vanilla mechanic with no Bukkit event** (trial vaults, sculk sensors): wrap the NMS strategy object in a delegate that checks `island.hasPermission(...)`, via a feature-gated template.

## Lessons
- Check the entity's *state*, not just the item held. A rule keyed only on shears also blocked shearing sheep; the fix asks NMS whether the entity actually has a saddle.
- Check **both hands**: off-hand interactions bypassed checks. Don't send the "protected" message twice for duplicate off-hand events.
- **Projectiles** reach blocks through several events (`EntityInteractEvent`, `ProjectileHitEvent`). On 1.8/1.12 `ProjectileHitEvent` can't be cancelled, so remove the projectile instead.
- Use supertypes and name patterns (`instanceof AbstractHorse`, `endsWith("_CHEST")`, `contains("_RAFT")`); lists of types missed variants (zombie/skeleton horses, zombie nautilus).
- Resolve new content by name: `@Nullable static final X = EnumHelper.getEnum(...)`.
- Plugins that open their own GUIs (WildStacker barrels…) need a protection listener in the hook. Check `isStackedX` before `getStackedX`.
- Hoist repeated `entity.getType()`/`item.getType()` into locals; these handlers are hot paths.
