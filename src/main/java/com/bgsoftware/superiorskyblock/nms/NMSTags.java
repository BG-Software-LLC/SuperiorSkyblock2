package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public interface NMSTags {

    CompoundTag getNBTTag(ItemStack itemStack);

    ItemStack getFromNBTTag(ItemStack itemStack, CompoundTag compoundTag);

    CompoundTag getNBTTag(LivingEntity livingEntity);

    void getFromNBTTag(LivingEntity livingEntity, CompoundTag compoundTag);

    byte[] getNBTByteArrayValue(Object object);

    byte getNBTByteValue(Object object);

    Set<String> getNBTCompoundValue(Object object);

    double getNBTDoubleValue(Object object);

    float getNBTFloatValue(Object object);

    int[] getNBTIntArrayValue(Object object);

    int getNBTIntValue(Object object);

    Object getNBTListIndexValue(Object object, int index);

    long getNBTLongValue(Object object);

    short getNBTShortValue(Object object);

    String getNBTStringValue(Object object);

    Object parseList(ListTag listTag);

}
