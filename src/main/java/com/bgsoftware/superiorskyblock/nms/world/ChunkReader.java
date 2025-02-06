package com.bgsoftware.superiorskyblock.nms.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public interface ChunkReader {

    int getX();

    int getZ();

    Material getType(int x, int y, int z);

    short getData(int x, int y, int z);

    @Nullable
    CompoundTag getTileEntity(int x, int y, int z);

    @Nullable
    CompoundTag readBlockStates(int x, int y, int z);

    byte[] getLightLevels(int x, int y, int z);

    void forEachEntity(EntityConsumer consumer);

    interface EntityConsumer {

        void apply(EntityType entityType, CompoundTag entityTag, Location location);

    }

}
