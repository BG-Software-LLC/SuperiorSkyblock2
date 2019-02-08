package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameBreakEvent;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameRotationEvent;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public final class ProtectionListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    private List<String> interactableBlocks = Arrays.asList(
            "ACACIA_BUTTON", "ACACIA_DOOR", "ACACIA_FENCE", "ACACIA_FENCE_GATE", "ACACIA_TRAPDOOR", "ANVIL", "BEACON",
            "BIRCH_BUTTON", "BIRCH_DOOR", "BIRCH_FENCE", "BIRCH_FENCE_GATE", "BIRCH_TRAPDOOR", "BLACK_BED",
            "BLACK_SHULKER_BOX", "BLUE_BED", "BLUE_SHULKER_BOX", "BREWING_STAND", "BROWN_BED", "BROWN_SHULKER_BOX",
            "CAKE", "CAULDRON", "CHAIN_COMMAND_BLOCK", "CHEST", "CHIPPED_ANVIL", "COMMAND_BLOCK", "COMPARATOR",
            "CRAFTING_TABLE", "CYAN_BED","CYAN_SHULKER_BOX", "DAMAGED_ANVIL", "DARK_OAK_BUTTON", "DARK_OAK_DOOR",
            "DARK_OAK_FENCE", "DARK_OAK_FENCE_GATE", "DARK_OAK_TRAPDOOR", "DAYLIGHT_DETECTOR", "DISPENSER", "DRAGON_EGG",
            "DROPPER", "ENCHANTING_TABLE", "ENDER_CHEST", "FLOWER_POT", "FURNACE", "GRAY_BED", "GRAY_SHULKER_BOX",
            "GREEN_BED", "GREEN_SHULKER_BOX", "HOPPER", "IRON_DOOR", "IRON_TRAPDOOR", "JUKEBOX", "JUNGLE_BUTTON",
            "JUNGLE_DOOR", "JUNGLE_FENCE", "JUNGLE_FENCE_GATE", "JUNGLE_TRAPDOOR", "LEVER", "LIGHT_BLUE_BED",
            "LIGHT_BLUE_SHULKER_BOX", "LIGHT_GRAY_BED", "LIGHT_GRAY_SHULKER_BOX", "LIME_BED", "LIME_SHULKER_BOX",
            "LIGHT_BLUE_SHULKER_BOX", "LIGHT_GRAY_BED", "LIGHT_GRAY_SHULKER_BOX", "LIME_BED", "LIME_SHULKER_BOX",
            "MAGENTA_BED", "MAGENTA_SHULKER_BOX", "NETHER_BRICK_FENCE", "NOTE_BLOCK", "OAK_BUTTON", "OAK_DOOR",
            "OAK_FENCE", "OAK_FENCE_GATE", "OAK_TRAPDOOR", "ORANGE_BED", "ORANGE_SHULKER_BOX", "PINK_BED",
            "PINK_SHULKER_BOX", "POTTED_ACACIA_SAPLING", "POTTED_ALLIUM", "POTTED_AZURE_BLUET", "POTTED_BIRCH_SAPLING",
            "POTTED_BLUE_ORCHID", "POTTED_BROWN_MUSHROOM", "POTTED_CACTUS", "POTTED_DANDELION", "POTTED_DARK_OAK_SAPLING",
            "POTTED_DEAD_BUSH", "POTTED_FERN", "POTTED_JUNGLE_SAPLING", "POTTED_OAK_SAPLING", "POTTED_ORANGE_TULIP",
            "POTTED_OXEYE_DAISY", "POTTED_PINK_TULIP", "POTTED_POPPY", "POTTED_RED_MUSHROOM", "POTTED_RED_TULIP",
            "POTTED_SPRUCE_SAPLING", "POTTED_WHITE_TULIP", "PUMPKIN", "PURPLE_BED", "PURPLE_SHULKER_BOX", "REDSTONE_ORE",
            "RED_BED", "RED_SHULKER_BOX", "REPEATER", "REPEATING_COMMAND_BLOCK", "SHULKER_BOX", "SIGN", "SPRUCE_BUTTON",
            "SPRUCE_DOOR", "SPRUCE_FENCE", "SPRUCE_FENCE_GATE", "SPRUCE_TRAPDOOR", "STONE_BUTTON", "STRUCTURE_BLOCK",
            "TNT", "TRAPPED_CHEST", "WALL_SIGN", "WHITE_BED", "WHITE_SHULKER_BOX", "YELLOW_BED", "YELLOW_SHULKER_BOX",
            "BED_BLOCK", "WORKBENCH", "BURNING_FURNACE", "SIGN_POST", "WOODEN_DOOR", "IRON_DOOR_BLOCK", "IRON_FENCE",
            "GLOWING_REDSTONE_ORE", "FENCE", "CAKE_BLOCK", "DIODE_BLOCK_OFF", "DIODE_BLOCK_ON", "TRAP_DOOR", "FENCE_GATE",
            "NETHER_FENCE", "ENCHANTMENT_TABLE", "ENDER_PORTAL_FRAME", "COMMAND", "WOOD_BUTTON", "GOLD_PLATE", "IRON_PLATE",
            "REDSTONE_COMPARATOR_OFF", "REDSTONE_COMPARATOR_ON", "DAYLIGHT_DETECTOR_INVERTED", "COMMAND_REPEATING",
            "COMMAND_CHAIN", "STRUCTURE_VOID", "ACACIA_PRESSURE_PLATE", "BIRCH_PRESSURE_PLATE", "DARK_OAK_PRESSURE_PLATE",
            "HEAVY_WEIGHTED_PRESSURE_PLATE", "JUNGLE_PRESSURE_PLATE", "LIGHT_WEIGHTED_PRESSURE_PLATE", "OAK_PRESSURE_PLATE",
            "SPRUCE_PRESSURE_PLATE", "STONE_PRESSURE_PLATE"
    );

    public ProtectionListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null)
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(!island.hasPermission(superiorPlayer, IslandPermission.BUILD)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getBlock().getLocation())){
            e.setCancelled(true);
            Locale.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null)
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(!island.hasPermission(superiorPlayer, IslandPermission.BREAK)){
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
        if(e.getClickedBlock() == null || !interactableBlocks.contains(e.getClickedBlock().getType().name()))
            return;

        Block clickedBlock = e.getClickedBlock();

        Island island = plugin.getGrid().getIslandAt(clickedBlock.getLocation());

        if(island == null)
            return;

        IslandPermission islandPermission;

        if(clickedBlock.getState() instanceof Chest) islandPermission = IslandPermission.CHEST_ACCESS;
        else if(clickedBlock.getState() instanceof InventoryHolder) islandPermission = IslandPermission.USE;
        else if(clickedBlock.getState() instanceof Sign) islandPermission = IslandPermission.SIGN_INTERACT;
        else islandPermission = IslandPermission.INTERACT;

        if(!island.hasPermission(SSuperiorPlayer.of(e.getPlayer()), islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e){
        if(!(e.getRemover() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of((Player) e.getRemover());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null)
            return;

        IslandPermission islandPermission = e.getEntity() instanceof ItemFrame ? IslandPermission.ITEM_FRAME : IslandPermission.PAINTING;
        if(!island.hasPermission(superiorPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameRotate(ItemFrameRotationEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getItemFrame().getLocation());

        if(island == null)
            return;

        if(!island.hasPermission(superiorPlayer, IslandPermission.ITEM_FRAME)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameBreak(ItemFrameBreakEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getItemFrame().getLocation());

        if(island == null)
            return;

        if(!island.hasPermission(superiorPlayer, IslandPermission.ITEM_FRAME)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null) {
            for (Block block : e.getBlocks()) {
                if (!island.isInsideRange(block.getRelative(e.getDirection()).getLocation())) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null){
            for(Block block : e.getBlocks()){
                if(!island.isInsideRange(block.getRelative(e.getDirection()).getLocation())){
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFlow(BlockFromToEvent e){
        if(plugin == null || plugin.getGrid() == null)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        Location toLocation = e.getBlock().getRelative(e.getFace()).getLocation();

        if(fromIsland != null && !fromIsland.isInsideRange(toLocation)){
            e.setCancelled(true);
        }
    }

}
