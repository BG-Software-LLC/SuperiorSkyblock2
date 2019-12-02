package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.listeners.events.BlockGenerateEvent;
import com.bgsoftware.superiorskyblock.listeners.events.DragonEggChangeEvent;
import com.bgsoftware.superiorskyblock.listeners.events.SignBreakEvent;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameBreakEvent;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameRotationEvent;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

@SuppressWarnings("unused")
public final class CustomEventsListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public CustomEventsListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameRotation(PlayerInteractEntityEvent e){
        if((e.getRightClicked() instanceof ItemFrame)){
            ItemFrameRotationEvent itemFrameRotationEvent = new ItemFrameRotationEvent(e.getPlayer(), (ItemFrame) e.getRightClicked());
            Bukkit.getPluginManager().callEvent(itemFrameRotationEvent);
            if(itemFrameRotationEvent.isCancelled())
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameBreak(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame) {
            Player shooter;

            if(e.getDamager() instanceof Player)
                shooter = (Player) e.getDamager();
            else if(e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player)
                shooter = (Player) ((Projectile) e.getDamager()).getShooter();
            else return;

            ItemFrameBreakEvent itemFrameBreakEvent = new ItemFrameBreakEvent(shooter, (ItemFrame) e.getEntity());
            Bukkit.getPluginManager().callEvent(itemFrameBreakEvent);
            if(itemFrameBreakEvent.isCancelled())
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer == null)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());
        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());
        boolean sameWorld = e.getFrom().getWorld().equals(e.getTo().getWorld());

        if(fromIsland != null && fromIsland.equals(toIsland)) {
            //We want to update the border if player teleported between dimensions of his island.
            if(!sameWorld){
                Executor.sync(() -> plugin.getNMSAdapter().setWorldBorder(superiorPlayer, toIsland), 1L);
            }

            return;
        }

        if (fromIsland != null && !fromIsland.isSpawn()) {
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, fromIsland, IslandLeaveEvent.LeaveCause.PLAYER_TELEPORT);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
            if(islandLeaveEvent.isCancelled()) {
                e.setCancelled(true);
                return;
            }
        }

        if (toIsland != null && !toIsland.isSpawn()) {
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, toIsland, IslandEnterEvent.EnterCause.PLAYER_TELEPORT);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
            if(islandEnterEvent.isCancelled()) {
                e.setCancelled(true);
                if(islandEnterEvent.getCancelTeleport() != null)
                    e.getPlayer().teleport(islandEnterEvent.getCancelTeleport());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e){
        Executor.sync(() -> {
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

            if(superiorPlayer == null)
                return;

            Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

            if(island != null && island.isBanned(superiorPlayer)){
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                return;
            }

            if(island != null && !island.isSpawn()){
                IslandEnterEvent islandEnterEvent = new IslandEnterEvent(SSuperiorPlayer.of(e.getPlayer()), island, IslandEnterEvent.EnterCause.PLAYER_JOIN);
                Bukkit.getPluginManager().callEvent(islandEnterEvent);
                if(islandEnterEvent.isCancelled() && islandEnterEvent.getCancelTeleport() != null) {
                    e.getPlayer().teleport(islandEnterEvent.getCancelTeleport());
                }
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer == null)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island != null && !(island instanceof SpawnIsland)){
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, island, IslandLeaveEvent.LeaveCause.PLAYER_QUIT);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer == null)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getTo());

        if(island != null && !(island instanceof SpawnIsland)){
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, island, IslandEnterEvent.EnterCause.PORTAL);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
            if(islandEnterEvent.isCancelled()) {
                e.setCancelled(true);
                if(islandEnterEvent.getCancelTeleport() != null)
                    e.getPlayer().teleport(islandEnterEvent.getCancelTeleport());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e){
        if(plugin.getGrid() == null) return;

        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer == null)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(from);
        Island toIsland = plugin.getGrid().getIslandAt(to);

        if(fromIsland != null && fromIsland.equals(toIsland)){
            if(fromIsland.isInsideRange(e.getFrom()) && !fromIsland.isInsideRange(e.getTo())){
                IslandLeaveProtectedEvent islandLeaveProtectedEvent = new IslandLeaveProtectedEvent(superiorPlayer, toIsland, IslandLeaveEvent.LeaveCause.PLAYER_MOVE);
                Bukkit.getPluginManager().callEvent(islandLeaveProtectedEvent);
                if(islandLeaveProtectedEvent.isCancelled())
                    e.setCancelled(true);
            }
            else if(!fromIsland.isInsideRange(e.getFrom()) && fromIsland.isInsideRange(e.getTo())){
                IslandEnterProtectedEvent islandEnterProtectedEvent = new IslandEnterProtectedEvent(superiorPlayer, toIsland, IslandEnterEvent.EnterCause.PLAYER_MOVE);
                Bukkit.getPluginManager().callEvent(islandEnterProtectedEvent);
                if(islandEnterProtectedEvent.isCancelled()) {
                    e.setCancelled(true);
                    if(islandEnterProtectedEvent.getCancelTeleport() != null)
                        e.getPlayer().teleport(islandEnterProtectedEvent.getCancelTeleport());
                }
            }
            return;
        }

        if (fromIsland != null && !(fromIsland instanceof SpawnIsland)) {
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, fromIsland, IslandLeaveEvent.LeaveCause.PLAYER_MOVE);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
            if(islandLeaveEvent.isCancelled())
                e.setCancelled(true);
        }

        if (toIsland != null && !(toIsland instanceof SpawnIsland)) {
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, toIsland, IslandEnterEvent.EnterCause.PLAYER_MOVE);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
            if(islandEnterEvent.isCancelled()) {
                e.setCancelled(true);
                if(islandEnterEvent.getCancelTeleport() != null)
                    e.getPlayer().teleport(islandEnterEvent.getCancelTeleport());
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent e){
        if(e.getBlock().getState() instanceof Sign){
            SignBreakEvent signBreakEvent = new SignBreakEvent(SSuperiorPlayer.of(e.getPlayer()), (Sign) e.getBlock().getState());
            Bukkit.getPluginManager().callEvent(signBreakEvent);
        }else{
            for(BlockFace blockFace : BlockFace.values()){
                Block faceBlock = e.getBlock().getRelative(blockFace);
                if(faceBlock.getState() instanceof Sign){
                    boolean isSign;

                    if(ServerVersion.isLegacy()) {
                        isSign = ((org.bukkit.material.Sign) faceBlock.getState().getData()).getAttachedFace().getOppositeFace() == blockFace;
                    }
                    else {
                        isSign = ((org.bukkit.block.data.Directional) plugin.getNMSAdapter().getBlockData(faceBlock)).getFacing().getOppositeFace() == blockFace;
                    }

                    if(isSign) {
                        SignBreakEvent signBreakEvent = new SignBreakEvent(SSuperiorPlayer.of(e.getPlayer()), (Sign) faceBlock.getState());
                        Bukkit.getPluginManager().callEvent(signBreakEvent);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggChange(PlayerInteractEvent e){
        if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.DRAGON_EGG){
            DragonEggChangeEvent dragonEggChangeEvent = new DragonEggChangeEvent(e.getClickedBlock());
            Bukkit.getPluginManager().callEvent(dragonEggChangeEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggChange(BlockPistonExtendEvent e){
        for(Block block : e.getBlocks()){
            if(block.getType() == Material.DRAGON_EGG){
                DragonEggChangeEvent dragonEggChangeEvent = new DragonEggChangeEvent(e.getBlock());
                Bukkit.getPluginManager().callEvent(dragonEggChangeEvent);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggChange(EntityDamageEvent e){
        if(e.getEntity() instanceof FallingBlock && ((FallingBlock) e.getEntity()).getMaterial() == Material.DRAGON_EGG){
            DragonEggChangeEvent dragonEggChangeEvent = new DragonEggChangeEvent(e.getEntity().getLocation().getBlock());
            Bukkit.getPluginManager().callEvent(dragonEggChangeEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFromToEvent e){
        Block block = e.getToBlock();

        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        if(island == null)
            return;

        Executor.sync(() -> {
            if(block.getType() == Material.COBBLESTONE){
                Executor.async(() -> {
                    BlockGenerateEvent blockGenerateEvent = new BlockGenerateEvent(block, island);
                    Bukkit.getPluginManager().callEvent(blockGenerateEvent);

                    String[] typeSections = blockGenerateEvent.getNewStateKey().toString().split(":");

                    if(typeSections[0].contains("COBBLESTONE"))
                        return;

                    Executor.sync(() -> {
                        block.setType(Material.valueOf(typeSections[0]));
                        if(typeSections.length == 2)
                            //noinspection deprecation
                            block.setData(Byte.parseByte(typeSections[1]));

                        island.handleBlockPlace(blockGenerateEvent.getNewStateKey(), 1);

                        plugin.getNMSAdapter().playGeneratorSound(block.getLocation());
                    });
                });
            }
        }, 1L);
    }

    /*
     *  This event is used for the event-commands feature!
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandEvent(IslandEvent e){
        List<String> commands = plugin.getSettings().eventCommands.get(e.getClass().getSimpleName().toLowerCase());

        if(commands == null)
            return;

        String islandName = e.getIsland().getName(), playerName = "", schematicName = "", enterCause = "",
                targetName = "", leaveCause = "", oldOwner = "", newOwner = "", worth = "", level = "";

        switch (e.getClass().getSimpleName().toLowerCase()){
            case "islandcreateevent":
                playerName = ((IslandCreateEvent) e).getPlayer().getName();
                schematicName = ((IslandCreateEvent) e).getSchematic();
                break;
            case "islanddisbandevent":
                playerName = ((IslandDisbandEvent) e).getPlayer().getName();
                break;
            case "islandenterevent":
            case "islandenterprotectedevent":
                playerName = ((IslandEnterEvent) e).getPlayer().getName();
                enterCause = ((IslandEnterEvent) e).getCause().name();
                break;
            case "islandinviteevent":
                playerName = ((IslandInviteEvent) e).getPlayer().getName();
                targetName = ((IslandInviteEvent) e).getTarget().getName();
                break;
            case "islandjoinevent":
                playerName = ((IslandJoinEvent) e).getPlayer().getName();
                break;
            case "islandkickevent":
                playerName = ((IslandKickEvent) e).getPlayer().getName();
                targetName = ((IslandKickEvent) e).getTarget().getName();
                break;
            case "islandleaveevent":
            case "islandleaveprotectedevent":
                playerName = ((IslandLeaveEvent) e).getPlayer().getName();
                leaveCause = ((IslandLeaveEvent) e).getCause().name();
                break;
            case "islandquitevent":
                playerName = ((IslandQuitEvent) e).getPlayer().getName();
                break;
            case "islandtransferevent":
                oldOwner = playerName = ((IslandTransferEvent) e).getOldOwner().getName();
                newOwner = targetName = ((IslandTransferEvent) e).getNewOwner().getName();
                break;
            case "islandworthcalculatedevent":
                playerName = ((IslandWorthCalculatedEvent) e).getPlayer().getName();
                worth = ((IslandWorthCalculatedEvent) e).getWorth().toString();
                level = ((IslandWorthCalculatedEvent) e).getLevel().toString();
                break;
        }

        for(String command : commands){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                    .replace("%island%", islandName)
                    .replace("%player%", playerName)
                    .replace("%schematic%", schematicName)
                    .replace("%enter-cause%", enterCause)
                    .replace("%target%", targetName)
                    .replace("%leave-cause%", leaveCause)
                    .replace("%old-owner%", oldOwner)
                    .replace("%new-owner%", newOwner)
                    .replace("%worth%", worth)
                    .replace("%level%", level)
            );
        }
    }

}
