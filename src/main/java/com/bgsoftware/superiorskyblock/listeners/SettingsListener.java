package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;

import com.bgsoftware.superiorskyblock.wrappers.player.SuperiorNPCPlayer;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

@SuppressWarnings("unused")
public final class SettingsListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public SettingsListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getLocation());
        boolean animal = e.getEntity() instanceof Animals;

        if(island != null){
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            switch (e.getSpawnReason()){
                case JOCKEY:
                case CHUNK_GEN:
                case NATURAL:
                    if(!island.hasSettingsEnabled(animal ? IslandFlags.NATURAL_ANIMALS_SPAWN : IslandFlags.NATURAL_MONSTER_SPAWN))
                        e.setCancelled(true);
                    break;
                case SPAWNER:
                case SPAWNER_EGG:
                    if(!island.hasSettingsEnabled(animal ? IslandFlags.SPAWNER_ANIMALS_SPAWN : IslandFlags.SPAWNER_MONSTER_SPAWN))
                        e.setCancelled(true);
                    break;
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandJoin(IslandEnterEvent e){
        Player player = e.getPlayer().asPlayer();
        Island island = e.getIsland();

        if(!plugin.getSettings().spawnProtection && island.isSpawn())
            return;

        if(island.hasSettingsEnabled(IslandFlags.ALWAYS_DAY)){
            player.setPlayerTime(0, false);
        }
        else if(island.hasSettingsEnabled(IslandFlags.ALWAYS_MIDDLE_DAY)){
            player.setPlayerTime(6000, false);
        }
        else if(island.hasSettingsEnabled(IslandFlags.ALWAYS_NIGHT)){
            player.setPlayerTime(14000, false);
        }
        else if(island.hasSettingsEnabled(IslandFlags.ALWAYS_MIDDLE_NIGHT)){
            player.setPlayerTime(18000, false);
        }

        if(island.hasSettingsEnabled(IslandFlags.ALWAYS_SHINY)){
            player.setPlayerWeather(WeatherType.CLEAR);
        }
        else if(island.hasSettingsEnabled(IslandFlags.ALWAYS_RAIN)){
            player.setPlayerWeather(WeatherType.DOWNFALL);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandLeave(IslandLeaveEvent e){
        Player player = e.getPlayer().asPlayer();
        player.resetPlayerTime();
        player.resetPlayerWeather();
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
    public void onFireSpread(BlockSpreadEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null){
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(e.getNewState().getType() == Material.FIRE && !island.hasSettingsEnabled(IslandFlags.FIRE_SPREAD))
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
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        if(!(e.getEntity() instanceof Player))
            return;

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of((Player) e.getEntity());

        if(targetPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        SuperiorPlayer damagerPlayer;

        if(e.getDamager() instanceof Player){
            damagerPlayer = SSuperiorPlayer.of((Player) e.getDamager());
        }

        else if(e.getDamager() instanceof Projectile){
            ProjectileSource shooter = ((Projectile) e.getDamager()).getShooter();
            if(shooter instanceof Player)
                damagerPlayer = SSuperiorPlayer.of((Player) ((Projectile) e.getDamager()).getShooter());
            else return;
        }

        else return;

        boolean cancelFlames = false;

        if(!damagerPlayer.equals(targetPlayer) && damagerPlayer.getIslandLeader().equals(targetPlayer.getIslandLeader()) &&
                !plugin.getSettings().pvpWorlds.contains(targetPlayer.getWorld().getName())){
            e.setCancelled(true);
            Locale.HIT_ISLAND_MEMBER.send(damagerPlayer);
            cancelFlames = true;
        }

        else if (!damagerPlayer.equals(targetPlayer) && island != null && (plugin.getSettings().spawnProtection || !island.isSpawn()) &&
                !island.hasSettingsEnabled(IslandFlags.PVP)) {
            e.setCancelled(true);
            Locale.HIT_PLAYER_IN_ISLAND.send(damagerPlayer);
            cancelFlames = true;
        }

        //Disable flame
        if(cancelFlames && e.getDamager() instanceof Arrow && targetPlayer.asPlayer().getFireTicks() > 0)
            targetPlayer.asPlayer().setFireTicks(0);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPoisonAttack(ProjectileHitEvent e){
        if(!e.getEntityType().name().equals("SPLASH_POTION") || !(e.getEntity().getShooter() instanceof Player))
            return;

        SuperiorPlayer damagerPlayer = SSuperiorPlayer.of((Player) e.getEntity().getShooter());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null || (!plugin.getSettings().spawnProtection && island.isSpawn()) || island.hasSettingsEnabled(IslandFlags.PVP))
            return;

        for(Entity entity : e.getEntity().getNearbyEntities(2, 2, 2)){
            if(entity instanceof Player){
                SuperiorPlayer targetPlayer = SSuperiorPlayer.of((Player) entity);

                if(damagerPlayer.equals(targetPlayer))
                    continue;

                targetPlayer.asPlayer().removePotionEffect(PotionEffectType.POISON);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());
        if(island != null){
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if((e.getEntity() instanceof Creeper && !island.hasSettingsEnabled(IslandFlags.CREEPER_EXPLOSION)) ||
                    e.getEntity() instanceof TNTPrimed && !island.hasSettingsEnabled(IslandFlags.TNT_EXPLOSION) ||
                    ((e.getEntity() instanceof Wither || e.getEntity() instanceof WitherSkull) && !island.hasSettingsEnabled(IslandFlags.WITHER_EXPLOSION)) ||
                    (e.getEntity() instanceof Fireball && !island.hasSettingsEnabled(IslandFlags.GHAST_FIREBALL)))
            e.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null) {
            if(!plugin.getSettings().spawnProtection && island.isSpawn())
                return;

            if(((e.getEntity() instanceof Wither) && !island.hasSettingsEnabled(IslandFlags.WITHER_EXPLOSION)) ||
                    ((e.getEntity() instanceof Enderman) && !island.hasSettingsEnabled(IslandFlags.ENDERMAN_GRIEF)))
                e.setCancelled(true);
        }
    }

}
