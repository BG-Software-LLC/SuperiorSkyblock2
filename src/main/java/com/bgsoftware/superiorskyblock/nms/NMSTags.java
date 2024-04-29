package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public interface NMSTags {

    CompoundTag getNBTTag(ItemStack itemStack);

    CompoundTag convertToNBT(ItemStack itemStack);

    ItemStack getFromNBTTag(ItemStack itemStack, CompoundTag compoundTag);

    default ItemStack getSkullWithTexture(ItemStack itemStack, String texture) {
        return ItemSkulls.getPlayerHeadNoNMS(itemStack, texture);
    }

    CompoundTag getNBTTag(Entity entity);

    void spawnEntity(EntityType entityType, Location location, CompoundTag compoundTag);

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

    Object getNBTCompoundTag(Object object, String key);

    void setNBTCompoundTagValue(Object object, String key, Object value);

    int getNBTTagListSize(Object object);

}
