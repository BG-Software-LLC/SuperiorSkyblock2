package com.bgsoftware.superiorskyblock.nms.v26_2_folia.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.ticks.LevelTicks;

import java.util.function.BiConsumer;

public class BlockLevelTicksTracker extends LevelTicks<Block> {

    private final ServerLevel serverLevel;

    public BlockLevelTicksTracker(ServerLevel serverLevel) {
        super(serverLevel::isPositionTickingWithEntitiesLoaded, serverLevel, true);
        this.serverLevel = serverLevel;
    }

    @Override
    public void tick(long gameTime, int maxAllowedTicks, BiConsumer<BlockPos, Block> ticker) {
        super.tick(gameTime, maxAllowedTicks,
                com.bgsoftware.superiorskyblock.nms.v26_2.world.BlockLevelTicksTracker.captureTicks(this.serverLevel, ticker));
    }
}
