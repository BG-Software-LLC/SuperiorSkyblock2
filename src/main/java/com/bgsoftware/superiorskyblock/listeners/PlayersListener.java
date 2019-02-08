package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class PlayersListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public PlayersListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        new PlayerArrowPickup();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        if(!superiorPlayer.getName().equals(e.getPlayer().getName())){
            superiorPlayer.updateName();
        }
        plugin.getNMSAdapter().setSkinTexture(superiorPlayer);

        Island island = superiorPlayer.getIsland();

        if(island != null)
            island.sendMessage(Locale.PLAYER_JOIN_ANNOUNCEMENT.getMessage(superiorPlayer.getName()), superiorPlayer.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = superiorPlayer.getIsland();

        if(island != null)
            island.sendMessage(Locale.PLAYER_QUIT_ANNOUNCEMENT.getMessage(superiorPlayer.getName()), superiorPlayer.getUniqueId());
    }

    @EventHandler
    public void onIslandJoin(IslandEnterEvent e){
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getNMSAdapter().setWorldBorder(e.getPlayer(), e.getIsland()), 5L);
    }

    @EventHandler
    public void onIslandJoin(IslandLeaveEvent e){
        plugin.getNMSAdapter().setWorldBorder(e.getPlayer(), null);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        if(!(e.getEntity() instanceof Player))
            return;

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of((Player) e.getEntity());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null || (plugin.getSettings().spawnPvp && island instanceof SpawnIsland))
            return;

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

        if(damagerPlayer.equals(targetPlayer))
            return;

        e.setCancelled(true);
        Locale.HIT_PLAYER_IN_ISLAND.send(damagerPlayer);
    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Painting || e.getEntity() instanceof ItemFrame)
            return;

        if(e.getEntity() instanceof Player || !(e.getDamager() instanceof Player))
            return;

        SuperiorPlayer damagerPlayer = SSuperiorPlayer.of((Player) e.getDamager());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island != null && !island.hasPermission(damagerPlayer, IslandPermission.ANIMAL_DAMAGE)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(damagerPlayer);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent e){
        if(e.getRightClicked() instanceof Painting || e.getRightClicked() instanceof ItemFrame)
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());

        if(island != null && !island.hasPermission(superiorPlayer, IslandPermission.ANIMAL_BREED)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerAsyncChat(AsyncPlayerChatEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = superiorPlayer.getIsland();

        if(superiorPlayer.hasTeamChatEnabled()){
            e.setCancelled(true);
            island.sendMessage(Locale.TEAM_CHAT_FORMAT.getMessage(superiorPlayer.getIslandRole(), superiorPlayer.getName(), e.getMessage()));
        }

        else {
            e.setFormat(e.getFormat()
                    .replace("{island-level}", String.valueOf(island == null ? 0 : island.getLevelAsString()))
                    .replace("{island-worth}", String.valueOf(island == null ? 0 : island.getWorthAsString())));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if(e.getItem() == null || e.getItem().getType() != Materials.GOLDEN_AXE.toBukkitType() ||
                !(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(!superiorPlayer.hasSchematicModeEnabled())
            return;

        e.setCancelled(true);

        if(e.getAction().name().contains("RIGHT")){
            Locale.SCHEMATIC_RIGHT_SELECT.send(superiorPlayer, SBlockPosition.of(e.getClickedBlock().getLocation()));
            superiorPlayer.setSchematicPos1(e.getClickedBlock());
        }
        else{
            Locale.SCHEMATIC_LEFT_SELECT.send(superiorPlayer, SBlockPosition.of(e.getClickedBlock().getLocation()));
            superiorPlayer.setSchematicPos2(e.getClickedBlock());
        }

        if(superiorPlayer.getSchematicPos1() != null && superiorPlayer.getSchematicPos2() != null)
            Locale.SCHEMATIC_READY_TO_CREATE.send(superiorPlayer);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

        if(island != null && !island.hasPermission(superiorPlayer, IslandPermission.DROP_ITEMS)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler
    public void onPlayerItemPickup(PlayerPickupItemEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

        if(island != null && !island.hasPermission(superiorPlayer, IslandPermission.PICKUP_DROPS)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    private Set<UUID> noFallDamage = new HashSet<>();

    @EventHandler
    public void onPlayerFall(PlayerMoveEvent e){
        if(!plugin.getSettings().voidTeleport)
            return;

        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockY() == to.getBlockY() || to.getBlockY() >= 0)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island == null)
            island = plugin.getGrid().getSpawnIsland();

        noFallDamage.add(e.getPlayer().getUniqueId());
        e.getPlayer().teleport(island.getCenter().add(0, 1, 0));
        Bukkit.getScheduler().runTaskLater(plugin, () -> noFallDamage.remove(e.getPlayer().getUniqueId()), 20L);
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent e){
        if(e.getEntity() instanceof Player && e.getCause() == EntityDamageEvent.DamageCause.FALL && noFallDamage.contains(e.getEntity().getUniqueId()))
            e.setCancelled(true);
    }

    class PlayerArrowPickup implements Listener{

        PlayerArrowPickup(){
            if(load())
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        boolean load(){
            try{
                Class.forName("org.bukkit.event.player.PlayerPickupArrowEvent");
                return true;
            }catch(ClassNotFoundException ex){
                return false;
            }
        }

        @EventHandler
        public void onPlayerArrowPickup(PlayerPickupArrowEvent e){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
            Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

            if(island != null && !island.hasPermission(superiorPlayer, IslandPermission.PICKUP_DROPS)){
                e.setCancelled(true);
                Locale.sendProtectionMessage(superiorPlayer);
            }
        }

    }

}
