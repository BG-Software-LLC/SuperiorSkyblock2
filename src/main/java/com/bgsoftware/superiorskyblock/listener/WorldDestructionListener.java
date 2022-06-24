package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.LinkedList;
import java.util.List;

public class WorldDestructionListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public WorldDestructionListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    //Checking for structures growing outside island.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onStructureGrow(StructureGrowEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getLocation());
        if (island != null && plugin.getGrid().isIslandsWorld(e.getLocation().getWorld()))
            e.getBlocks().removeIf(blockState -> !island.isInsideRange(blockState.getLocation()));
    }

    //Checking for chorus flower spread outside island.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockSpread(BlockSpreadEvent e) {
        if (preventDestruction(e.getSource().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        List<Location> blockLocations = new LinkedList<>();
        e.getBlocks().forEach(block -> blockLocations.add(block.getRelative(e.getDirection()).getLocation()));
        if (preventMultiDestruction(e.getBlock().getLocation(), blockLocations))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        List<Location> blockLocations = new LinkedList<>();
        e.getBlocks().forEach(block -> blockLocations.add(block.getRelative(e.getDirection()).getLocation()));
        if (preventMultiDestruction(e.getBlock().getLocation(), blockLocations))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFlow(BlockFromToEvent e) {
        Location blockLocation = e.getBlock().getRelative(e.getFace()).getLocation();
        if (preventDestruction(blockLocation))
            e.setCancelled(true);
    }

    /* INTERNAL */

    private boolean preventDestruction(Location location) {
        Island island = plugin.getGrid().getIslandAt(location);
        return island == null ? plugin.getGrid().isIslandsWorld(location.getWorld()) : !island.isInsideRange(location);
    }

    private boolean preventMultiDestruction(Location islandLocation, List<Location> blockLocations) {
        Island island = plugin.getGrid().getIslandAt(islandLocation);

        if (island == null)
            return plugin.getGrid().isIslandsWorld(islandLocation.getWorld());

        for (Location blockLocation : blockLocations) {
            if (!island.isInsideRange(blockLocation))
                return true;
        }

        return false;
    }

}
