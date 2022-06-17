package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.nbt;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NBTTagCompound extends MappedObject<net.minecraft.nbt.NBTTagCompound> {

    public NBTTagCompound() {
        this(new net.minecraft.nbt.NBTTagCompound());
    }

    public NBTTagCompound(net.minecraft.nbt.NBTTagCompound handle) {
        super(handle);
    }

    @Nullable
    public static NBTTagCompound ofNullable(net.minecraft.nbt.NBTTagCompound handle) {
        return handle == null ? null : new NBTTagCompound(handle);
    }

    public void remove(String key) {
        handle.r(key);
    }

    public boolean isEmpty() {
        return handle.f();
    }

    public void setInt(String key, int value) {
        handle.a(key, value);
    }

    public boolean hasKey(String key) {
        return handle.e(key);
    }

    public void setString(String key, String value) {
        handle.a(key, value);
    }

    public void setBoolean(String key, boolean value) {
        handle.a(key, value);
    }

    public void setByte(String key, byte value) {
        handle.a(key, value);
    }

    public NBTTagList getList(String key, int type) {
        return handle.c(key, type);
    }

    public byte getByte(String key) {
        return handle.f(key);
    }

    public boolean hasKeyOfType(String key, int type) {
        return handle.b(key, type);
    }

    public void set(String key, NBTBase nbtBase) {
        handle.a(key, nbtBase);
    }

    public NBTTagCompound getCompound(String key) {
        return new NBTTagCompound(handle.p(key));
    }

    public void setUUID(String key, UUID uuid) {
        handle.a(key, uuid);
    }

}
