package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.chunk;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.block.state.BlockData;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import org.jetbrains.annotations.Nullable;

public final class ChunkSection extends MappedObject<net.minecraft.world.level.chunk.ChunkSection> {

    public ChunkSection(net.minecraft.world.level.chunk.ChunkSection handle) {
        super(handle);
    }

    @Nullable
    public static ChunkSection ofNullable(@Nullable net.minecraft.world.level.chunk.ChunkSection handle) {
        return handle == null ? null : new ChunkSection(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunkSection",
            name = "getStates",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public DataPaletteBlock<IBlockData> getBlocks() {
        return handle.i();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunkSection",
            name = "getBiomes",
            type = Remap.Type.METHOD,
            remappedName = "j")
    public DataPaletteBlock<BiomeBase> getBiomes() {
        return handle.j();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunkSection",
            name = "setBlockState",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setType(int x, int y, int z, BlockData state, boolean lock) {
        handle.a(x, y, z, state.getHandle(), lock);
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunkSection",
            name = "getBlockState",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public BlockData getType(int x, int y, int z) {
        return new BlockData(handle.a(x, y, z));
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunkSection",
            name = "bottomBlockY",
            type = Remap.Type.METHOD,
            remappedName = "g")
    public int getYPosition() {
        return handle.g();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunkSection",
            name = "isRandomlyTicking",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public boolean isRandomlyTicking() {
        return handle.d();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunkSection",
            name = "hasOnlyAir",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public boolean isEmpty() {
        return handle.c();
    }

}
