package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityLiving;
import net.minecraft.server.v1_8_R2.EntityTypes;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.MinecraftKey;
import net.minecraft.server.v1_8_R2.NBTBase;
import net.minecraft.server.v1_8_R2.NBTTagByte;
import net.minecraft.server.v1_8_R2.NBTTagByteArray;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagDouble;
import net.minecraft.server.v1_8_R2.NBTTagFloat;
import net.minecraft.server.v1_8_R2.NBTTagInt;
import net.minecraft.server.v1_8_R2.NBTTagIntArray;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.NBTTagLong;
import net.minecraft.server.v1_8_R2.NBTTagShort;
import net.minecraft.server.v1_8_R2.NBTTagString;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Set;

@SuppressWarnings({"unused", "rawtypes"})
public final class NMSTags_v1_8_R2 implements NMSTags {

    @Override
    public CompoundTag getNBTTag(org.bukkit.inventory.ItemStack bukkitStack) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        NBTTagCompound nbtTagCompound = itemStack.hasTag() ? itemStack.getTag() : new NBTTagCompound();
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
        entityLiving.c(nbtTagCompound);
        nbtTagCompound.set("Yaw", new NBTTagFloat(entityLiving.yaw));
        nbtTagCompound.set("Pitch", new NBTTagFloat(entityLiving.pitch));
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    public void spawnEntity(EntityType entityType, Location location, CompoundTag compoundTag) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) compoundTag.toNBT();

        if(nbtTagCompound == null)
            nbtTagCompound = new NBTTagCompound();

        if(!nbtTagCompound.hasKey("id"))
            //noinspection deprecation
            nbtTagCompound.setString("id", new MinecraftKey(entityType.getName()).a());

        Entity entity = EntityTypes.a(nbtTagCompound, craftWorld.getHandle());
        entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }


    @Override
    public byte[] getNBTByteArrayValue(Object object) {
        return ((NBTTagByteArray) object).c();
    }

    @Override
    public byte getNBTByteValue(Object object) {
        return ((NBTTagByte) object).f();
    }

    @Override
    public Set<String> getNBTCompoundValue(Object object) {
        return ((NBTTagCompound) object).c();
    }

    @Override
    public double getNBTDoubleValue(Object object) {
        return ((NBTTagDouble) object).g();
    }

    @Override
    public float getNBTFloatValue(Object object) {
        return ((NBTTagFloat) object).h();
    }

    @Override
    public int[] getNBTIntArrayValue(Object object) {
        return ((NBTTagIntArray) object).c();
    }

    @Override
    public int getNBTIntValue(Object object) {
        return ((NBTTagInt) object).d();
    }

    @Override
    public Object getNBTListIndexValue(Object object, int index) {
        return ((NBTTagList) object).g(index);
    }

    @Override
    public long getNBTLongValue(Object object) {
        return ((NBTTagLong) object).c();
    }

    @Override
    public short getNBTShortValue(Object object) {
        return ((NBTTagShort) object).e();
    }

    @Override
    public String getNBTStringValue(Object object) {
        return ((NBTTagString) object).a_();
    }

    @Override
    public Object parseList(ListTag listTag) {
        NBTTagList nbtTagList = new NBTTagList();

        for(Tag tag : listTag.getValue())
            nbtTagList.add((NBTBase) tag.toNBT());

        return nbtTagList;
    }

}
