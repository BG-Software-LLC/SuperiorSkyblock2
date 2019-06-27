package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.jnbt.ListTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import com.mojang.authlib.properties.Property;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import net.minecraft.server.v1_13_R1.Block;
import net.minecraft.server.v1_13_R1.BlockFlowerPot;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.ChatMessage;
import net.minecraft.server.v1_13_R1.Chunk;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.IBlockData;
import net.minecraft.server.v1_13_R1.ItemStack;
import net.minecraft.server.v1_13_R1.NBTBase;
import net.minecraft.server.v1_13_R1.NBTTagByte;
import net.minecraft.server.v1_13_R1.NBTTagByteArray;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.NBTTagDouble;
import net.minecraft.server.v1_13_R1.NBTTagFloat;
import net.minecraft.server.v1_13_R1.NBTTagInt;
import net.minecraft.server.v1_13_R1.NBTTagIntArray;
import net.minecraft.server.v1_13_R1.NBTTagList;
import net.minecraft.server.v1_13_R1.NBTTagLong;
import net.minecraft.server.v1_13_R1.NBTTagShort;
import net.minecraft.server.v1_13_R1.NBTTagString;
import net.minecraft.server.v1_13_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_13_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_13_R1.TileEntityHopper;
import net.minecraft.server.v1_13_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_13_R1.World;

import net.minecraft.server.v1_13_R1.WorldBorder;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_13_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_13_R1 implements NMSAdapter {

    private SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        IBlockData blockData = world.getType(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return Block.getCombinedId(blockData);
    }

    @Override
    public void setBlock(Location location, int combinedId) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        chunk.a(blockPosition, Block.getByCombinedId(combinedId), true);
    }

    @Override
    public org.bukkit.inventory.ItemStack getFlowerPot(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        BlockFlowerPot blockFlowerPot = (BlockFlowerPot) world.getType(blockPosition).getBlock();
        Block flower;
        try{
            Field flowerField = blockFlowerPot.getClass().getField("c");
            flowerField.setAccessible(true);
            flower = (Block) flowerField.get(blockFlowerPot);
            flowerField.setAccessible(false);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
        ItemStack itemStack = new ItemStack(flower.getItem(), 1);
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public void setFlowerPot(Location location, org.bukkit.inventory.ItemStack itemStack) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        BlockFlowerPot blockFlowerPot = (BlockFlowerPot) world.getType(blockPosition).getBlock();
        ItemStack flower = CraftItemStack.asNMSCopy(itemStack);
        try{
            Field flowerField = blockFlowerPot.getClass().getField("c");
            flowerField.setAccessible(true);
            flowerField.set(blockFlowerPot, Block.asBlock(flower.getItem()));
            flowerField.setAccessible(false);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        world.update(blockPosition, blockFlowerPot);
    }

    @Override
    public CompoundTag getNBTTag(org.bukkit.inventory.ItemStack bukkitStack) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        NBTTagCompound nbtTagCompound = itemStack.hasTag() ? itemStack.getTag() : new NBTTagCompound();
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    public org.bukkit.inventory.ItemStack getFromNBTTag(org.bukkit.inventory.ItemStack bukkitStack, CompoundTag compoundTag) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitStack);
        itemStack.setTag((NBTTagCompound) compoundTag.toNBT());
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public CompoundTag getNBTTag(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entityLiving.b(nbtTagCompound);
        return CompoundTag.fromNBT(nbtTagCompound);
    }

    @Override
    public void getFromNBTTag(LivingEntity livingEntity, CompoundTag compoundTag) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) compoundTag.toNBT();
        if(nbtTagCompound != null)
            entityLiving.a(nbtTagCompound);
    }

    @Override
    public SKey getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        IBlockData blockData = ((CraftBlockData) chunkSnapshot.getBlockData(x, y, z)).getState();
        Material type = chunkSnapshot.getBlockType(x, y, z);
        short data = (short) (Block.getCombinedId(blockData) >> 12 & 15);
        return SKey.of(type, data);
    }

    @Override
    public int getSpawnerDelay(CreatureSpawner creatureSpawner) {
        Location location = creatureSpawner.getLocation();
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)((CraftWorld) location.getWorld())
                .getTileEntityAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return mobSpawner.getSpawner().spawnDelay;
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        World world = ((CraftWorld) bukkitChunk.getWorld()).getHandle();
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        for(EntityHuman entityHuman : world.players)
            ((EntityPlayer) entityHuman).playerConnection.sendPacket(new PacketPlayOutMapChunk(chunk, 65535));
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        try {
            if(!plugin.getSettings().worldBordersEnabled)
                return;

            boolean disabled = !superiorPlayer.hasWorldBorderEnabled();

            WorldBorder worldBorder = new WorldBorder();

            worldBorder.world = ((CraftWorld) superiorPlayer.getWorld()).getHandle();
            worldBorder.setSize(disabled || island == null ? Integer.MAX_VALUE : (island.getIslandSize() * 2) + 1);

            Location center = island == null ? superiorPlayer.getLocation() : island.getCenter();

            if (superiorPlayer.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER) {
                worldBorder.setCenter(center.getX() * 8, center.getZ() * 8);
            } else {
                worldBorder.setCenter(center.getX(), center.getZ());
            }

            PacketPlayOutWorldBorder packetPlayOutWorldBorder = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
            ((CraftPlayer) superiorPlayer.asPlayer()).getHandle().playerConnection.sendPacket(packetPlayOutWorldBorder);
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer) {
        EntityPlayer entityPlayer = ((CraftPlayer) superiorPlayer.asPlayer()).getHandle();
        Optional<Property> optional = entityPlayer.getProfile().getProperties().get("textures").stream().findFirst();
        optional.ifPresent(property -> superiorPlayer.setTextureValue(property.getValue()));
    }

    @Override
    public byte[] getNBTByteArrayValue(Object object) {
        return ((NBTTagByteArray) object).c();
    }

    @Override
    public byte getNBTByteValue(Object object) {
        return ((NBTTagByte) object).g();
    }

    @Override
    public Set<String> getNBTCompoundValue(Object object) {
        return ((NBTTagCompound) object).getKeys();
    }

    @Override
    public double getNBTDoubleValue(Object object) {
        return ((NBTTagDouble) object).asDouble();
    }

    @Override
    public float getNBTFloatValue(Object object) {
        return ((NBTTagFloat) object).i();
    }

    @Override
    public int[] getNBTIntArrayValue(Object object) {
        return ((NBTTagIntArray) object).d();
    }

    @Override
    public int getNBTIntValue(Object object) {
        return ((NBTTagInt) object).e();
    }

    @Override
    public Object getNBTListIndexValue(Object object, int index) {
        return ((NBTTagList) object).get(index);
    }

    @Override
    public long getNBTLongValue(Object object) {
        return ((NBTTagLong) object).d();
    }

    @Override
    public short getNBTShortValue(Object object) {
        return ((NBTTagShort) object).f();
    }

    @Override
    public String getNBTStringValue(Object object) {
        return ((NBTTagString) object).b_();
    }

    @Override
    public Object parseList(ListTag listTag) {
        NBTTagList nbtTagList = new NBTTagList();

        for(Tag tag : listTag.getValue())
            nbtTagList.add((NBTBase) tag.toNBT());

        return nbtTagList;
    }

    @Override
    public Object getCustomHolder(InventoryHolder defaultHolder, String title) {
        return new CustomTileEntityHopper(defaultHolder, title);
    }

    private class CustomTileEntityHopper extends TileEntityHopper{

        private InventoryHolder holder;

        CustomTileEntityHopper(InventoryHolder holder, String title){
            this.holder = holder;
            this.setCustomName(new ChatMessage(title));
        }

        @Override
        public InventoryHolder getOwner() {
            return holder;
        }
    }

}
