package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;

import java.util.Set;

@SuppressWarnings({"unused"})
public class NMSTagsImpl implements NMSTags {

    @Override
    public CompoundTag getNBTTag(org.bukkit.inventory.ItemStack bukkitStack) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        NBTTagCompound nbtTagCompound = itemStack.getOrCreateTag();
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    public CompoundTag convertToNBT(org.bukkit.inventory.ItemStack bukkitItem) {
        return CompoundTag.fromNBT(CraftItemStack.asNMSCopy(bukkitItem).save(new NBTTagCompound()));
    }

    @Override
    public org.bukkit.inventory.ItemStack getFromNBTTag(org.bukkit.inventory.ItemStack bukkitStack, CompoundTag compoundTag) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        itemStack.setTag((NBTTagCompound) compoundTag.toNBT());
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public CompoundTag getNBTTag(org.bukkit.entity.Entity bukkitEntity) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entity.save(nbtTagCompound);
        nbtTagCompound.set("Yaw", NBTTagFloat.a(entity.getYRot()));
        nbtTagCompound.set("Pitch", NBTTagFloat.a(entity.getXRot()));
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void spawnEntity(EntityType entityType, Location location, CompoundTag compoundTag) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) compoundTag.toNBT();

        if (nbtTagCompound == null)
            nbtTagCompound = new NBTTagCompound();

        if (!nbtTagCompound.hasKey("id"))
            //noinspection deprecation
            nbtTagCompound.setString("id", new MinecraftKey(entityType.getName()).getKey());

        Entity entity = EntityTypes.a(nbtTagCompound, craftWorld.getHandle(), (_entity) -> {
            _entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), _entity.getYRot(), _entity.getXRot());
            return !craftWorld.getHandle().addEntitySerialized(_entity) ? null : _entity;
        });
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

        for (Tag<?> tag : listTag)
            nbtTagList.add((NBTBase) tag.toNBT());

        return nbtTagList;
    }

    @Override
    public Object getNBTCompoundTag(Object object, String key) {
        return ((NBTTagCompound) object).get(key);
    }

    @Override
    public void setNBTCompoundTagValue(Object object, String key, Object value) {
        ((NBTTagCompound) object).set(key, (NBTBase) value);
    }

    @Override
    public int getNBTTagListSize(Object object) {
        return ((NBTTagList) object).size();
    }

}
