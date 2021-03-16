package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
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

@SuppressWarnings("unused")
public final class SettingsListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public SettingsListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e){
        if(plugin.getGrid() == null)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getLocation());

        if(island != null){
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            switch (e.getSpawnReason().name()){
                case "JOCKEY":
                case "CHUNK_GEN":
                case "NATURAL":
                case "TRAP":
                case "MOUNT":
                    {
                    IslandFlag toCheck = EntityUtils.isMonster(e.getEntityType()) ? IslandFlags.NATURAL_MONSTER_SPAWN :
                            EntityUtils.isAnimal(e.getEntityType()) ? IslandFlags.NATURAL_ANIMALS_SPAWN : null;
                    if (toCheck != null && !island.hasSettingsEnabled(toCheck))
                        e.setCancelled(true);
                    break;
                }
                case "SPAWNER":
                case "SPAWNER_EGG": {
                    IslandFlag toCheck = EntityUtils.isMonster(e.getEntityType()) ? IslandFlags.SPAWNER_MONSTER_SPAWN :
                            EntityUtils.isAnimal(e.getEntityType()) ? IslandFlags.SPAWNER_ANIMALS_SPAWN : null;
                    if (toCheck != null && !island.hasSettingsEnabled(toCheck))
                        e.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFlow(BlockFromToEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getToBlock().getLocation());

        if(island != null) {
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(!island.hasSettingsEnabled(e.getBlock().getType().name().contains("WATER") ? IslandFlags.WATER_FLOW : IslandFlags.LAVA_FLOW))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCropsGrowth(BlockGrowEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null) {
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(!island.hasSettingsEnabled(IslandFlags.CROPS_GROWTH))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrowth(StructureGrowEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getLocation());

        if(island != null){
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(!island.hasSettingsEnabled(IslandFlags.TREE_GROWTH))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFireSpread(BlockBurnEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null) {
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(!island.hasSettingsEnabled(IslandFlags.FIRE_SPREAD))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent e){
        if(e.getCause() != BlockIgniteEvent.IgniteCause.SPREAD)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null) {
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(!island.hasSettingsEnabled(IslandFlags.FIRE_SPREAD))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEndermanGrief(EntityChangeBlockEvent e){
        if(!(e.getEntity() instanceof Enderman))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null) {
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(!island.hasSettingsEnabled(IslandFlags.ENDERMAN_GRIEF))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEggLay(ItemSpawnEvent e){
        if(e.getEntity().getItemStack().getType() != Material.EGG)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island != null){
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(!island.hasSettingsEnabled(IslandFlags.EGG_LAY)) {
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
    public void onPoisonAttack(ProjectileHitEvent e){
        if(!e.getEntityType().name().equals("SPLASH_POTION") || !(e.getEntity().getShooter() instanceof Player))
            return;

        SuperiorPlayer damagerPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getEntity().getShooter());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null || (!plugin.getSettings().spawnProtection && island.isSpawn()) || island.hasSettingsEnabled(IslandFlags.PVP))
            return;

        for(Entity entity : e.getEntity().getNearbyEntities(2, 2, 2)){
            if(entity instanceof Player){
                SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer((Player) entity);

                if(damagerPlayer.equals(targetPlayer))
                    continue;

                ((Player) entity).removePotionEffect(PotionEffectType.POISON);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplodeDamage(HangingBreakByEntityEvent e){
        if(handleEntityExplode(e.getRemover(), e.getEntity().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e){
        if(handleEntityExplode(e.getEntity(), e.getLocation()))
            e.blockList().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e){
        if(handleEntityExplode(e.getEntity(), e.getBlock().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplodeDamage(EntityDamageByEntityEvent e){
        if(handleEntityExplode(e.getDamager(), e.getEntity().getLocation()))
            e.setCancelled(true);
    }

    private boolean handleEntityExplode(Entity source, Location explodeLocation){
        Island island = plugin.getGrid().getIslandAt(explodeLocation);
        if(island != null && (plugin.getSettings().spawnProtection || !island.isSpawn())){
            if((source instanceof Creeper && !island.hasSettingsEnabled(IslandFlags.CREEPER_EXPLOSION)) ||
                    (source instanceof TNTPrimed && !island.hasSettingsEnabled(IslandFlags.TNT_EXPLOSION)) ||
                    ((source instanceof Wither || source instanceof WitherSkull) && !island.hasSettingsEnabled(IslandFlags.WITHER_EXPLOSION)) ||
                    (source instanceof Fireball && !(source instanceof WitherSkull) && !island.hasSettingsEnabled(IslandFlags.GHAST_FIREBALL))) {
                return true;
            }
        }

        return false;
    }

}
