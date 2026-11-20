package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.MutableBlockPosition;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;

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
            try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
                e.getArgs().blocks.removeIf(blockState ->
                        !island.isInsideRange(blockPosition.set(blockState.getX(), blockState.getY(), blockState.getZ())));
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

        Island blockIsland = e.getArgs().islandResolver.get();
        Island spreadIsland = WorldBlocks.findIslandWithHint(e.getArgs().source, blockIsland);

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (preventDestruction(e.getArgs().source.getLocation(wrapper.getHandle()), spreadIsland) ||
                    preventDestruction(block.getLocation(wrapper.getHandle()), blockIsland))
                e.setCancelled();
        }
    }

    public void onPistonExtend(GameEvent<GameEventArgs.PistonExtendEvent> e) {
        Block pistonBlock = e.getArgs().block;

        // We care about destruction of island worlds only
        if (!plugin.getGrid().isIslandsWorld(pistonBlock.getWorld())) {
            return;
        }

        Island island = e.getArgs().islandResolver.get();
        if (preventMultiDestruction(e.getArgs().blocks, island, e.getArgs().direction))
            e.setCancelled();
    }

    public void onPistonRetract(GameEvent<GameEventArgs.PistonRetractEvent> e) {
        Block pistonBlock = e.getArgs().block;

        // We care about destruction of island worlds only
        if (!plugin.getGrid().isIslandsWorld(pistonBlock.getWorld())) {
            return;
        }

        Island island = e.getArgs().islandResolver.get();
        if (preventMultiDestruction(e.getArgs().blocks, island, e.getArgs().direction))
            e.setCancelled();
    }

    private void onBlockFlow(GameEvent<GameEventArgs.BlockFromToEvent> e) {
        Block block = e.getArgs().toBlock;

        // We care about destruction of island worlds only
        if (!plugin.getGrid().isIslandsWorld(block.getWorld())) {
            return;
        }

        Island island = WorldBlocks.findIslandWithHint(block, e.getArgs().islandResolver.get());

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());
            if (preventDestruction(blockLocation, island))
                e.setCancelled();
        }
    }

    /* INTERNAL */

    private boolean preventDestruction(Location location, @Nullable Island island) {
        if (island == null)
            return true;

        try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
            return !island.isInsideRange(blockPosition.set(location));
        }
    }

    private boolean preventMultiDestruction(List<Block> blockList, @Nullable Island island, @Nullable BlockFace moveDirection) {
        if (island == null)
            return true;

        try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
            for (Block block : blockList) {
                int x = moveDirection == null ? block.getX() : block.getX() + moveDirection.getModX();
                int y = moveDirection == null ? block.getY() : block.getY() + moveDirection.getModY();
                int z = moveDirection == null ? block.getZ() : block.getZ() + moveDirection.getModZ();
                if (!island.isInsideRange(blockPosition.set(x, y, z)))
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
