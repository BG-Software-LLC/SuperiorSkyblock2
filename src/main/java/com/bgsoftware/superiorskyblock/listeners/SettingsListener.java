package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.flags.IslandFlags;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.lang.PlayerLocales;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.logic.BlocksLogic;
import com.bgsoftware.superiorskyblock.world.blocks.ICachedBlock;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
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
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public final class SettingsListener implements Listener {

    private static final ReflectMethod<Block> PROJECTILE_HIT_EVENT_TARGET_BLOCK =
            new ReflectMethod<>(ProjectileHitEvent.class, "getHitBlock");

    private static final LoadingCache<UUID, Optional<ProjectileSource>> originalFireballsDamager = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build(new CacheLoader<UUID, Optional<ProjectileSource>>() {
                @Override
                public Optional<ProjectileSource> load(@NotNull UUID uuid) {
                    return Optional.empty();
                }
            });

    private final SuperiorSkyblockPlugin plugin;

    public SettingsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        try {
            Class.forName("com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent");
            Bukkit.getPluginManager().registerEvents(new PaperListener(), plugin);
        } catch (Throwable ignored) {
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e) {
        if (shouldBlockEntitySpawn(e.getLocation(), e.getSpawnReason(), e.getEntityType()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFlow(BlockFromToEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getToBlock().getLocation());

        if (island != null) {
            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return;

            if (!island.hasSettingsEnabled(e.getBlock().getType().name().contains("WATER") ? IslandFlags.WATER_FLOW : IslandFlags.LAVA_FLOW))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCropsGrowth(BlockGrowEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if (island != null) {
            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return;

            if (!island.hasSettingsEnabled(IslandFlags.CROPS_GROWTH))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrowth(StructureGrowEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getLocation());

        if (island != null) {
            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return;

            if (!island.hasSettingsEnabled(IslandFlags.TREE_GROWTH))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFireSpread(BlockBurnEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if (island != null) {
            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return;

            if (!island.hasSettingsEnabled(IslandFlags.FIRE_SPREAD))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getCause() != BlockIgniteEvent.IgniteCause.SPREAD)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if (island != null) {
            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return;

            if (!island.hasSettingsEnabled(IslandFlags.FIRE_SPREAD))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEndermanGrief(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof Enderman))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if (island != null) {
            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return;

            if (!island.hasSettingsEnabled(IslandFlags.ENDERMAN_GRIEF))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEggLay(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getType() != Material.EGG)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island != null) {
            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return;

            if (!island.hasSettingsEnabled(IslandFlags.EGG_LAY)) {
                for (Entity entity : e.getEntity().getNearbyEntities(1, 1, 1)) {
                    if (entity instanceof Chicken) {
                        e.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPoisonAttack(ProjectileHitEvent e) {
        if (!e.getEntityType().name().equals("SPLASH_POTION") || !(e.getEntity().getShooter() instanceof Player))
            return;

        SuperiorPlayer damagerPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getEntity().getShooter());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island == null || (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn()) || island.hasSettingsEnabled(IslandFlags.PVP))
            return;

        for (Entity entity : e.getEntity().getNearbyEntities(2, 2, 2)) {
            if (entity instanceof Player) {
                SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer((Player) entity);

                if (damagerPlayer.equals(targetPlayer))
                    continue;

                ((Player) entity).removePotionEffect(PotionEffectType.POISON);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBowAttackChorus(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Arrow) || !(e.getEntity().getShooter() instanceof Player) ||
                !PROJECTILE_HIT_EVENT_TARGET_BLOCK.isValid())
            return;

        SuperiorPlayer damagerPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getEntity().getShooter());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island == null || (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn()))
            return;

        Block hitBlock = PROJECTILE_HIT_EVENT_TARGET_BLOCK.invoke(e);

        if (hitBlock == null || !hitBlock.getType().name().equals("CHORUS_FLOWER"))
            return;

        if (island.hasPermission(damagerPlayer, IslandPrivileges.BREAK)) {
            BlocksLogic.handleBreak(hitBlock);
        } else {
            ICachedBlock cachedBlock = plugin.getNMSWorld().cacheBlock(hitBlock);
            hitBlock.setType(Material.AIR);

            PlayerLocales.sendProtectionMessage(damagerPlayer);

            Executor.sync(() -> cachedBlock.setBlock(hitBlock.getLocation()), 1L);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplodeDamage(HangingBreakByEntityEvent e) {
        if (handleEntityExplode(e.getRemover(), e.getEntity().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (handleEntityExplode(e.getEntity(), e.getLocation()))
            e.blockList().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (handleEntityExplode(e.getEntity(), e.getBlock().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplodeDamage(EntityDamageByEntityEvent e) {
        if (handleEntityExplode(e.getDamager(), e.getEntity().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFireballDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Fireball) {
            originalFireballsDamager.put(e.getEntity().getUniqueId(),
                    Optional.of(((Fireball) e.getEntity()).getShooter()));
        }
    }

    private boolean handleEntityExplode(Entity source, Location explodeLocation) {
        Island island = plugin.getGrid().getIslandAt(explodeLocation);

        if (island == null || (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn()))
            return false;

        IslandFlag targetFlagCheck = null;

        if (source instanceof Creeper) {
            targetFlagCheck = IslandFlags.CREEPER_EXPLOSION;
        } else if (source instanceof TNTPrimed) {
            targetFlagCheck = IslandFlags.TNT_EXPLOSION;
        } else if (source instanceof Wither || source instanceof WitherSkull) {
            targetFlagCheck = IslandFlags.WITHER_EXPLOSION;
        } else if (source instanceof Fireball) {
            ProjectileSource projectileSource = originalFireballsDamager.getUnchecked(source.getUniqueId())
                    .orElse(((Fireball) source).getShooter());
            if (projectileSource instanceof Ghast) {
                targetFlagCheck = IslandFlags.GHAST_FIREBALL;
            }
        }

        return targetFlagCheck != null && !island.hasSettingsEnabled(targetFlagCheck);
    }

    private boolean shouldBlockEntitySpawn(Location location, CreatureSpawnEvent.SpawnReason spawnReason, EntityType entityType) {
        Island island = plugin.getGrid().getIslandAt(location);

        if (island != null) {
            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return false;

            switch (spawnReason.name()) {
                case "JOCKEY":
                case "CHUNK_GEN":
                case "NATURAL":
                case "TRAP":
                case "MOUNT":
                case "BEEHIVE": {
                    IslandFlag toCheck = EntityUtils.isMonster(entityType) ? IslandFlags.NATURAL_MONSTER_SPAWN :
                            EntityUtils.isAnimal(entityType) ? IslandFlags.NATURAL_ANIMALS_SPAWN : null;
                    if (toCheck != null && !island.hasSettingsEnabled(toCheck))
                        return true;
                    break;
                }
                case "SPAWNER":
                case "SPAWNER_EGG": {
                    IslandFlag toCheck = EntityUtils.isMonster(entityType) ? IslandFlags.SPAWNER_MONSTER_SPAWN :
                            EntityUtils.isAnimal(entityType) ? IslandFlags.SPAWNER_ANIMALS_SPAWN : null;
                    if (toCheck != null && !island.hasSettingsEnabled(toCheck))
                        return true;
                    break;
                }
            }
        }

        return false;
    }

    private class PaperListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onEntitySpawn(PreCreatureSpawnEvent e) {
            if (shouldBlockEntitySpawn(e.getSpawnLocation(), e.getReason(), e.getType())) {
                e.setCancelled(true);
                e.setShouldAbortSpawn(true);
            }
        }

    }

}
