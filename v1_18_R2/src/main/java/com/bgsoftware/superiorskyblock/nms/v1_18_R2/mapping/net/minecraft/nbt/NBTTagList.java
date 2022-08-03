package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.nbt;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.nbt.NBTBase;

public final class NBTTagList extends MappedObject<net.minecraft.nbt.NBTTagList> {

    public NBTTagList() {
        this(new net.minecraft.nbt.NBTTagList());
    }

    public NBTTagList(net.minecraft.nbt.NBTTagList handle) {
        super(handle);
    }

    public void add(NBTBase nbtBase) {
        handle.add(nbtBase);
    }

    public int size() {
        return handle.size();
    }

    @Remap(classPath = "net.minecraft.nbt.ListTag",
            name = "getCompound",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public NBTTagCompound getCompound(int index) {
        return new NBTTagCompound(handle.a(index));
    }

}
