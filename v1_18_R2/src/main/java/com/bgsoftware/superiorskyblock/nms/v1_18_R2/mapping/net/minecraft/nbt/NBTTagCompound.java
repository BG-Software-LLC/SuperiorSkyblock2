package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.nbt;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.Nullable;

public final class NBTTagCompound extends MappedObject<net.minecraft.nbt.NBTTagCompound> {

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

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "remove",
            type = Remap.Type.METHOD,
            remappedName = "r")
    public void remove(String key) {
        handle.r(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "isEmpty",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public boolean isEmpty() {
        return handle.f();
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "putInt",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setInt(String key, int value) {
        handle.a(key, value);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "contains",
            type = Remap.Type.METHOD,
            remappedName = "e")
    public boolean hasKey(String key) {
        return handle.e(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "putString",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setString(String key, String value) {
        handle.a(key, value);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "putByte",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setByte(String key, byte value) {
        handle.a(key, value);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getList",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public NBTTagList getList(String key, int type) {
        return handle.c(key, type);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getByte",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public byte getByte(String key) {
        return handle.f(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "contains",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public boolean hasKeyOfType(String key, int type) {
        return handle.b(key, type);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "put",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void set(String key, NBTBase nbtBase) {
        handle.a(key, nbtBase);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getCompound",
            type = Remap.Type.METHOD,
            remappedName = "p")
    public NBTTagCompound getCompound(String key) {
        return new NBTTagCompound(handle.p(key));
    }

}
