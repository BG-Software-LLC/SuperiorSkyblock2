package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.entity.Entity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.item.ItemStack;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.entity.EntityTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;

import java.util.Set;

@SuppressWarnings({"unused"})
public final class NMSTagsImpl implements NMSTags {

    @Override
    public CompoundTag getNBTTag(org.bukkit.inventory.ItemStack bukkitStack) {
        ItemStack itemStack = new ItemStack(CraftItemStack.asNMSCopy(bukkitStack));
        NBTTagCompound nbtTagCompound = itemStack.getOrCreateTag();
        return CompoundTag.fromNBT(nbtTagCompound.getHandle());
    }

    @Override
    public CompoundTag convertToNBT(org.bukkit.inventory.ItemStack bukkitItem) {
        ItemStack itemStack = new ItemStack(CraftItemStack.asNMSCopy(bukkitItem));
        return CompoundTag.fromNBT(itemStack.save(new NBTTagCompound()).getHandle());
    }

    @Override
    public org.bukkit.inventory.ItemStack getFromNBTTag(org.bukkit.inventory.ItemStack bukkitStack, CompoundTag compoundTag) {
        ItemStack itemStack = new ItemStack(CraftItemStack.asNMSCopy(bukkitStack));
        itemStack.setTag((net.minecraft.nbt.NBTTagCompound) compoundTag.toNBT());
        return CraftItemStack.asBukkitCopy(itemStack.getHandle());
    }

    @Override
    public CompoundTag getNBTTag(org.bukkit.entity.Entity bukkitEntity) {
        Entity entity = new Entity(((CraftEntity) bukkitEntity).getHandle());
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entity.save(nbtTagCompound);
        nbtTagCompound.set("Yaw", NBTTagFloat.a(entity.getYRot()));
        nbtTagCompound.set("Pitch", NBTTagFloat.a(entity.getXRot()));
        return CompoundTag.fromNBT(nbtTagCompound.getHandle());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void spawnEntity(EntityType entityType, Location location, CompoundTag compoundTag) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound((net.minecraft.nbt.NBTTagCompound) compoundTag.toNBT());

        if (nbtTagCompound == null)
            nbtTagCompound = new NBTTagCompound();

        if (!nbtTagCompound.hasKey("id"))
            //noinspection deprecation
            nbtTagCompound.setString("id", entityType.getName());

        WorldServer worldServer = new WorldServer(((CraftWorld) location.getWorld()).getHandle());

        EntityTypes.a(nbtTagCompound.getHandle(), worldServer.getHandle(), nmsEntity -> {
            Entity entity = new Entity(nmsEntity);
            entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), entity.getYRot(), entity.getXRot());
            return !worldServer.addEntitySerialized(entity) ? null : entity.getHandle();
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
        return ((net.minecraft.nbt.NBTTagCompound) object).d();
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

        for (Tag<?> tag : listTag)
            nbtTagList.add((NBTBase) tag.toNBT());

        return nbtTagList;
    }

    @Override
    public Object getNBTCompoundTag(Object object, String key) {
        return ((net.minecraft.nbt.NBTTagCompound) object).c(key);
    }

    @Override
    public void setNBTCompoundTagValue(Object object, String key, Object value) {
        ((net.minecraft.nbt.NBTTagCompound) object).a(key, (NBTBase) value);
    }

    @Override
    public int getNBTTagListSize(Object object) {
        return ((NBTTagList) object).size();
    }

}
