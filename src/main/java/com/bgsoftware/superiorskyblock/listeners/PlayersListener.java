package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveProtectedEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIslandEnter(IslandEnterEvent e){
        if(e.getIsland().isBanned(e.getPlayer())) {
            e.setCancelled(true);
            Locale.BANNED_FROM_ISLAND.send(e.getPlayer());
            return;
        }

        IslandEnterProtectedEvent islandEnterProtectedEvent = new IslandEnterProtectedEvent(e.getPlayer(), e.getIsland());
        Bukkit.getPluginManager().callEvent(islandEnterProtectedEvent);
        if(islandEnterProtectedEvent.isCancelled())
            e.setCancelled(true);
    }

    @EventHandler
    public void onIslandEnterProtected(IslandEnterProtectedEvent e){
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                plugin.getNMSAdapter().setWorldBorder(e.getPlayer(), plugin.getGrid().getIslandAt(e.getPlayer().getLocation())), 5L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onIslandLeave(IslandLeaveEvent e){
        IslandLeaveProtectedEvent islandLeaveProtectedEvent = new IslandLeaveProtectedEvent(e.getPlayer(), e.getIsland());
        Bukkit.getPluginManager().callEvent(islandLeaveProtectedEvent);
        if(islandLeaveProtectedEvent.isCancelled())
            e.setCancelled(true);
    }

    @EventHandler
    public void onIslandLeaveProtected(IslandLeaveProtectedEvent e){
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(e.getPlayer().asOfflinePlayer().isOnline())
                plugin.getNMSAdapter().setWorldBorder(e.getPlayer(), plugin.getGrid().getIslandAt(e.getPlayer().getLocation()));
        }, 5L);
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

        //Disable flame
        if(e.getDamager() instanceof Arrow && targetPlayer.asPlayer().getFireTicks() > 0)
            targetPlayer.asPlayer().setFireTicks(0);

        Locale.HIT_PLAYER_IN_ISLAND.send(damagerPlayer);
    }

    @EventHandler
    public void onPoisonAttack(ProjectileHitEvent e){
        if(e.getEntityType().name().equals("SPLASH_POTION") || !(e.getEntity().getShooter() instanceof Player))
            return;

        SuperiorPlayer damagerPlayer = SSuperiorPlayer.of((Player) e.getEntity().getShooter());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null || (plugin.getSettings().spawnPvp && island instanceof SpawnIsland))
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
    public void onVisitorDamage(EntityDamageEvent e){
        if(!(e.getEntity() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of((Player) e.getEntity());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island != null && !island.isMember(superiorPlayer) && !plugin.getSettings().visitorsDamage)
            e.setCancelled(true);
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerAsyncChat(AsyncPlayerChatEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = superiorPlayer.getIsland();

        if(superiorPlayer.hasTeamChatEnabled()){
            if (superiorPlayer.getIsland() == null)
                superiorPlayer.toggleTeamChat();
            else {
                e.setCancelled(true);
                island.sendMessage(Locale.TEAM_CHAT_FORMAT.getMessage(superiorPlayer.getIslandRole(), superiorPlayer.getName(), e.getMessage()));
                return;
            }
        }

        e.setFormat(e.getFormat()
                .replace("{island-level}", String.valueOf(island == null ? 0 : island.getIslandLevelAsBigDecimal()))
                .replace("{island-worth}", String.valueOf(island == null ? 0 : island.getWorthAsBigDecimal())));

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
        e.getPlayer().teleport(island.getTeleportLocation().add(0, 1, 0));
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
