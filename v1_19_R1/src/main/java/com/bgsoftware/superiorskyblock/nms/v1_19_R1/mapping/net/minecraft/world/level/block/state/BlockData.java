package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.state;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.Block;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.SoundEffectType;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.state.properties.BlockState;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.material.Material;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;

import java.util.Map;

public final class BlockData extends MappedObject<IBlockData> {

    public BlockData(IBlockData handle) {
        super(handle);
    }

    public static BlockData ofNullable(IBlockData blockData) {
        return blockData == null ? null : new BlockData(blockData);
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase",
            name = "getBlock",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public Block getBlock() {
        return new Block(handle.b());
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase",
            name = "getMaterial",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public Material getMaterial() {
        return new Material(handle.d());
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.StateHolder",
            name = "getValues",
            type = Remap.Type.METHOD,
            remappedName = "w")
    public Map<IBlockState<?>, Comparable<?>> getStateMap() {
        return handle.w();
    }


    public <T extends Comparable<T>> T get(BlockState<T> blockState) {
        return get(blockState.getHandle());
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.StateHolder",
            name = "getValue",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public <T extends Comparable<T>> T get(IBlockState<T> blockState) {
        return handle.c(blockState);
    }

    public <T extends Comparable<T>, V extends T> BlockData set(BlockState<T> blockState, V value) {
        return set(blockState.getHandle(), value);
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.StateHolder",
            name = "setValue",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public <T extends Comparable<T>, V extends T> BlockData set(IBlockState<T> blockState, V value) {
        return new BlockData(handle.a(blockState, value));
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase",
            name = "getSoundType",
            type = Remap.Type.METHOD,
            remappedName = "r")
    public SoundEffectType getStepSound() {
        return new SoundEffectType(handle.r());
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase",
            name = "randomTick",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void randomTick(WorldServer worldServer, BlockPosition blockPosition, RandomSource random) {
        handle.b(worldServer.getHandle(), blockPosition.getHandle(), random.getHandle());
    }

}
