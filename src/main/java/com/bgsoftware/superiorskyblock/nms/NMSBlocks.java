package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.blocks.ICachedBlock;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.objects.CalculatedChunk;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.MaterialData;

import java.util.List;

public interface NMSBlocks {

    void setBlocks(Chunk chunk, List<BlockData> blockDataList);

    void setBlock(Location location, Material material, byte data);

    ICachedBlock cacheBlock(Block block);

    default CompoundTag readBlockStates(Location location){
        return null;
    }

    byte[] getLightLevels(Location location);

    default void refreshLights(World bukkitWorld, List<BlockData> blockData){

    }

    CompoundTag readTileEntity(Location location);

    String parseSignLine(String original);

    void refreshChunk(Chunk chunk);

    int getCombinedId(Location location);

    default int getCombinedId(Material material, byte data){
        return 0;
    }

    default int compareMaterials(Material o1, Material o2){
        return Integer.compare(o1.ordinal(), o2.ordinal());
    }

    Chunk getChunkIfLoaded(ChunkPosition chunkPosition);

    void startTickingChunk(Island island, Chunk chunk, boolean stop);

    void handleSignPlace(Island island, Location location);

    default void setSignLines(SignChangeEvent signChangeEvent, String[] lines){

    }

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

    default boolean isWaterLogged(Block block){
        return block.getType().name().contains("WATER");
    }

    int getDefaultAmount(Block block);

}
