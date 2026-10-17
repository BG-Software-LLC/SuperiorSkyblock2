package com.bgsoftware.superiorskyblock.nms.v26_3.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.bukkit.World;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;

import java.lang.reflect.Method;

public class CollectingNeighborUpdaterTracker extends CollectingNeighborUpdater {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Method GET_BLOCK_STATE = initializeGetBlockStateMethod();

    private final Level level;

    public CollectingNeighborUpdaterTracker(Level level) {
        super(level, MinecraftServer.getServer().getMaxChainedNeighborUpdates());
        this.level = level;
    }

    @Override
    public void shapeUpdate(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, @Block.UpdateFlags int flags, int recursionLeft) {
        BlockState oldState = this.level.getBlockState(pos);
        // The block entity must be captured before the update, as it might be removed by it.
        BlockEntity oldBlockEntity = oldState.hasBlockEntity() ? this.level.getBlockEntity(pos) : null;
        super.shapeUpdate(direction, state, pos, neighborPos, flags, recursionLeft);
        BlockState newState = this.level.getBlockState(pos);
        if (oldState.getBlock() != newState.getBlock()) {
            // We cannot create a snapshot of the old state without its block entity.
            if (oldState.hasBlockEntity() && oldBlockEntity == null)
                return;

            // Block was changed, let's call an update
            GameEventArgs.BlockUpdateShapeEvent blockUpdateShapeEvent = new GameEventArgs.BlockUpdateShapeEvent();
            blockUpdateShapeEvent.block = CraftBlock.at(this.level, pos);

            if (GET_BLOCK_STATE == null)
                return;

            try {
                blockUpdateShapeEvent.oldState = (CraftBlockState) GET_BLOCK_STATE.invoke(null,
                        blockUpdateShapeEvent.block.getWorld(), pos, oldState, oldBlockEntity);
            } catch (Exception error) {
                Log.error(error, "An unexpected error occurred while invoking CraftBlockStates#getBlockState() method:");
                return;
            }

            GameEvent<GameEventArgs.BlockUpdateShapeEvent> gameEvent = GameEventType.BLOCK_UPDATE_SHAPE_EVENT.createEvent(blockUpdateShapeEvent);
            plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.MONITOR);
        }
    }

    @Nullable
    public static Method initializeGetBlockStateMethod() {
        try {
            Method getBlockState = CraftBlockStates.class.getDeclaredMethod("getBlockState",
                    World.class, BlockPos.class, BlockState.class, BlockEntity.class);
            getBlockState.setAccessible(true);
            return getBlockState;
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while accessing CraftBlockStates#getBlockState() method:");
            return null;
        }
    }

}
