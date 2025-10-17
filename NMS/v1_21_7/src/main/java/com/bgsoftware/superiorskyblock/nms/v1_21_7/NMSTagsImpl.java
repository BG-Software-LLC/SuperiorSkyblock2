package com.bgsoftware.superiorskyblock.nms.v1_21_7;

import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;

public class NMSTagsImpl extends com.bgsoftware.superiorskyblock.nms.v1_21_7.AbstractNMSTags {

    private static final Logger LOGGER = LogUtils.getLogger();

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
        RegistryOps<Tag> context = MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE);

        net.minecraft.nbt.CompoundTag tagCompound = (net.minecraft.nbt.CompoundTag)
                ItemStack.CODEC.encodeStart(context, itemStack).getOrThrow();
        tagCompound.putInt("DataVersion", com.bgsoftware.superiorskyblock.nms.v1_21_7.AbstractNMSAlgorithms.DATA_VERSION);

        return CompoundTag.fromNBT(tagCompound);
    }

    @Override
    protected int getCompoundTagInt(net.minecraft.nbt.CompoundTag compoundTag, String key, int def) {
        return compoundTag.getIntOr(key, def);
    }

    @Override
    protected ItemStack parseItemStack(net.minecraft.nbt.CompoundTag compoundTag) {
        RegistryOps<net.minecraft.nbt.Tag> context =
                MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE);

        return ItemStack.CODEC.parse(context, compoundTag)
                .resultOrPartial((itemId) -> LOGGER.error("Tried to load invalid item: '{}'", itemId))
                .orElseThrow();
    }

    @Override
    protected void setItemStackCompoundTag(ItemStack itemStack, net.minecraft.nbt.CompoundTag compoundTag) {
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
    }

    @Override
    protected void loadEntity(net.minecraft.nbt.CompoundTag compoundTag, ServerLevel serverLevel, Location location) {
        EntityType.loadEntityRecursive(compoundTag, serverLevel, EntitySpawnReason.NATURAL, entity -> {
            entity.absSnapTo(location.getX(), location.getY(), location.getZ(), entity.getYRot(), entity.getXRot());
            return !serverLevel.addWithUUID(entity) ? null : entity;
        });
    }

    @Override
    public byte[] getNBTByteArrayValue(Object object) {
        return ((ByteArrayTag) object).getAsByteArray();
    }

    @Override
    public byte getNBTByteValue(Object object) {
        return ((NumericTag) object).byteValue();
    }

    @Override
    public Set<String> getNBTCompoundValue(Object object) {
        return ((net.minecraft.nbt.CompoundTag) object).keySet();
    }

    @Override
    public double getNBTDoubleValue(Object object) {
        return ((NumericTag) object).doubleValue();
    }

    @Override
    public float getNBTFloatValue(Object object) {
        return ((NumericTag) object).floatValue();
    }

    @Override
    public int[] getNBTIntArrayValue(Object object) {
        return ((IntArrayTag) object).getAsIntArray();
    }

    @Override
    public int getNBTIntValue(Object object) {
        return ((NumericTag) object).intValue();
    }

    @Override
    public Object getNBTListIndexValue(Object object, int index) {
        return ((net.minecraft.nbt.ListTag) object).get(index);
    }

    @Override
    public long getNBTLongValue(Object object) {
        return ((NumericTag) object).longValue();
    }

    @Override
    public short getNBTShortValue(Object object) {
        return ((NumericTag) object).shortValue();
    }

    @Override
    public String getNBTStringValue(Object object) {
        return ((StringTag) object).value();
    }

}
