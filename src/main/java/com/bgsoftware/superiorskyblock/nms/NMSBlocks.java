package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.schematics.data.BlockType;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.pair.BiPair;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface NMSBlocks {

    void setBlock(Chunk chunk, Location location, int combinedId, BlockType blockType, Object... args);

    void setBlock(Location location, Material material, byte data);

    void refreshChunk(Chunk chunk);

    void refreshLight(Chunk chunk);

    ItemStack getFlowerPot(Location location);

    int getCombinedId(Location location);

    default int getCombinedId(Material material, byte data){
        return 0;
    }

    default int compareMaterials(Material o1, Material o2){
        return Integer.compare(o1.ordinal(), o2.ordinal());
    }

    void setTileEntityBanner(Object tileEntityBanner, DyeColor dyeColor, List<Pattern> patterns);

    void setTileEntityInventoryHolder(Object tileEntityInventoryHolder, ItemStack[] contents, String name);

    void setTileEntityFlowerPot(Object tileEntityFlowerPot, ItemStack flower);

    void setTileEntitySkull(Object tileEntitySkull, SkullType skullType, BlockFace rotation, String owner);

    void setTileEntitySign(Object tileEntitySign, String[] lines);

    void setTileEntityMobSpawner(Object tileEntityMobSpawner, EntityType spawnedType);

    Chunk getChunkIfLoaded(World world, int x, int z);

    CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> loadChunk(World world, int x, int z);

    void deleteChunk(Island island, World world, int x, int z);

    default void setChunkBiome(World world, int x, int z, Biome biome, List<Player> playersToUpdate){

    }

    int tickIslands(int random);

    String getTileName(Location location);

    default Material getMaterial(int combinedId) {
        //noinspection deprecation
        return Material.getMaterial(combinedId & 4095);
    }

    default byte getData(int combinedId) {
        return (byte) (combinedId >> 12 & 15);
    }

    default Key getMinecartBlock(Minecart minecart){
        MaterialData materialData = minecart.getDisplayBlock();
        //noinspection deprecation
        return Key.of(materialData.getItemType(), materialData.getData());
    }

}
