package com.bgsoftware.superiorskyblock.nms.v1_21_4.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventFlags;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.LevelTicks;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockStates;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class BlockLevelTicksTracker extends LevelTicks<Block> {

    private static final BiFunction<ServerLevel, Long, Boolean> IS_POSITION_TICKING_WITH_ENTITIES_LOADED_FUNCTION =
            initializePositionTickingWithEntitiesLoaded();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final ServerLevel serverLevel;

    public BlockLevelTicksTracker(ServerLevel serverLevel) {
        super(chunkPos -> IS_POSITION_TICKING_WITH_ENTITIES_LOADED_FUNCTION.apply(serverLevel, chunkPos));
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
                blockUpdateShapeEvent.oldState = CraftBlockStates.getBlockState(this.serverLevel, blockPos, oldState, null);
                GameEvent<GameEventArgs.BlockUpdateShapeEvent> gameEvent = GameEventType.BLOCK_UPDATE_SHAPE_EVENT.createEvent(blockUpdateShapeEvent);
                plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.MONITOR);
            }
        });
    }

    private static boolean isPositionTickingWithEntitiesLoadedSpigot(ServerLevel serverLevel, long chunkPos) {
        return serverLevel.areEntitiesLoaded(chunkPos) && serverLevel.chunkSource.isPositionTicking(chunkPos);
    }

    private static boolean isPositionTickingWithEntitiesLoadedPaper(ServerLevel serverLevel, long chunkPos) {
        ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder chunkHolder =
                serverLevel.moonrise$getChunkTaskScheduler().chunkHolderManager.getChunkHolder(chunkPos);
        return chunkHolder != null && chunkHolder.isTickingReady();
    }

    private static BiFunction<ServerLevel, Long, Boolean> initializePositionTickingWithEntitiesLoaded() {
        try {
            Class.forName("ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder");
            return BlockLevelTicksTracker::isPositionTickingWithEntitiesLoadedPaper;
        } catch (Throwable error) {
            return BlockLevelTicksTracker::isPositionTickingWithEntitiesLoadedSpigot;
        }
    }

}
