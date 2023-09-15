package com.bgsoftware.superiorskyblock.nms.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.List;

public interface WorldEditSession {

    void setBlock(Location location, int combinedId, @Nullable CompoundTag statesTag, @Nullable CompoundTag blockEntityData);

    List<ChunkPosition> getAffectedChunks();

    void applyBlocks(Chunk chunk);

    void finish(Island island);

}
