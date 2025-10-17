package com.bgsoftware.superiorskyblock.nms.v1_20_4;

import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

import java.util.Optional;
import java.util.Set;

public class NMSTagsImpl extends com.bgsoftware.superiorskyblock.nms.v1_20_4.AbstractNMSTags {

    @Override
    public org.bukkit.inventory.ItemStack getSkullWithTexture(org.bukkit.inventory.ItemStack bukkitItem, String texture) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put("textures", new Property("textures", texture));

        ResolvableProfile resolvableProfile = new ResolvableProfile(Optional.empty(), Optional.empty(), propertyMap);

        itemStack.set(DataComponents.PROFILE, resolvableProfile);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    protected CompoundTag setTagAndGetCompoundTag(ItemStack itemStack, String key, int value) {
        net.minecraft.nbt.CompoundTag tagCompound = (net.minecraft.nbt.CompoundTag)
                itemStack.save(MinecraftServer.getServer().registryAccess());
        tagCompound.putInt("DataVersion", com.bgsoftware.superiorskyblock.nms.v1_20_4.AbstractNMSAlgorithms.DATA_VERSION);
        return CompoundTag.fromNBT(tagCompound);
    }

    @Override
    protected int getCompoundTagInt(net.minecraft.nbt.CompoundTag compoundTag, String key, int def) {
        return compoundTag.contains(key, 3) ? compoundTag.getInt(key) : def;
    }

    @Override
    protected ItemStack parseItemStack(net.minecraft.nbt.CompoundTag compoundTag) {
        return ItemStack.parse(MinecraftServer.getServer().registryAccess(), compoundTag).orElseThrow();
    }

    @Override
    protected void setItemStackCompoundTag(ItemStack itemStack, net.minecraft.nbt.CompoundTag compoundTag) {
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
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
