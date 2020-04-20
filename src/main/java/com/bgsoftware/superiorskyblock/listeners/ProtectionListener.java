package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameBreakEvent;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameRotationEvent;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public final class ProtectionListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public ProtectionListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        new PlayerArrowPickup();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(!handleBlockPlace(island, superiorPlayer, e.getBlock(), true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld())) {
                Locale.DESTROY_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPrivilege islandPermission = e.getBlock().getType() == Materials.SPAWNER.toBukkitType() ?
                IslandPrivileges.SPAWNER_BREAK : IslandPrivileges.BREAK;

        if(!island.hasPermission(superiorPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getBlock().getLocation())){
            e.setCancelled(true);
            Locale.DESTROY_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e){
        if(e.getClickedBlock() == null)
            return;

        if(!plugin.getSettings().interactables.contains(e.getClickedBlock().getType().name()) &&
                plugin.getGrid().getBlockAmount(e.getClickedBlock()) <= 1)
            return;

        Block clickedBlock = e.getClickedBlock();

        Island island = plugin.getGrid().getIslandAt(clickedBlock.getLocation());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPrivilege islandPermission;

        if(clickedBlock.getState() instanceof Chest || clickedBlock.getType().name().contains("SHULKER_BOX")) islandPermission = IslandPrivileges.CHEST_ACCESS;
        else if(clickedBlock.getState() instanceof InventoryHolder) islandPermission = IslandPrivileges.USE;
        else if(clickedBlock.getState() instanceof Sign) islandPermission = IslandPrivileges.SIGN_INTERACT;
        else if(clickedBlock.getType() == Materials.SPAWNER.toBukkitType()) islandPermission = IslandPrivileges.SPAWNER_BREAK;
        else if(clickedBlock.getType().name().equals("SOIL") || clickedBlock.getType().name().equals("FARMLAND"))
            islandPermission = e.getAction() == Action.PHYSICAL ? IslandPrivileges.FARM_TRAMPING : IslandPrivileges.BUILD;
        else islandPermission = IslandPrivileges.INTERACT;

        if(!island.hasPermission(superiorPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            return;
        }

        if(!island.isInsideRange(clickedBlock.getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFrostWalker(EntityBlockFormEvent e){
        if(!(e.getEntity() instanceof Player))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getEntity());

        if(!handleBlockPlace(island, superiorPlayer, e.getBlock(), false))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent e){
        if(!(e.getTarget() instanceof Player))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getTarget());

        if(island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.MONSTER_DAMAGE))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e){
        if(!(e.getRemover() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of((Player) e.getRemover());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPrivilege islandPermission = e.getEntity() instanceof ItemFrame ? IslandPrivileges.ITEM_FRAME : IslandPrivileges.PAINTING;
        if(!island.hasPermission(superiorPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            return;
        }

        if(!island.isInsideRange(e.getEntity().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPrivilege islandPermission = e.getEntity() instanceof ItemFrame ? IslandPrivileges.ITEM_FRAME : IslandPrivileges.PAINTING;
        if(!island.hasPermission(superiorPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            return;
        }

        if(!island.isInsideRange(e.getEntity().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameRotate(ItemFrameRotationEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getItemFrame().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.ITEM_FRAME)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getItemFrame().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameBreak(ItemFrameBreakEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getItemFrame().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.ITEM_FRAME)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getItemFrame().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null) {
            if(plugin.getGrid().isIslandsWorld(e.getBlock().getWorld()))
                e.setCancelled(true);

            return;
        }

        for (Block block : e.getBlocks()) {
            if (!island.isInsideRange(block.getRelative(e.getDirection()).getLocation())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null) {
            if(plugin.getGrid().isIslandsWorld(e.getBlock().getWorld()))
                e.setCancelled(true);

            return;
        }

        for(Block block : e.getBlocks()){
            if(!island.isInsideRange(block.getRelative(e.getDirection()).getLocation())){
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFlow(BlockFromToEvent e){
        if(plugin == null || plugin.getGrid() == null)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        Location toLocation = e.getBlock().getRelative(e.getFace()).getLocation();

        if((fromIsland == null && plugin.getGrid().isIslandsWorld(e.getBlock().getWorld())) ||
                (fromIsland != null && !fromIsland.isInsideRange(toLocation))){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getBlockClicked().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.BUILD)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getBlockClicked().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getBlockClicked().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.BREAK)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getBlockClicked().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractAtEntityEvent e){
        if(e.getRightClicked() instanceof Painting || e.getRightClicked() instanceof ItemFrame)
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());

        if(island != null && !island.hasPermission(superiorPlayer, e.getRightClicked() instanceof ArmorStand ?
                IslandPrivileges.INTERACT : IslandPrivileges.ANIMAL_BREED)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

        if(island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.DROP_ITEMS)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerItemPickup(PlayerPickupItemEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

        if(island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.PICKUP_DROPS)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityAttack(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Painting || e.getEntity() instanceof ItemFrame || e.getEntity() instanceof Player)
            return;

        Player damager = null;

        if(e.getDamager() instanceof Player){
            damager = (Player) e.getDamager();
        }
        else if(e.getDamager() instanceof Projectile){
            Projectile projectile = (Projectile) e.getDamager();
            if(projectile.getShooter() instanceof Player)
                damager = (Player) projectile.getShooter();
        }

        if(damager == null)
            return;

        SuperiorPlayer damagerPlayer = SSuperiorPlayer.of(damager);
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        IslandPrivilege islandPermission = e.getEntity() instanceof ArmorStand ? IslandPrivileges.BREAK : e.getEntity() instanceof Animals ? IslandPrivileges.ANIMAL_DAMAGE : IslandPrivileges.MONSTER_DAMAGE;

        if(island != null && !island.hasPermission(damagerPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(damagerPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntitySpawn(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.hasItem())
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        EntityType spawnType = ItemUtils.getEntityType(e.getItem());

        if(spawnType == EntityType.UNKNOWN)
            return;

        IslandPrivilege islandPermission = e.getItem().getType() == Material.ARMOR_STAND ? IslandPrivileges.BUILD : Animals.class.isAssignableFrom(spawnType.getEntityClass()) ? IslandPrivileges.ANIMAL_SPAWN : IslandPrivileges.MONSTER_SPAWN;

        if(island != null && !island.hasPermission(superiorPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleSpawn(PlayerInteractEvent e){
        if(e.getClickedBlock() == null || e.getItem() == null)
            return;

        if(!e.getItem().getType().name().contains("MINECART") && !e.getItem().getType().name().contains("BOAT"))
            return;

        if(e.getItem().getType().name().contains("MINECART") && !e.getClickedBlock().getType().name().contains("RAIL"))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if(island == null)
            return;

        if(!island.hasPermission(e.getPlayer(), IslandPrivileges.MINECART_PLACE)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDamageEvent e){
        if(!(e.getAttacker() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getAttacker());
        Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if(island == null)
            return;

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.MINECART_DAMAGE)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent e){
        if(!(e.getEntered() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getEntered());
        Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if(island == null)
            return;

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.MINECART_ENTER)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleOpen(InventoryOpenEvent e){
        InventoryHolder inventoryHolder = e.getInventory().getHolder();

        if(!(inventoryHolder instanceof Minecart))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(((Minecart) inventoryHolder).getLocation());

        if(island == null)
            return;

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.MINECART_OPEN)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPearlTeleport(PlayerTeleportEvent e){
        if(e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getTo());

        if(island == null){
            e.setCancelled(true);
            Locale.TELEPORT_OUTSIDE_ISLAND.send(superiorPlayer);
        }

        else if(!island.hasPermission(superiorPlayer, IslandPrivileges.ENDER_PEARL)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }

        else if(!island.isInsideRange(e.getTo())){
            e.setCancelled(true);
            Locale.TELEPORT_OUTSIDE_ISLAND.send(superiorPlayer);
        }

        if(e.isCancelled() && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        }
    }

    private boolean handleBlockPlace(Island island, SuperiorPlayer superiorPlayer, Block block, boolean sendMessages){
        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())){
                if(sendMessages)
                    Locale.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
                return false;
            }

            return true;
        }

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.BUILD)){
            if(sendMessages)
                Locale.sendProtectionMessage(superiorPlayer);
            return false;
        }

        if(!island.isInsideRange(block.getLocation())){
            if(sendMessages)
                Locale.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
            return false;
        }

        return true;
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

            if(island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.PICKUP_DROPS)){
                e.setCancelled(true);
                Locale.sendProtectionMessage(superiorPlayer);
            }
        }

    }

}
