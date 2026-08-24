package com.bgsoftware.superiorskyblock.nms.v1_20_3.world;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockStates;

public class CollectingNeighborUpdaterTracker extends CollectingNeighborUpdater {

    private static final ReflectMethod<CraftBlockState> BLOCK_STATE_CREATE = new ReflectMethod<>(
            CraftBlockStates.class, "getBlockState", World.class, BlockPos.class, BlockState.class, BlockEntity.class);
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Level level;

    public CollectingNeighborUpdaterTracker(Level level) {
        super(level, MinecraftServer.getServer().getMaxChainedNeighborUpdates());
        this.level = level;
    }

    @Override
    public void shapeUpdate(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, int flags, int recursionLeft) {
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
            blockUpdateShapeEvent.oldState = BLOCK_STATE_CREATE.invoke(null, blockUpdateShapeEvent.block.getWorld(), pos, oldState, oldBlockEntity);
            GameEvent<GameEventArgs.BlockUpdateShapeEvent> gameEvent = GameEventType.BLOCK_UPDATE_SHAPE_EVENT.createEvent(blockUpdateShapeEvent);
            plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.MONITOR);
        }
    }

}
