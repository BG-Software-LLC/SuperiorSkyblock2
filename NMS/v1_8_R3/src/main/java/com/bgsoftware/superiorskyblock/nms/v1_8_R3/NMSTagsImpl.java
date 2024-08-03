package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagByteArray;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagIntArray;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagShort;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;

import java.util.Set;

@SuppressWarnings({"unused"})
public class NMSTagsImpl implements NMSTags {

    @Override
    public CompoundTag serializeItem(org.bukkit.inventory.ItemStack bukkitItem) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        NBTTagCompound tagCompound = itemStack.save(new NBTTagCompound());

        return CompoundTag.fromNBT(tagCompound);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserializeItem(CompoundTag compoundTag) {
        if (compoundTag.containsKey("NBT")) {
            // Old compound version, deserialize it accordingly
            return deserializeItemOld(compoundTag);
        }

        NBTTagCompound tagCompound = (NBTTagCompound) compoundTag.toNBT();
        ItemStack itemStack = ItemStack.createStack(tagCompound);

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
        itemStack.setTag((NBTTagCompound) nbtData.toNBT());

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public CompoundTag getNBTTag(org.bukkit.entity.Entity bukkitEntity) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entity.c(nbtTagCompound);
        nbtTagCompound.set("Yaw", new NBTTagFloat(entity.yaw));
        nbtTagCompound.set("Pitch", new NBTTagFloat(entity.pitch));
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    public void spawnEntity(EntityType entityType, Location location, CompoundTag compoundTag) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) compoundTag.toNBT();

        if (nbtTagCompound == null)
            nbtTagCompound = new NBTTagCompound();

        if (!nbtTagCompound.hasKey("id"))
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
