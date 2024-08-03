package com.bgsoftware.superiorskyblock.nms.v1_19;

import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;

import java.util.Set;

@SuppressWarnings({"unused"})
public class NMSTagsImpl implements NMSTags {

    @Override
    public CompoundTag serializeItem(org.bukkit.inventory.ItemStack bukkitItem) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        net.minecraft.nbt.CompoundTag tagCompound = itemStack.save(new net.minecraft.nbt.CompoundTag());
        tagCompound.putInt("DataVersion", CraftMagicNumbers.INSTANCE.getDataVersion());

        return CompoundTag.fromNBT(tagCompound);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserializeItem(CompoundTag compoundTag) {
        if (compoundTag.containsKey("NBT")) {
            // Old compound version, deserialize it accordingly
            return deserializeItemOld(compoundTag);
        }

        net.minecraft.nbt.CompoundTag tagCompound = (net.minecraft.nbt.CompoundTag) compoundTag.toNBT();

        int currentVersion = CraftMagicNumbers.INSTANCE.getDataVersion();
        int itemVersion = tagCompound.getInt("DataVersion");
        if (itemVersion < currentVersion) {
            tagCompound = (net.minecraft.nbt.CompoundTag) DataFixers.getDataFixer().update(References.ITEM_STACK,
                    new Dynamic<>(NbtOps.INSTANCE, tagCompound), itemVersion, currentVersion).getValue();
        }

        ItemStack itemStack = ItemStack.of(tagCompound);

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
        itemStack.setTag((net.minecraft.nbt.CompoundTag) nbtData.toNBT());

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public CompoundTag getNBTTag(org.bukkit.entity.Entity bukkitEntity) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        net.minecraft.nbt.CompoundTag compoundTag = new net.minecraft.nbt.CompoundTag();
        entity.save(compoundTag);
        compoundTag.putFloat("Yaw", entity.getYRot());
        compoundTag.putFloat("Pitch", entity.getXRot());
        return CompoundTag.fromNBT(compoundTag);
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
