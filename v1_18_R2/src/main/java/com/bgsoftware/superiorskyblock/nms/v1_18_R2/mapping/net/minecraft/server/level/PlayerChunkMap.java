package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.server.level;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.ChunkCoordIntPair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.storage.WorldPersistentData;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class PlayerChunkMap extends MappedObject<net.minecraft.server.level.PlayerChunkMap> {

    private static final ReflectField<Map<Long, PlayerChunk>> VISIBLE_CHUNKS = new ReflectField<>(
            net.minecraft.server.level.PlayerChunkMap.class, Map.class, Modifier.PUBLIC | Modifier.VOLATILE, 1);

    public PlayerChunkMap(net.minecraft.server.level.PlayerChunkMap handle) {
        super(handle);
    }

    public NBTTagCompound read(ChunkCoordIntPair chunkCoordIntPair) throws IOException {
        return NBTTagCompound.ofNullable(handle.f(chunkCoordIntPair.getHandle()));
    }

    public NBTTagCompound getChunkData(ResourceKey<WorldDimension> resourcekey, Supplier<WorldPersistentData> supplier,
                                       NBTTagCompound nbttagcompound, ChunkCoordIntPair pos,
                                       GeneratorAccess generatoraccess) throws IOException {
        return new NBTTagCompound(handle.upgradeChunkTag(resourcekey, supplier, nbttagcompound.getHandle(),
                Optional.empty(), pos.getHandle(), generatoraccess));
    }

    public void saveChunk(ChunkCoordIntPair chunkCoords, NBTTagCompound nbtTagCompound) throws IOException {
        handle.a(chunkCoords.getHandle(), nbtTagCompound.getHandle());
    }

    public PlayerChunk getPlayerChunk(long chunkCoordsPair) {
        try {
            return handle.b(chunkCoordsPair);
        } catch (Throwable ex) {
            return VISIBLE_CHUNKS.get(handle).get(chunkCoordsPair);
        }
    }

}
