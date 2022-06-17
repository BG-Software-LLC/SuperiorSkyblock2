package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R1.mapping.nbt;

import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R1.mapping.MappedObject;
import net.minecraft.nbt.NBTBase;

public class NBTTagList extends MappedObject<net.minecraft.nbt.NBTTagList> {

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

    public NBTTagCompound getCompound(int index) {
        return new NBTTagCompound(handle.a(index));
    }

}
