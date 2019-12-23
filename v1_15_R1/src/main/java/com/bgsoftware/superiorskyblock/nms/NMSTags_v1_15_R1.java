package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTTagByte;
import net.minecraft.server.v1_15_R1.NBTTagByteArray;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagDouble;
import net.minecraft.server.v1_15_R1.NBTTagFloat;
import net.minecraft.server.v1_15_R1.NBTTagInt;
import net.minecraft.server.v1_15_R1.NBTTagIntArray;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagLong;
import net.minecraft.server.v1_15_R1.NBTTagShort;
import net.minecraft.server.v1_15_R1.NBTTagString;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;

import java.util.Set;

@SuppressWarnings({"unused", "rawtypes"})
public final class NMSTags_v1_15_R1 implements NMSTags {

    @Override
    public CompoundTag getNBTTag(org.bukkit.inventory.ItemStack bukkitStack) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        NBTTagCompound nbtTagCompound = itemStack.getOrCreateTag();
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    public org.bukkit.inventory.ItemStack getFromNBTTag(org.bukkit.inventory.ItemStack bukkitStack, CompoundTag compoundTag) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        itemStack.setTag((NBTTagCompound) compoundTag.toNBT());
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public CompoundTag getNBTTag(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entityLiving.b(nbtTagCompound);
        nbtTagCompound.set("Yaw", NBTTagFloat.a(entityLiving.yaw));
        nbtTagCompound.set("Pitch", NBTTagFloat.a(entityLiving.pitch));
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    public void getFromNBTTag(LivingEntity livingEntity, CompoundTag compoundTag) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) compoundTag.toNBT();
        if(nbtTagCompound != null) {
            entityLiving.a(nbtTagCompound);
            if(nbtTagCompound.hasKey("Yaw") && nbtTagCompound.hasKey("Pitch")){
                entityLiving.setLocation(
                        entityLiving.locX(), entityLiving.locY(), entityLiving.locZ(),
                        nbtTagCompound.getFloat("Yaw"),
                        nbtTagCompound.getFloat("Pitch")
                );
            }
        }
    }

    @Override
    public byte[] getNBTByteArrayValue(Object object) {
        return ((NBTTagByteArray) object).getBytes();
    }

    @Override
    public byte getNBTByteValue(Object object) {
        return ((NBTTagByte) object).asByte();
    }

    @Override
    public Set<String> getNBTCompoundValue(Object object) {
        return ((NBTTagCompound) object).getKeys();
    }

    @Override
    public double getNBTDoubleValue(Object object) {
        return ((NBTTagDouble) object).asDouble();
    }

    @Override
    public float getNBTFloatValue(Object object) {
        return ((NBTTagFloat) object).asFloat();
    }

    @Override
    public int[] getNBTIntArrayValue(Object object) {
        return ((NBTTagIntArray) object).getInts();
    }

    @Override
    public int getNBTIntValue(Object object) {
        return ((NBTTagInt) object).asInt();
    }

    @Override
    public Object getNBTListIndexValue(Object object, int index) {
        return ((NBTTagList) object).get(index);
    }

    @Override
    public long getNBTLongValue(Object object) {
        return ((NBTTagLong) object).asLong();
    }

    @Override
    public short getNBTShortValue(Object object) {
        return ((NBTTagShort) object).asShort();
    }

    @Override
    public String getNBTStringValue(Object object) {
        return ((NBTTagString) object).asString();
    }

    @Override
    public Object parseList(ListTag listTag) {
        NBTTagList nbtTagList = new NBTTagList();

        for(Tag tag : listTag.getValue())
            nbtTagList.add((NBTBase) tag.toNBT());

        return nbtTagList;
    }

}
