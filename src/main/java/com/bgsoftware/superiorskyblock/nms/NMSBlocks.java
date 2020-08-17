package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.pair.BiPair;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface NMSBlocks {

    void setBlock(Chunk chunk, Location location, int combinedId, CompoundTag statesTag, CompoundTag tileEntity);

    void setBlock(Location location, Material material, byte data);

    default CompoundTag readBlockStates(Location location){
        return null;
    }

    CompoundTag readTileEntity(Location location);

    String parseSignLine(String original);

    void refreshChunk(Chunk chunk);

    void refreshLight(Chunk chunk);

    int getCombinedId(Location location);

    default int getCombinedId(Material material, byte data){
        return 0;
    }

    default int compareMaterials(Material o1, Material o2){
        return Integer.compare(o1.ordinal(), o2.ordinal());
    }

    Chunk getChunkIfLoaded(ChunkPosition chunkPosition);

    CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> calculateChunk(ChunkPosition chunkPosition);

    void deleteChunk(Island island, ChunkPosition chunkPosition, Runnable onFinish);

    void setChunkBiome(ChunkPosition chunkPosition, Biome biome, List<Player> playersToUpdate);

    void startTickingChunk(Island island, Chunk chunk, boolean stop);

    void handleSignPlace(Island island, Location location);

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
