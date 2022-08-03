package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.item;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.Item;

public final class ItemStack extends MappedObject<net.minecraft.world.item.ItemStack> {

    public ItemStack(net.minecraft.world.item.ItemStack handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getOrCreateTag",
            type = Remap.Type.METHOD,
            remappedName = "v")
    public NBTTagCompound getOrCreateTag() {
        return new NBTTagCompound(handle.v());
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "save",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public NBTTagCompound save(NBTTagCompound nbtTagCompound) {
        return new NBTTagCompound(handle.b(nbtTagCompound.getHandle()));
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "setTag",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public void setTag(net.minecraft.nbt.NBTTagCompound nbtTagCompound) {
        handle.c(nbtTagCompound);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getItem",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public Item getItem() {
        return handle.c();
    }

}
