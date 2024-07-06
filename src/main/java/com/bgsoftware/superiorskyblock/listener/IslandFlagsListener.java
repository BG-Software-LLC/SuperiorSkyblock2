package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalMap;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class IslandFlagsListener implements Listener {

    private final Map<UUID, ProjectileSource> originalFireballsDamager = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);

    private final SuperiorSkyblockPlugin plugin;

    public IslandFlagsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.registerPaperListener();
    }

    /* ENTITY SPAWNING */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntitySpawn(CreatureSpawnEvent e) {
        if (preventEntitySpawn(e.getLocation(), e.getSpawnReason(), e.getEntityType()))
            e.setCancelled(true);
    }

    public boolean preventEntitySpawn(Location location, CreatureSpawnEvent.SpawnReason spawnReason, EntityType entityType) {
        IslandFlag actionFlag;

        switch (spawnReason.name()) {
            case "JOCKEY":
            case "CHUNK_GEN":
            case "NATURAL":
            case "TRAP":
            case "MOUNT":
            case "VILLAGE_INVASION":
            case "VILLAGE_DEFENSE":
            case "PATROL":
            case "BEEHIVE": {
                switch (BukkitEntities.getCategory(entityType)) {
                    case ANIMAL:
                        actionFlag = IslandFlags.NATURAL_ANIMALS_SPAWN;
                        break;
                    case MONSTER:
                        actionFlag = IslandFlags.NATURAL_MONSTER_SPAWN;
                        break;
                    default:
                        return false;
                }
                break;
            }
            case "SPAWNER":
            case "SPAWNER_EGG": {
                switch (BukkitEntities.getCategory(entityType)) {
                    case ANIMAL:
                        actionFlag = IslandFlags.SPAWNER_ANIMALS_SPAWN;
                        break;
                    case MONSTER:
                        actionFlag = IslandFlags.SPAWNER_MONSTER_SPAWN;
                        break;
                    default:
                        return false;
                }
                break;
            }
            default:
                return false;
        }

        return preventAction(location, actionFlag);
    }

    /* ENTITY EXPLOSIONS */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        if (e.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            if (e.getRemover() instanceof Player) {
                // Explosion was set by TNT.
                if (preventAction(e.getEntity().getLocation(), IslandFlags.TNT_EXPLOSION)) {
                    e.setCancelled(true);
                    return;
                }
            } else if (e.getRemover() instanceof Ghast) {
                // Explosion was set by TNT.
                if (preventAction(e.getEntity().getLocation(), IslandFlags.GHAST_FIREBALL)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (preventEntityExplosion(e.getRemover(), e.getEntity().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityExplode(EntityExplodeEvent e) {
        if (preventEntityExplosion(e.getEntity(), e.getLocation()))
            e.blockList().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (preventEntityExplosion(e.getEntity(), e.getBlock().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onFireballDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Fireball) {
            originalFireballsDamager.put(e.getEntity().getUniqueId(), ((Fireball) e.getEntity()).getShooter());
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onBlockFlow(BlockFromToEvent e) {
        IslandFlag islandFlag = plugin.getNMSWorld().isWaterLogged(e.getBlock()) ?
                IslandFlags.WATER_FLOW : IslandFlags.LAVA_FLOW;
        if (preventAction(e.getToBlock().getLocation(), islandFlag))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onCropsGrowth(BlockGrowEvent e) {
        if (preventAction(e.getBlock().getLocation(), IslandFlags.CROPS_GROWTH))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onTreeGrowth(StructureGrowEvent e) {
        if (preventAction(e.getLocation(), IslandFlags.TREE_GROWTH))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onFireSpread(BlockBurnEvent e) {
        if (preventAction(e.getBlock().getLocation(), IslandFlags.FIRE_SPREAD))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getCause() == BlockIgniteEvent.IgniteCause.SPREAD && preventAction(e.getBlock().getLocation(), IslandFlags.FIRE_SPREAD))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEndermanGrief(EntityChangeBlockEvent e) {
        if (e.getEntity() instanceof Enderman && preventAction(e.getBlock().getLocation(), IslandFlags.ENDERMAN_GRIEF))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEggLay(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getType() == Material.EGG && preventAction(e.getEntity().getLocation(), IslandFlags.EGG_LAY)) {
            for (Entity entity : e.getEntity().getNearbyEntities(1, 1, 1)) {
                if (entity instanceof Chicken) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPoisonAttack(ProjectileHitEvent e) {
        if (e.getEntityType() != EntityType.SPLASH_POTION)
            return;

        BukkitEntities.getPlayerSource(e.getEntity()).ifPresent(shooterPlayer -> {
            if (!preventAction(e.getEntity().getLocation(), IslandFlags.PVP))
                return;

            List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(2, 2, 2);

            BukkitExecutor.sync(() -> nearbyEntities.forEach(entity -> {
                if (entity instanceof LivingEntity && !entity.getUniqueId().equals(shooterPlayer.getUniqueId()))
                    ((LivingEntity) entity).removePotionEffect(PotionEffectType.POISON);
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

    private void registerPaperListener() {
        try {
            Class.forName("com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent");
            Bukkit.getPluginManager().registerEvents(new PaperListener(), plugin);
        } catch (Throwable ignored) {
        }
    }

    private class PaperListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        private void onEntitySpawn(PreCreatureSpawnEvent e) {
            if (preventEntitySpawn(e.getSpawnLocation(), e.getReason(), e.getType())) {
                e.setCancelled(true);
                e.setShouldAbortSpawn(true);
            }
        }

    }

    private enum Flag {

        ALLOW_OUTSIDE

    }

}
