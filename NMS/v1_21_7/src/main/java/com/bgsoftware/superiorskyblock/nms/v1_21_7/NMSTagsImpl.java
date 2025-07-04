package com.bgsoftware.superiorskyblock.nms.v1_21_7;

import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;

@SuppressWarnings({"unused"})
public class NMSTagsImpl implements NMSTags {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public CompoundTag serializeItem(org.bukkit.inventory.ItemStack bukkitItem) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        RegistryOps<net.minecraft.nbt.Tag> context =
                MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE);

        net.minecraft.nbt.CompoundTag tagCompound = (net.minecraft.nbt.CompoundTag)
                ItemStack.CODEC.encodeStart(context, itemStack).getOrThrow();
        tagCompound.putInt("DataVersion", SharedConstants.getCurrentVersion().dataVersion().version());

        return CompoundTag.fromNBT(tagCompound);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserializeItem(CompoundTag compoundTag) {
        if (compoundTag.containsKey("NBT")) {
            // Old compound version, deserialize it accordingly
            return deserializeItemOld(compoundTag);
        }

        net.minecraft.nbt.CompoundTag tagCompound = (net.minecraft.nbt.CompoundTag) compoundTag.toNBT();

        int currentVersion = SharedConstants.getCurrentVersion().dataVersion().version();
        int itemVersion = tagCompound.getIntOr("DataVersion", 0);
        if (itemVersion < currentVersion) {
            tagCompound = (net.minecraft.nbt.CompoundTag) DataFixers.getDataFixer().update(References.ITEM_STACK,
                    new Dynamic<>(NbtOps.INSTANCE, tagCompound), itemVersion, currentVersion).getValue();
        }

        RegistryOps<net.minecraft.nbt.Tag> context =
                MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE);

        ItemStack itemStack = ItemStack.CODEC.parse(context, tagCompound)
                .resultOrPartial((itemId) -> LOGGER.error("Tried to load invalid item: '{}'", itemId))
                .orElseThrow();

        return CraftItemStack.asCraftMirror(itemStack);
    }

    private static org.bukkit.inventory.ItemStack deserializeItemOld(CompoundTag compoundTag) {
        String typeName = Materials.patchOldMaterialName(compoundTag.getString("type"));
        Material type = Material.valueOf(typeName);
        int amount = compoundTag.getInt("amount");
        short data = compoundTag.getShort("data");
        CompoundTag nbtData = compoundTag.getCompound("NBT");

        org.bukkit.inventory.ItemStack bukkitItem = new org.bukkit.inventory.ItemStack(type, amount, data);
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of((net.minecraft.nbt.CompoundTag) nbtData.toNBT()));

        return CraftItemStack.asCraftMirror(itemStack);
    }

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
    @SuppressWarnings("ConstantConditions")
    public void spawnEntity(org.bukkit.entity.EntityType entityType, Location location, CompoundTag entityTag) {
        net.minecraft.nbt.CompoundTag compoundTag = (net.minecraft.nbt.CompoundTag) entityTag.toNBT();

        if (compoundTag == null)
            compoundTag = new net.minecraft.nbt.CompoundTag();

        if (!compoundTag.contains("id"))
            //noinspection deprecation
            compoundTag.putString("id", entityType.getName());

        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
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

    @Override
    public Object parseList(ListTag listTag) {
        net.minecraft.nbt.ListTag nbtTagList = new net.minecraft.nbt.ListTag();

        for (Tag<?> tag : listTag)
            nbtTagList.add((net.minecraft.nbt.Tag) tag.toNBT());

        return nbtTagList;
    }

    @Override
    public Object getNBTCompoundTag(Object object, String key) {
        return ((net.minecraft.nbt.CompoundTag) object).get(key);
    }

    @Override
    public void setNBTCompoundTagValue(Object object, String key, Object value) {
        ((net.minecraft.nbt.CompoundTag) object).put(key, (net.minecraft.nbt.Tag) value);
    }

    @Override
    public int getNBTTagListSize(Object object) {
        return ((net.minecraft.nbt.ListTag) object).size();
    }

}
