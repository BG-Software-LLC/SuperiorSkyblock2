package com.bgsoftware.superiorskyblock.nms.v1_18;

import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;

import java.util.Set;

public class NMSTagsImpl extends com.bgsoftware.superiorskyblock.nms.v1_18.AbstractNMSTags {

    @Override
    protected CompoundTag setTagAndGetCompoundTag(ItemStack itemStack, String key, int value) {
        net.minecraft.nbt.CompoundTag tagCompound = itemStack.save(new net.minecraft.nbt.CompoundTag());
        tagCompound.putInt("DataVersion", com.bgsoftware.superiorskyblock.nms.v1_18.AbstractNMSAlgorithms.DATA_VERSION);
        return CompoundTag.fromNBT(tagCompound);
    }

    @Override
    protected int getCompoundTagInt(net.minecraft.nbt.CompoundTag compoundTag, String key, int def) {
        return compoundTag.contains(key, 3) ? compoundTag.getInt(key) : def;
    }

    @Override
    protected ItemStack parseItemStack(net.minecraft.nbt.CompoundTag compoundTag) {
        return ItemStack.of(compoundTag);
    }

    @Override
    protected void setItemStackCompoundTag(ItemStack itemStack, net.minecraft.nbt.CompoundTag compoundTag) {
        itemStack.setTag(compoundTag);
    }

    @Override
    protected void loadEntity(net.minecraft.nbt.CompoundTag compoundTag, ServerLevel serverLevel, Location location) {
        EntityType.loadEntityRecursive(compoundTag, serverLevel, entity -> {
            entity.absMoveTo(location.getX(), location.getY(), location.getZ(), entity.getYRot(), entity.getXRot());
            return !serverLevel.addWithUUID(entity) ? null : entity;
        });
    }

    @Override
    public byte[] getNBTByteArrayValue(Object object) {
        return ((ByteArrayTag) object).getAsByteArray();
    }

    @Override
    public byte getNBTByteValue(Object object) {
        return ((NumericTag) object).getAsByte();
    }

    @Override
    public Set<String> getNBTCompoundValue(Object object) {
        return ((net.minecraft.nbt.CompoundTag) object).getAllKeys();
    }

    @Override
    public double getNBTDoubleValue(Object object) {
        return ((NumericTag) object).getAsDouble();
    }

    @Override
    public float getNBTFloatValue(Object object) {
        return ((NumericTag) object).getAsFloat();
    }

    @Override
    public int[] getNBTIntArrayValue(Object object) {
        return ((IntArrayTag) object).getAsIntArray();
    }

    @Override
    public int getNBTIntValue(Object object) {
        return ((NumericTag) object).getAsInt();
    }

    @Override
    public Object getNBTListIndexValue(Object object, int index) {
        return ((net.minecraft.nbt.ListTag) object).get(index);
    }

    @Override
    public long getNBTLongValue(Object object) {
        return ((NumericTag) object).getAsLong();
    }

    @Override
    public short getNBTShortValue(Object object) {
        return ((NumericTag) object).getAsShort();
    }

    @Override
    public String getNBTStringValue(Object object) {
        return ((StringTag) object).getAsString();
    }

}
