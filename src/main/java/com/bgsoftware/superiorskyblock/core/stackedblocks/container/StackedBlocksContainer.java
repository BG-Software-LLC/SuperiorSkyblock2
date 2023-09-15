package com.bgsoftware.superiorskyblock.core.stackedblocks.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.stackedblocks.StackedBlock;
import org.bukkit.Location;

import java.util.function.Consumer;

public interface StackedBlocksContainer {

    @Nullable
    StackedBlock getStackedBlock(Location location);

    StackedBlock createStackedBlock(Location location);

    StackedBlock removeStackedBlock(Location location);

    void forEach(ChunkPosition chunkPosition, Consumer<StackedBlock> consumer);

    void forEach(Consumer<StackedBlock> consumer);

    void removeStackedBlocks(ChunkPosition chunkPosition, Consumer<StackedBlock> consumer);

}
