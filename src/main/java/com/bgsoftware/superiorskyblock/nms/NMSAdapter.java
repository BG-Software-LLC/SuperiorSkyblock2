package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public interface NMSAdapter {

    int getCombinedId(Location location);

    void setBlock(Location location, int combinedId);

    ItemStack getFlowerPot(Location location);

    void setFlowerPot(Location location, ItemStack itemStack);

    CompoundTag getNBTTag(ItemStack itemStack);

    ItemStack getFromNBTTag(ItemStack itemStack, CompoundTag compoundTag);

    CompoundTag getNBTTag(LivingEntity livingEntity);

    void getFromNBTTag(LivingEntity livingEntity, CompoundTag compoundTag);

    Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z);

    int getSpawnerDelay(CreatureSpawner creatureSpawner);

    void refreshChunk(Chunk chunk);

    void setWorldBorder(SuperiorPlayer superiorPlayer, Island island);

    void setSkinTexture(SuperiorPlayer superiorPlayer);

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

    default Object getCustomHolder(InventoryHolder defaultHolder, String title){
        return defaultHolder;
    }

    void clearInventory(OfflinePlayer offlinePlayer);

    void playGeneratorSound(Location location);

    void setBiome(Location min, Location max, Biome biome);

    default Object getBlockData(Block block){
        return null;
    }

    Enchantment getGlowEnchant();

}
