package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.wrappers.WrappedLocation;
import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings("unused")
public class PlayersListener implements Listener {

    private SuperiorSkyblock plugin;

    public PlayersListener(SuperiorSkyblock plugin){
        this.plugin = plugin;
        new PlayerArrowPickup();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        WrappedPlayer wrappedPlayer = plugin.getPlayers().getWrappedPlayer(e.getPlayer().getUniqueId());
        if(!wrappedPlayer.getName().equals(e.getPlayer().getName())){
            wrappedPlayer.updateName();
        }
        plugin.getNMSAdapter().setSkinTexture(wrappedPlayer);

        Island island = wrappedPlayer.getIsland();

        if(island != null)
            island.sendMessage(Locale.PLAYER_JOIN_ANNOUNCEMENT.getMessage(wrappedPlayer.getName()), wrappedPlayer.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        Island island = wrappedPlayer.getIsland();

        if(island != null)
            island.sendMessage(Locale.PLAYER_QUIT_ANNOUNCEMENT.getMessage(wrappedPlayer.getName()), wrappedPlayer.getUniqueId());
    }

    @EventHandler
    public void onIslandJoin(IslandEnterEvent e){
        plugin.getNMSAdapter().setWorldBorder(e.getPlayer(), e.getIsland());
    }

    @EventHandler
    public void onIslandJoin(IslandLeaveEvent e){
        plugin.getNMSAdapter().setWorldBorder(e.getPlayer(), null);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        if(!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player))
            return;

        WrappedPlayer targetPlayer = WrappedPlayer.of((Player) e.getEntity());
        WrappedPlayer damagerPlayer = WrappedPlayer.of((Player) e.getDamager());

        if(targetPlayer.getIsland() != null && targetPlayer.getIsland().equals(damagerPlayer.getIsland())){
            e.setCancelled(true);
            Locale.HIT_TEAM_MEMBER.send(targetPlayer);
        }

    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player || !(e.getDamager() instanceof Player))
            return;

        WrappedPlayer damagerPlayer = WrappedPlayer.of((Player) e.getDamager());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island != null && !island.hasPermission(damagerPlayer, IslandPermission.ANIMAL_DAMAGE)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(damagerPlayer);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent e){
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());

        if(island != null && !island.hasPermission(wrappedPlayer, IslandPermission.ANIMAL_BREED)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(wrappedPlayer);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerAsyncChat(AsyncPlayerChatEvent e){
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        Island island = wrappedPlayer.getIsland();

        if(wrappedPlayer.hasTeamChatEnabled()){
            e.setCancelled(true);
            island.sendMessage(Locale.TEAM_CHAT_FORMAT.getMessage(wrappedPlayer.getIslandRole(), wrappedPlayer.getName(), e.getMessage()));
        }

        else {
            e.setFormat(e.getFormat()
                    .replace("{island-level}", String.valueOf(island == null ? 0 : island.getIslandLevel()))
                    .replace("{island-worth}", String.valueOf(island == null ? 0 : (int) island.getWorth())));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if(e.getItem() == null || e.getItem().getType() != Materials.GOLDEN_AXE.toBukkitType() ||
                !(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK))
            return;

        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());

        if(!wrappedPlayer.hasSchematicModeEnabled())
            return;

        e.setCancelled(true);

        if(e.getAction().name().contains("RIGHT")){
            Locale.SCHEMATIC_RIGHT_SELECT.send(wrappedPlayer, WrappedLocation.of(e.getClickedBlock().getLocation()));
            wrappedPlayer.setSchematicPos1(e.getClickedBlock());
        }
        else{
            Locale.SCHEMATIC_LEFT_SELECT.send(wrappedPlayer, WrappedLocation.of(e.getClickedBlock().getLocation()));
            wrappedPlayer.setSchematicPos2(e.getClickedBlock());
        }

        if(wrappedPlayer.getSchematicPos1() != null && wrappedPlayer.getSchematicPos2() != null)
            Locale.SCHEMATIC_READY_TO_CREATE.send(wrappedPlayer);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(wrappedPlayer.getLocation());

        if(island != null && !island.hasPermission(wrappedPlayer, IslandPermission.DROP_ITEMS)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(wrappedPlayer);
        }
    }

    @EventHandler
    public void onPlayerItemPickup(PlayerPickupItemEvent e){
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(wrappedPlayer.getLocation());

        if(island != null && !island.hasPermission(wrappedPlayer, IslandPermission.PICKUP_DROPS)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(wrappedPlayer);
        }
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
            WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
            Island island = plugin.getGrid().getIslandAt(wrappedPlayer.getLocation());

            if(island != null && !island.hasPermission(wrappedPlayer, IslandPermission.PICKUP_DROPS)){
                e.setCancelled(true);
                Locale.sendProtectionMessage(wrappedPlayer);
            }
        }

    }

}
