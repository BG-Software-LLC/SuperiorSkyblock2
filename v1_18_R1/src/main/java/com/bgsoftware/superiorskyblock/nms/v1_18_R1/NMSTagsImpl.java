package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

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
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;

import java.util.Set;

import static com.bgsoftware.superiorskyblock.nms.v1_18_R1.NMSMappings.*;

@SuppressWarnings({"unused", "rawtypes"})
public final class NMSTagsImpl implements NMSTags {

    @Override
    public CompoundTag getNBTTag(org.bukkit.inventory.ItemStack bukkitStack) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        NBTTagCompound nbtTagCompound = getOrCreateTag(itemStack);
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    public CompoundTag convertToNBT(org.bukkit.inventory.ItemStack bukkitItem) {
        return CompoundTag.fromNBT(save(CraftItemStack.asNMSCopy(bukkitItem), new NBTTagCompound()));
    }

    @Override
    public org.bukkit.inventory.ItemStack getFromNBTTag(org.bukkit.inventory.ItemStack bukkitStack, CompoundTag compoundTag) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        setTag(itemStack, (NBTTagCompound) compoundTag.toNBT());
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public CompoundTag getNBTTag(org.bukkit.entity.Entity bukkitEntity) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        save(entity, nbtTagCompound);
        set(nbtTagCompound, "Yaw", NBTTagFloat.a(getYRot(entity)));
        set(nbtTagCompound, "Pitch", NBTTagFloat.a(getXRot(entity)));
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void spawnEntity(EntityType entityType, Location location, CompoundTag compoundTag) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) compoundTag.toNBT();

        if (nbtTagCompound == null)
            nbtTagCompound = new NBTTagCompound();

        if (!hasKey(nbtTagCompound, "id"))
            //noinspection deprecation
            setString(nbtTagCompound, "id", getKey(new MinecraftKey(entityType.getName())));

        Entity entity = EntityTypes.a(nbtTagCompound, craftWorld.getHandle(), (_entity) -> {
            setPositionRotation(_entity, location.getX(), location.getY(), location.getZ(), getYRot(_entity), getXRot(_entity));
            return !addEntitySerialized(craftWorld.getHandle(), _entity) ? null : _entity;
        });
    }

    @Override
    public byte[] getNBTByteArrayValue(Object object) {
        return ((NBTTagByteArray) object).d();
    }

    @Override
    public byte getNBTByteValue(Object object) {
        return ((NBTTagByte) object).h();
    }

    @Override
    public Set<String> getNBTCompoundValue(Object object) {
        return ((NBTTagCompound) object).d();
    }

    @Override
    public double getNBTDoubleValue(Object object) {
        return ((NBTTagDouble) object).i();
    }

    @Override
    public float getNBTFloatValue(Object object) {
        return ((NBTTagFloat) object).j();
    }

    @Override
    public int[] getNBTIntArrayValue(Object object) {
        return ((NBTTagIntArray) object).f();
    }

    @Override
    public int getNBTIntValue(Object object) {
        return ((NBTTagInt) object).f();
    }

    @Override
    public Object getNBTListIndexValue(Object object, int index) {
        return ((NBTTagList) object).get(index);
    }

    @Override
    public long getNBTLongValue(Object object) {
        return ((NBTTagLong) object).e();
    }

    @Override
    public short getNBTShortValue(Object object) {
        return ((NBTTagShort) object).g();
    }

    @Override
    public String getNBTStringValue(Object object) {
        return ((NBTTagString) object).e_();
    }

    @Override
    public Object parseList(ListTag listTag) {
        NBTTagList nbtTagList = new NBTTagList();

        for (Tag tag : listTag.getValue())
            nbtTagList.add((NBTBase) tag.toNBT());

        return nbtTagList;
    }

}
