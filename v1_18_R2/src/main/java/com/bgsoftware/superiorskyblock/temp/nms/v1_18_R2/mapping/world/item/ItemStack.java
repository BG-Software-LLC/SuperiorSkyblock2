package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.world.item;

import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.nbt.NBTTagCompound;
import net.minecraft.world.item.Item;

public class ItemStack extends MappedObject<net.minecraft.world.item.ItemStack> {

    public ItemStack(net.minecraft.world.item.ItemStack handle) {
        super(handle);
    }

    public NBTTagCompound getOrCreateTag() {
        return new NBTTagCompound(handle.u());
    }

    public NBTTagCompound save(NBTTagCompound nbtTagCompound) {
        return new NBTTagCompound(handle.b(nbtTagCompound.getHandle()));
    }

    public void setTag(net.minecraft.nbt.NBTTagCompound nbtTagCompound) {
        handle.c(nbtTagCompound);
    }

    public Item getItem() {
        return handle.c();
    }

}
