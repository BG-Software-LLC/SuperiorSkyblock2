package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.block.entity;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.jetbrains.annotations.Nullable;

public final class TileEntity extends MappedObject<net.minecraft.world.level.block.entity.TileEntity> {

    public TileEntity(net.minecraft.world.level.block.entity.TileEntity handle) {
        super(handle);
    }

    @Nullable
    public static TileEntity ofNullable(@Nullable net.minecraft.world.level.block.entity.TileEntity handle) {
        return handle == null ? null : new TileEntity(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.BlockEntity",
            name = "getBlockPos",
            type = Remap.Type.METHOD,
            remappedName = "p")
    public BlockPosition getPosition() {
        return new BlockPosition(handle.p());
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.BlockEntity",
            name = "load",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void load(NBTTagCompound nbtTagCompound) {
        handle.a(nbtTagCompound.getHandle());
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.BlockEntity",
            name = "saveWithFullMetadata",
            type = Remap.Type.METHOD,
            remappedName = "m")
    public NBTTagCompound save() {
        return new NBTTagCompound(handle.m());
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.SpawnerBlockEntity",
            name = "getSpawner",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public MobSpawnerAbstract getSpawner() {
        return ((TileEntityMobSpawner) handle).d();
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.SignBlockEntity",
            name = "messages",
            type = Remap.Type.FIELD,
            remappedName = "d")
    public IChatBaseComponent[] getSignLines() {
        return ((TileEntitySign) handle).d;
    }

}
