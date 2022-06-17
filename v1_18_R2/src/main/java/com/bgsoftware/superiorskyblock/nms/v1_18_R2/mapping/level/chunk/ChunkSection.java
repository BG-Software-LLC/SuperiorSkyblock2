package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.chunk;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.state.BlockData;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import org.jetbrains.annotations.Nullable;

public class ChunkSection extends MappedObject<net.minecraft.world.level.chunk.ChunkSection> {

    public ChunkSection(net.minecraft.world.level.chunk.ChunkSection handle) {
        super(handle);
    }

    @Nullable
    public static ChunkSection ofNullable(@Nullable net.minecraft.world.level.chunk.ChunkSection handle) {
        return handle == null ? null : new ChunkSection(handle);
    }

    public DataPaletteBlock<IBlockData> getBlocks() {
        return handle.i();
    }

    public DataPaletteBlock<Holder<BiomeBase>> getBiomes() {
        return handle.j();
    }

    public void setType(int x, int y, int z, BlockData state, boolean lock) {
        handle.a(x, y, z, state.getHandle(), lock);
    }

    public BlockData getType(int x, int y, int z) {
        return new BlockData(handle.a(x, y, z));
    }

    public int getYPosition() {
        return handle.g();
    }

    public boolean isRandomlyTicking() {
        return handle.d();
    }

    public boolean isEmpty() {
        return handle.c();
    }

}
