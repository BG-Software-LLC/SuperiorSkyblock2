package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;
import java.util.function.Function;

public class WorldDestructionListener extends AbstractGameEventListener {

    public WorldDestructionListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        registerListeners();
    }

    //Checking for structures growing outside island.
    private void onStructureGrow(GameEvent<GameEventArgs.StructureGrowEvent> e) {
        Location location = e.getArgs().location;

        // We care about destruction of island worlds only
        if (!plugin.getGrid().isIslandsWorld(location.getWorld())) {
            return;
        }

        Island island = plugin.getGrid().getIslandAt(location);
        if (island != null) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                e.getArgs().blocks.removeIf(blockState ->
                        !island.isInsideRange(blockState.getLocation(wrapper.getHandle())));
            }
        }
    }

    //Checking for chorus flower spread outside island.
    private void onBlockSpread(GameEvent<GameEventArgs.BlockSpreadEvent> e) {
        Block block = e.getArgs().block;

        // We care about destruction of island worlds only
        if (!plugin.getGrid().isIslandsWorld(block.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventDestruction(e.getArgs().source.getLocation(wrapper.getHandle())) ||
                    preventDestruction(block.getLocation(wrapper.getHandle())))
                e.setCancelled();
        }
    }

    public void onPistonExtend(GameEvent<GameEventArgs.PistonExtendEvent> e) {
        Block block = e.getArgs().block;

        // We care about destruction of island worlds only
        if (!plugin.getGrid().isIslandsWorld(block.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventMultiDestruction(block.getLocation(wrapper.getHandle()), e.getArgs().blocks, null))
                e.setCancelled();
        }
    }

    public void onPistonRetract(GameEvent<GameEventArgs.PistonRetractEvent> e) {
        Block pistonBlock = e.getArgs().block;

        // We care about destruction of island worlds only
        if (!plugin.getGrid().isIslandsWorld(pistonBlock.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventMultiDestruction(pistonBlock.getLocation(wrapper.getHandle()), e.getArgs().blocks,
                    block -> block.getRelative(e.getArgs().direction)))
                e.setCancelled();
        }
    }

    private void onBlockFlow(GameEvent<GameEventArgs.BlockFromToEvent> e) {
        Block block = e.getArgs().toBlock;

        // We care about destruction of island worlds only
        if (!plugin.getGrid().isIslandsWorld(block.getWorld())) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());
            if (preventDestruction(blockLocation))
                e.setCancelled();
        }
    }

    /* INTERNAL */

    private boolean preventDestruction(Location location) {
        Island island = plugin.getGrid().getIslandAt(location);
        return island == null || !island.isInsideRange(location);
    }

    private boolean preventMultiDestruction(Location islandLocation, List<Block> blockList,
                                            @Nullable Function<Block, Block> blockTransform) {
        Island island = plugin.getGrid().getIslandAt(islandLocation);

        if (island == null)
            return true;

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

    private void registerListeners() {
        registerCallback(GameEventType.STRUCTURE_GROW_EVENT, GameEventPriority.NORMAL, this::onStructureGrow);
        registerCallback(GameEventType.BLOCK_SPREAD_EVENT, GameEventPriority.NORMAL, this::onBlockSpread);
        registerCallback(GameEventType.PISTON_EXTEND_EVENT, GameEventPriority.NORMAL, this::onPistonExtend);
        registerCallback(GameEventType.PISTON_RETRACT_EVENT, GameEventPriority.NORMAL, this::onPistonRetract);
        registerCallback(GameEventType.BLOCK_FROM_TO_EVENT, GameEventPriority.NORMAL, this::onBlockFlow);
    }

}
