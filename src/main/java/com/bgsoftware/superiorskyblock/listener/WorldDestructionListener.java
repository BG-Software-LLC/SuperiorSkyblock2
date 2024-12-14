package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.List;
import java.util.function.Function;

public class WorldDestructionListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public WorldDestructionListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    //Checking for structures growing outside island.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onStructureGrow(StructureGrowEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getLocation());
        if (island != null && plugin.getGrid().isIslandsWorld(e.getLocation().getWorld())) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                e.getBlocks().removeIf(blockState ->
                        !island.isInsideRange(blockState.getLocation(wrapper.getHandle())));
            }
        }
    }

    //Checking for chorus flower spread outside island.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockSpread(BlockSpreadEvent e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventDestruction(e.getSource().getLocation(wrapper.getHandle())) ||
                    preventDestruction(e.getBlock().getLocation(wrapper.getHandle())))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventMultiDestruction(e.getBlock().getLocation(wrapper.getHandle()), e.getBlocks(), null))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventMultiDestruction(e.getBlock().getLocation(wrapper.getHandle()), e.getBlocks(),
                    block -> block.getRelative(e.getDirection())))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFlow(BlockFromToEvent e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = e.getBlock().getRelative(e.getFace()).getLocation(wrapper.getHandle());
            if (preventDestruction(blockLocation))
                e.setCancelled(true);
        }
    }

    /* INTERNAL */

    private boolean preventDestruction(Location location) {
        Island island = plugin.getGrid().getIslandAt(location);
        return island == null ? plugin.getGrid().isIslandsWorld(location.getWorld()) : !island.isInsideRange(location);
    }

    private boolean preventMultiDestruction(Location islandLocation, List<Block> blockList,
                                            @Nullable Function<Block, Block> blockTransform) {
        Island island = plugin.getGrid().getIslandAt(islandLocation);

        if (island == null)
            return plugin.getGrid().isIslandsWorld(islandLocation.getWorld());

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            for (Block block : blockList) {
                if (blockTransform != null)
                    block = blockTransform.apply(block);
                if (!island.isInsideRange(block.getLocation(wrapper.getHandle())))
                    return true;
            }
        }

        return false;
    }

}
