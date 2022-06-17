package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.level.block.entity;

import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.jetbrains.annotations.Nullable;

public class TileEntity extends MappedObject<net.minecraft.world.level.block.entity.TileEntity> {

    public TileEntity(net.minecraft.world.level.block.entity.TileEntity handle) {
        super(handle);
    }

    @Nullable
    public static TileEntity ofNullable(@Nullable net.minecraft.world.level.block.entity.TileEntity handle) {
        return handle == null ? null : new TileEntity(handle);
    }

    public BlockPosition getPosition() {
        return new BlockPosition(handle.p());
    }

    public void load(NBTTagCompound nbtTagCompound) {
        handle.a(nbtTagCompound.getHandle());
    }

    public NBTTagCompound save() {
        return new NBTTagCompound(handle.m());
    }

    public MobSpawnerAbstract getSpawner() {
        return ((TileEntityMobSpawner) handle).d();
    }

    public IChatBaseComponent[] getSignLines() {
        return ((TileEntitySign) handle).d;
    }

}
