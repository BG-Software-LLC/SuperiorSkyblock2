package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.entity.EntityCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class IslandFlagsListener extends AbstractGameEventListener {

    private static final EnumSet<CreatureSpawnEvent.SpawnReason> NATURAL_SPAWN_REASONS = initializeNaturalSpawnReasons();

    private final Int2ObjectMapView<ProjectileSource> originalFireballsDamager = CollectionsFactory.createInt2ObjectArrayMap();

    private World spawnIslandWorld;

    public IslandFlagsListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        this.registerListeners();
    }

    /* INTERNAL EVENTS */

    private void onSpawnUpdate() {
        // We want to optimize island checks only for island worlds
        // However, if the spawn world is not an islands world, we want to check it as well.
        Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(
                plugin.getSettings().getWorlds().getDefaultWorldDimension());
        this.spawnIslandWorld = spawnLocation.getWorld();
    }

    /* ENTITY SPAWNING */

    private void onEntitySpawn(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(e.getArgs().entity.getWorld()))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = e.getArgs().entity.getLocation(wrapper.getHandle());

            if (checkPreventEntitySpawn(e, location) || checkPreventEggLay(e, location))
                e.setCancelled();
        }
    }

    private boolean checkPreventEntitySpawn(GameEvent<GameEventArgs.EntitySpawnEvent> e, Location entityLocation) {
        CreatureSpawnEvent.SpawnReason spawnReason = e.getArgs().spawnReason;

        IslandFlag actionFlag;

        if (spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER ||
                spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            EntityCategory entityCategory = EntityCategory.getEntityCategory(Keys.of(e.getArgs().entity));
            if (entityCategory == null)
                return false;
            actionFlag = entityCategory.getSpawnerSpawnFlag();
        } else if (NATURAL_SPAWN_REASONS.contains(spawnReason)) {
            EntityCategory entityCategory = EntityCategory.getEntityCategory(Keys.of(e.getArgs().entity));
            if (entityCategory == null)
                return false;
            actionFlag = entityCategory.getNaturalSpawnFlag();
        } else {
            return false;
        }

        return actionFlag != null && preventAction(entityLocation, actionFlag);
    }

    private boolean checkPreventEggLay(GameEvent<GameEventArgs.EntitySpawnEvent> e, Location entityLocation) {
        if (!(e.getArgs().entity instanceof Item))
            return false;

        Item item = (Item) e.getArgs().entity;

        if (item.getItemStack().getType() != Material.EGG)
            return false;

        if (preventAction(entityLocation, IslandFlags.EGG_LAY)) {
            for (Entity entity : item.getNearbyEntities(1, 1, 1)) {
                if (entity instanceof Chicken) {
                    return true;
                }
            }
        }

        return false;
    }

    /* ENTITY EXPLOSIONS */

    private void onHangingBreakByEntity(GameEvent<GameEventArgs.HangingBreakEvent> e) {
        Entity entity = e.getArgs().entity;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(entity.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());

            HangingBreakEvent.RemoveCause removeCause = e.getArgs().removeCause;
            Entity remover = e.getArgs().remover;

            if (removeCause == HangingBreakEvent.RemoveCause.EXPLOSION) {
                if (remover instanceof Player) {
                    // Explosion was set by TNT.
                    if (preventAction(entityLocation, IslandFlags.TNT_EXPLOSION)) {
                        e.setCancelled();
                        return;
                    }
                } else if (remover instanceof Ghast) {
                    // Explosion was set by TNT.
                    if (preventAction(entityLocation, IslandFlags.GHAST_FIREBALL)) {
                        e.setCancelled();
                        return;
                    }
                }
            }

            if (preventEntityExplosion(remover, entityLocation)) {
                e.setCancelled();
            }
        }
    }

    private void onEntityExplode(GameEvent<GameEventArgs.EntityExplodeEvent> e) {
        Entity entity = e.getArgs().entity;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(entity.getWorld()))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventEntityExplosion(entity, entity.getLocation(wrapper.getHandle())))
                e.getArgs().blocks.clear();
        }
    }

    private void onEntityChangeBlock(GameEvent<GameEventArgs.EntityChangeBlockEvent> e) {
        Block block = e.getArgs().block;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(block.getWorld()))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventEntityExplosion(e.getArgs().entity, block.getLocation(wrapper.getHandle())))
                e.setCancelled();
        }
    }

    private void onFireballDamage(GameEvent<GameEventArgs.EntityDamageEvent> e) {
        Entity entity = e.getArgs().entity;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(entity.getWorld())) {
            return;
        }

        if (entity instanceof Fireball) {
            originalFireballsDamager.put(entity.getEntityId(), ((Fireball) entity).getShooter());
            BukkitExecutor.sync(() -> originalFireballsDamager.remove(entity.getEntityId()), 40L);
        }
    }

    public boolean preventEntityExplosion(Entity source, Location explodeLocation) {
        IslandFlag islandFlag;

        switch (source.getType()) {
            case CREEPER:
                islandFlag = IslandFlags.CREEPER_EXPLOSION;
                break;
            case PRIMED_TNT:
            case MINECART_TNT:
                islandFlag = IslandFlags.TNT_EXPLOSION;
                break;
            case WITHER:
            case WITHER_SKULL:
                islandFlag = IslandFlags.WITHER_EXPLOSION;
                break;
            case FIREBALL: {
                ProjectileSource projectileSource = originalFireballsDamager.remove(source.getEntityId());
                if (projectileSource == null)
                    projectileSource = ((Fireball) source).getShooter();
                if (projectileSource instanceof Ghast) {
                    islandFlag = IslandFlags.GHAST_FIREBALL;
                    break;
                }
            }
            default:
                return false;
        }

        return preventAction(explodeLocation, islandFlag);
    }

    /* GENERAL EVENTS */

    private void onBlockFlow(GameEvent<GameEventArgs.BlockFromToEvent> e) {
        Block block = e.getArgs().block;
        Block toBlock = e.getArgs().toBlock;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(toBlock.getWorld())) {
            return;
        }

        IslandFlag islandFlag = plugin.getNMSWorld().isWaterLogged(block) ?
                IslandFlags.WATER_FLOW : IslandFlags.LAVA_FLOW;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(toBlock.getLocation(wrapper.getHandle()), islandFlag))
                e.setCancelled();
        }
    }

    private void onCropsGrowth(GameEvent<GameEventArgs.BlockGrowEvent> e) {
        Block block = e.getArgs().block;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(block.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(block.getLocation(wrapper.getHandle()), IslandFlags.CROPS_GROWTH))
                e.setCancelled();
        }
    }

    private void onTreeGrowth(GameEvent<GameEventArgs.StructureGrowEvent> e) {
        Location location = e.getArgs().location;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(location.getWorld())) {
            return;
        }

        if (preventAction(location, IslandFlags.TREE_GROWTH))
            e.setCancelled();
    }

    private void onFireSpread(GameEvent<GameEventArgs.BlockBurnEvent> e) {
        Block block = e.getArgs().block;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(block.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(block.getLocation(wrapper.getHandle()), IslandFlags.FIRE_SPREAD))
                e.setCancelled();
        }
    }

    private void onBlockIgnite(GameEvent<GameEventArgs.BlockIgniteEvent> e) {
        if (e.getArgs().igniteCause != BlockIgniteEvent.IgniteCause.SPREAD)
            return;

        Block block = e.getArgs().block;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(block.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(block.getLocation(wrapper.getHandle()), IslandFlags.FIRE_SPREAD))
                e.setCancelled();
        }
    }

    private void onEndermanGrief(GameEvent<GameEventArgs.EntityChangeBlockEvent> e) {
        if (!(e.getArgs().entity instanceof Enderman))
            return;

        Block block = e.getArgs().block;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(block.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(block.getLocation(wrapper.getHandle()), IslandFlags.ENDERMAN_GRIEF))
                e.setCancelled();
        }
    }

    private void onPoisonAttack(GameEvent<GameEventArgs.ProjectileHitEvent> e) {
        Entity entity = e.getArgs().entity;
        EntityType entityType = entity.getType();

        if (entityType != EntityType.SPLASH_POTION)
            return;

        // We only check island flags in relevant worlds
        if (shouldIgnoreWorldEvents(entity.getWorld())) {
            return;
        }

        BukkitEntities.getPlayerSource(entity).ifPresent(shooterPlayer -> {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                if (!preventAction(entity.getLocation(wrapper.getHandle()), IslandFlags.PVP))
                    return;
            }

            List<Entity> nearbyEntities = entity.getNearbyEntities(2, 2, 2);

            BukkitExecutor.sync(() -> nearbyEntities.forEach(nearbyEntity -> {
                if (nearbyEntity instanceof LivingEntity && !nearbyEntity.getUniqueId().equals(shooterPlayer.getUniqueId()))
                    ((LivingEntity) nearbyEntity).removePotionEffect(PotionEffectType.POISON);
            }), 1L);
        });
    }

    /* INTERNAL */

    private boolean preventAction(Location location, IslandFlag islandFlag) {
        Island island = plugin.getGrid().getIslandAt(location);
        if (island == null)
            return false;

        if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
            return false;

        return !island.hasSettingsEnabled(islandFlag);
    }

    private void registerListeners() {
        registerCallback(GameEventType.ENTITY_EXPLODE_EVENT, GameEventPriority.LOWEST, this::onEntityExplode);
        registerCallback(GameEventType.ENTITY_CHANGE_BLOCK_EVENT, GameEventPriority.LOWEST, this::onEntityChangeBlock);
        registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.LOWEST, this::onEntitySpawn);
        registerCallback(GameEventType.HANGING_BREAK_EVENT, GameEventPriority.LOWEST, this::onHangingBreakByEntity);
        registerCallback(GameEventType.ENTITY_DAMAGE_EVENT, GameEventPriority.MONITOR, this::onFireballDamage);
        registerCallback(GameEventType.BLOCK_FROM_TO_EVENT, GameEventPriority.LOWEST, this::onBlockFlow);
        registerCallback(GameEventType.BLOCK_GROW_EVENT, GameEventPriority.LOWEST, this::onCropsGrowth);
        registerCallback(GameEventType.STRUCTURE_GROW_EVENT, GameEventPriority.LOWEST, this::onTreeGrowth);
        registerCallback(GameEventType.BLOCK_BURN_EVENT, GameEventPriority.LOWEST, this::onFireSpread);
        registerCallback(GameEventType.BLOCK_IGNITE_EVENT, GameEventPriority.LOWEST, this::onBlockIgnite);
        registerCallback(GameEventType.ENTITY_CHANGE_BLOCK_EVENT, GameEventPriority.LOWEST, this::onEndermanGrief);
        registerCallback(GameEventType.PROJECTILE_HIT_EVENT, GameEventPriority.LOWEST, this::onPoisonAttack);
        plugin.getPluginEventsDispatcher().registerCallback(PluginEventType.SPAWN_UPDATE_EVENT, this::onSpawnUpdate);
    }

    private boolean shouldIgnoreWorldEvents(@Nullable World world) {
        return world == null || (world != this.spawnIslandWorld && !plugin.getGrid().isIslandsWorld(world));
    }

    private static EnumSet<CreatureSpawnEvent.SpawnReason> initializeNaturalSpawnReasons() {
        EnumSet<CreatureSpawnEvent.SpawnReason> naturalSpawnReasons = EnumSet.noneOf(CreatureSpawnEvent.SpawnReason.class);

        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "JOCKEY")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "CHUNK_GEN")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "NATURAL")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "TRAP")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "MOUNT")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "VILLAGE_INVASION")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "VILLAGE_DEFENSE")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "PATROL")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "BEEHIVE")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "LIGHTNING")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "DEFAULT")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "JOCKEY")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "JOCKEY")).ifPresent(naturalSpawnReasons::add);
        Optional.ofNullable(EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "JOCKEY")).ifPresent(naturalSpawnReasons::add);

        return naturalSpawnReasons;
    }

}
