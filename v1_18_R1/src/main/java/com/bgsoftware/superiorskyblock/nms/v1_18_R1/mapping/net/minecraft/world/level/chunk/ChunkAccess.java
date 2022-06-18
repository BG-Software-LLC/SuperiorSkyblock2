package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.chunk;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.levelgen.HeightMap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.core.IRegistry;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.IChunkAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChunkAccess extends MappedObject<IChunkAccess> {

    public ChunkAccess(IChunkAccess handle) {
        super(handle);
    }

    @Nullable
    public static ChunkAccess ofNullable(@Nullable IChunkAccess handle) {
        return handle == null ? null : new ChunkAccess(handle);
    }

    public ChunkCoordIntPair getPos() {
        return new ChunkCoordIntPair(handle.f());
    }

    public net.minecraft.world.level.chunk.ChunkSection[] getSections() {
        return handle.d();
    }

    public Map<BlockPosition, TileEntity> getTileEntities() {
        Map<BlockPosition, TileEntity> retVal = new HashMap<>(handle.i.size());
        handle.i.forEach((blockPosition, tileEntity) ->
                retVal.put(new BlockPosition(blockPosition), new TileEntity(tileEntity)));
        return retVal;
    }

    public Set<net.minecraft.core.BlockPosition> getTilePositions() {
        return handle.i.keySet();
    }

    public WorldServer getWorld() {
        return new WorldServer(((Chunk) handle).q);
    }

    public PlayerChunk.State getState() {
        return ((Chunk) handle).B();
    }

    public void setNeedsSaving(boolean needsSaving) {
        handle.a(needsSaving);
    }

    public IRegistry<BiomeBase> getBiomeRegistry() {
        return handle.biomeRegistry;
    }

    public NBTTagCompound getTileEntityNBT(BlockPosition blockPosition) {
        return new NBTTagCompound(handle.i(blockPosition.getHandle()));
    }

    public org.bukkit.Chunk getBukkitChunk() {
        return ((Chunk) handle).getBukkitChunk();
    }

    public HeightMap getHeightmap(net.minecraft.world.level.levelgen.HeightMap.Type type) {
        return HeightMap.ofNullable(handle.g.get(type));
    }

}
