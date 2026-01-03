package com.bgsoftware.superiorskyblock.nms.v1_18.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventFlags;
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

import java.util.List;
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
            try {
                // Only capture blocks related events
                plugin.getGameEventsDispatcher().startCaptureEvents(GameEventFlags.BLOCK_EVENT | GameEventFlags.MAYBE_BLOCK_EVENT);
                ticker.accept(blockPos, block);
            } finally {
                List<GameEvent<?>> capturedEvents = plugin.getGameEventsDispatcher().stopCaptureEvents();
                // Remove BlockPhysicsEvent which we don't listen to
                capturedEvents.removeIf(gameEvent -> gameEvent.getType() == GameEventType.BLOCK_PHYSICS_EVENT);
                // We don't want to fire the BlockUpdateShapeEvent if another event was fired in the tick method.
                // This is to prevent blocks from being considered updated twice.
                if (!capturedEvents.isEmpty())
                    return;
            }
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
