package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.chunk;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.ChunkCoordIntPair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.core.IRegistry;
import net.minecraft.core.Registry;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.IChunkAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ChunkAccess extends MappedObject<IChunkAccess> {

    public ChunkAccess(IChunkAccess handle) {
        super(handle);
    }

    @Nullable
    public static ChunkAccess ofNullable(@Nullable IChunkAccess handle) {
        return handle == null ? null : new ChunkAccess(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "getPos",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public ChunkCoordIntPair getPos() {
        return new ChunkCoordIntPair(handle.f());
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "getSections",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public net.minecraft.world.level.chunk.ChunkSection[] getSections() {
        return handle.d();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "blockEntities",
            type = Remap.Type.FIELD,
            remappedName = "i")
    public Map<BlockPosition, TileEntity> getTileEntities() {
        Map<BlockPosition, TileEntity> retVal = new HashMap<>(handle.i.size());
        handle.i.forEach((blockPosition, tileEntity) ->
                retVal.put(new BlockPosition(blockPosition), new TileEntity(tileEntity)));
        return retVal;
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "blockEntities",
            type = Remap.Type.FIELD,
            remappedName = "i")
    public Set<net.minecraft.core.BlockPosition> getTilePositions() {
        return handle.i.keySet();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunk",
            name = "level",
            type = Remap.Type.FIELD,
            remappedName = "q")
    public WorldServer getWorld() {
        return new WorldServer(((Chunk) handle).q);
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunk",
            name = "getFullStatus",
            type = Remap.Type.METHOD,
            remappedName = "B")
    public PlayerChunk.State getState() {
        return ((Chunk) handle).B();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "setUnsaved",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setNeedsSaving(boolean needsSaving) {
        handle.a(needsSaving);
    }

    public IRegistry<BiomeBase> getBiomeRegistry() {
        return handle.biomeRegistry;
    }

    @Remap(classPath = "net.minecraft.core.Registry",
            name = "asHolderIdMap",
            type = Remap.Type.METHOD,
            remappedName = "r")
    public Registry<net.minecraft.core.Holder<BiomeBase>> getBiomeRegistryHolder() {
        return getBiomeRegistry().r();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "getBlockEntityNbtForSaving",
            type = Remap.Type.METHOD,
            remappedName = "g")
    public NBTTagCompound getTileEntityNBT(BlockPosition blockPosition) {
        return new NBTTagCompound(handle.g(blockPosition.getHandle()));
    }

    public org.bukkit.Chunk getBukkitChunk() {
        return ((Chunk) handle).getBukkitChunk();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "heightmaps",
            type = Remap.Type.FIELD,
            remappedName = "g")
    public HeightMap getHeightmap(net.minecraft.world.level.levelgen.HeightMap.Type type) {
        return HeightMap.ofNullable(handle.g.get(type));
    }

}
