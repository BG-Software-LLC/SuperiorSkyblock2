package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block.state;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block.state.properties.BlockState;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.material.Material;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block.Block;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;

import java.util.Map;
import java.util.Random;

public class BlockData extends MappedObject<IBlockData> {

    public BlockData(IBlockData handle) {
        super(handle);
    }

    public static BlockData ofNullable(IBlockData blockData) {
        return blockData == null ? null : new BlockData(blockData);
    }

    public Block getBlock() {
        return new Block(handle.b());
    }

    public Material getMaterial() {
        return new Material(handle.c());
    }

    public Map<IBlockState<?>, Comparable<?>> getStateMap() {
        return handle.u();
    }

    public <T extends Comparable<T>> T get(BlockState<T> blockState) {
        return get(blockState.getHandle());
    }

    public <T extends Comparable<T>> T get(IBlockState<T> blockState) {
        return handle.c(blockState);
    }

    public <T extends Comparable<T>, V extends T> BlockData set(BlockState<T> blockState, V value) {
        return set(blockState.getHandle(), value);
    }

    public <T extends Comparable<T>, V extends T> BlockData set(IBlockState<T> blockState, V value) {
        return new BlockData(handle.a(blockState, value));
    }

    public SoundEffectType getStepSound() {
        return new SoundEffectType(handle.q());
    }

    public void randomTick(WorldServer worldServer, BlockPosition blockPosition, Random random) {
        handle.b(worldServer.getHandle(), blockPosition.getHandle(), random);
    }

}
