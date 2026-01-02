package com.bgsoftware.superiorskyblock.nms.v1_18.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.LevelTicks;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlockStates;

import java.util.function.BiConsumer;

public class BlockLevelTicksTracker extends LevelTicks<Block> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final ServerLevel serverLevel;

    public BlockLevelTicksTracker(ServerLevel serverLevel) {
        super(chunkPos -> isPositionTickingWithEntitiesLoaded(serverLevel, chunkPos),
                serverLevel.getProfilerSupplier());
        this.serverLevel = serverLevel;
    }

    @Override
    public void tick(long gameTime, int maxAllowedTicks, BiConsumer<BlockPos, Block> ticker) {
        super.tick(gameTime, maxAllowedTicks, (blockPos, block) -> {
            BlockState oldState = this.serverLevel.getBlockState(blockPos);
            ticker.accept(blockPos, block);
            BlockState newState = this.serverLevel.getBlockState(blockPos);
            if (oldState.getBlock() != newState.getBlock()) {
                // Block was changed, let's call an update
                GameEventArgs.BlockUpdateShapeEvent blockUpdateShapeEvent = new GameEventArgs.BlockUpdateShapeEvent();
                blockUpdateShapeEvent.block = CraftBlock.at(this.serverLevel, blockPos);
                blockUpdateShapeEvent.oldState = CraftBlockStates.getBlockState(blockPos, oldState, null);
                GameEvent<GameEventArgs.BlockUpdateShapeEvent> gameEvent = GameEventType.BLOCK_UPDATE_SHAPE_EVENT.createEvent(blockUpdateShapeEvent);
                plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.MONITOR);
            }
        });
    }

    private static boolean isPositionTickingWithEntitiesLoaded(ServerLevel serverLevel, long chunkPos) {
        ChunkHolder chunkHolder = serverLevel.chunkSource.chunkMap.getVisibleChunkIfPresent(chunkPos);
        return chunkHolder != null && chunkHolder.isTickingReady() && serverLevel.areEntitiesLoaded(chunkPos);
    }

}
