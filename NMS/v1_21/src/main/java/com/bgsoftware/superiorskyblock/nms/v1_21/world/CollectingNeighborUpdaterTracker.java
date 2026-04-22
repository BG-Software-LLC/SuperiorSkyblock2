package com.bgsoftware.superiorskyblock.nms.v1_21.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockStates;

public class CollectingNeighborUpdaterTracker extends CollectingNeighborUpdater {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Level level;

    public CollectingNeighborUpdaterTracker(Level level) {
        super(level, MinecraftServer.getServer().getMaxChainedNeighborUpdates());
        this.level = level;
    }

    @Override
    public void shapeUpdate(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, int flags, int recursionLeft) {
        BlockState oldState = this.level.getBlockState(pos);
        super.shapeUpdate(direction, state, pos, neighborPos, flags, recursionLeft);
        BlockState newState = this.level.getBlockState(pos);
        if (oldState.getBlock() != newState.getBlock()) {
            // Block was changed, let's call an update
            GameEventArgs.BlockUpdateShapeEvent blockUpdateShapeEvent = new GameEventArgs.BlockUpdateShapeEvent();
            blockUpdateShapeEvent.block = CraftBlock.at(this.level, pos);
            blockUpdateShapeEvent.oldState = CraftBlockStates.getBlockState(this.level, pos, oldState, null);
            GameEvent<GameEventArgs.BlockUpdateShapeEvent> gameEvent = GameEventType.BLOCK_UPDATE_SHAPE_EVENT.createEvent(blockUpdateShapeEvent);
            plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.MONITOR);
        }
    }

}
