package com.ome_r.superiorskyblock.nms;

import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.utils.key.Key;
import com.ome_r.superiorskyblock.utils.jnbt.CompoundTag;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.LivingEntity;
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

    void setWorldBorder(WrappedPlayer wrappedPlayer, Island island);

    void setSkinTexture(WrappedPlayer wrappedPlayer);

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
}
