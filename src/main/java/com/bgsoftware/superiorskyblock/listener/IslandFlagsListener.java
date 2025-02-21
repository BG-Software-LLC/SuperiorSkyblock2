package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalMap;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class IslandFlagsListener extends AbstractGameEventListener {

    private static final EnumSet<CreatureSpawnEvent.SpawnReason> NATURAL_SPAWN_REASONS = initializeNaturalSpawnReasons();

    private final Map<UUID, ProjectileSource> originalFireballsDamager = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);

    public IslandFlagsListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        this.registerListeners();
    }

    /* ENTITY SPAWNING */

    private void onAttemptEntitySpawn(GameEvent<GameEventArgs.AttemptEntitySpawnEvent> e) {
        EntityType entityType = e.getArgs().entityType;
        CreatureSpawnEvent.SpawnReason spawnReason = e.getArgs().spawnReason;

        IslandFlag actionFlag;

        if (spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER ||
                spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            switch (BukkitEntities.getCategory(entityType)) {
                case ANIMAL:
                    actionFlag = IslandFlags.SPAWNER_ANIMALS_SPAWN;
                    break;
                case MONSTER:
                    actionFlag = IslandFlags.SPAWNER_MONSTER_SPAWN;
                    break;
                default:
                    return;
            }
        } else if (NATURAL_SPAWN_REASONS.contains(spawnReason)) {
            switch (BukkitEntities.getCategory(entityType)) {
                case ANIMAL:
                    actionFlag = IslandFlags.NATURAL_ANIMALS_SPAWN;
                    break;
                case MONSTER:
                    actionFlag = IslandFlags.NATURAL_MONSTER_SPAWN;
                    break;
                default:
                    return;
            }
        } else {
            return;
        }

        Location location = e.getArgs().spawnLocation;
        if (!preventAction(location, actionFlag))
            return;

        e.setCancelled();
    }

    /* ENTITY EXPLOSIONS */

    private void onHangingBreakByEntity(GameEvent<GameEventArgs.HangingBreakEvent> e) {
        Entity entity = e.getArgs().entity;

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
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventEntityExplosion(entity, entity.getLocation(wrapper.getHandle())))
                e.getArgs().blocks.clear();
        }
    }

    private void onEntityChangeBlock(GameEvent<GameEventArgs.EntityChangeBlockEvent> e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventEntityExplosion(e.getArgs().entity, e.getArgs().block.getLocation(wrapper.getHandle())))
                e.setCancelled();
        }
    }

    private void onFireballDamage(GameEvent<GameEventArgs.EntityDamageEvent> e) {
        Entity entity = e.getArgs().entity;
        if (entity instanceof Fireball) {
            originalFireballsDamager.put(entity.getUniqueId(), ((Fireball) entity).getShooter());
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
                ProjectileSource projectileSource = originalFireballsDamager.get(source.getUniqueId());
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

        IslandFlag islandFlag = plugin.getNMSWorld().isWaterLogged(block) ?
                IslandFlags.WATER_FLOW : IslandFlags.LAVA_FLOW;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(toBlock.getLocation(wrapper.getHandle()), islandFlag))
                e.setCancelled();
        }
    }

    private void onCropsGrowth(GameEvent<GameEventArgs.BlockGrowEvent> e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(e.getArgs().block.getLocation(wrapper.getHandle()), IslandFlags.CROPS_GROWTH))
                e.setCancelled();
        }
    }

    private void onTreeGrowth(GameEvent<GameEventArgs.StructureGrowEvent> e) {
        if (preventAction(e.getArgs().location, IslandFlags.TREE_GROWTH))
            e.setCancelled();
    }

    private void onFireSpread(GameEvent<GameEventArgs.BlockBurnEvent> e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(e.getArgs().block.getLocation(wrapper.getHandle()), IslandFlags.FIRE_SPREAD))
                e.setCancelled();
        }
    }

    private void onBlockIgnite(GameEvent<GameEventArgs.BlockIgniteEvent> e) {
        if (e.getArgs().igniteCause != BlockIgniteEvent.IgniteCause.SPREAD)
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(e.getArgs().block.getLocation(wrapper.getHandle()), IslandFlags.FIRE_SPREAD))
                e.setCancelled();
        }
    }

    private void onEndermanGrief(GameEvent<GameEventArgs.EntityChangeBlockEvent> e) {
        if (!(e.getArgs().entity instanceof Enderman))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(e.getArgs().block.getLocation(wrapper.getHandle()), IslandFlags.ENDERMAN_GRIEF))
                e.setCancelled();
        }
    }

    private void onEggLay(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        if (!(e.getArgs().entity instanceof Item))
            return;

        Item item = (Item) e.getArgs().entity;

        if (item.getItemStack().getType() != Material.EGG)
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventAction(item.getLocation(wrapper.getHandle()), IslandFlags.EGG_LAY)) {
                for (Entity entity : item.getNearbyEntities(1, 1, 1)) {
                    if (entity instanceof Chicken) {
                        e.setCancelled();
                        return;
                    }
                }
            }
        }
    }

    private void onPoisonAttack(GameEvent<GameEventArgs.ProjectileHitEvent> e) {
        Entity entity = e.getArgs().entity;
        EntityType entityType = entity.getType();

        if (entityType != EntityType.SPLASH_POTION)
            return;

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

    private boolean preventAction(Location location, IslandFlag islandFlag, Flag... flags) {
        Island island = plugin.getGrid().getIslandAt(location);

        if (island == null) {
            EnumSet<Flag> flagsSet = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));
            return !flagsSet.contains(Flag.ALLOW_OUTSIDE) && plugin.getGrid().isIslandsWorld(location.getWorld());
        }

        if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
            return false;

        return !island.hasSettingsEnabled(islandFlag);
    }

    private void registerListeners() {
        registerCallback(GameEventType.ATTEMPT_ENTITY_SPAWN_EVENT, GameEventPriority.LOWEST, this::onAttemptEntitySpawn);
        registerCallback(GameEventType.ENTITY_EXPLODE_EVENT, GameEventPriority.LOWEST, this::onEntityExplode);
        registerCallback(GameEventType.ENTITY_CHANGE_BLOCK_EVENT, GameEventPriority.LOWEST, this::onEntityChangeBlock);
        registerCallback(GameEventType.HANGING_BREAK_EVENT, GameEventPriority.LOWEST, this::onHangingBreakByEntity);
        registerCallback(GameEventType.ENTITY_DAMAGE_EVENT, GameEventPriority.MONITOR, this::onFireballDamage);
        registerCallback(GameEventType.BLOCK_FROM_TO_EVENT, GameEventPriority.LOWEST, this::onBlockFlow);
        registerCallback(GameEventType.BLOCK_GROW_EVENT, GameEventPriority.LOWEST, this::onCropsGrowth);
        registerCallback(GameEventType.STRUCTURE_GROW_EVENT, GameEventPriority.LOWEST, this::onTreeGrowth);
        registerCallback(GameEventType.BLOCK_BURN_EVENT, GameEventPriority.LOWEST, this::onFireSpread);
        registerCallback(GameEventType.BLOCK_IGNITE_EVENT, GameEventPriority.LOWEST, this::onBlockIgnite);
        registerCallback(GameEventType.ENTITY_CHANGE_BLOCK_EVENT, GameEventPriority.LOWEST, this::onEndermanGrief);
        registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.LOWEST, this::onEggLay);
        registerCallback(GameEventType.PROJECTILE_HIT_EVENT, GameEventPriority.LOWEST, this::onPoisonAttack);
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

    private enum Flag {

        ALLOW_OUTSIDE

    }

}
