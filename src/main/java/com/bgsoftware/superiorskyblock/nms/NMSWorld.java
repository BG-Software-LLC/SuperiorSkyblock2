package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.blocks.ICachedBlock;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.generator.ChunkGenerator;

import java.util.List;

public interface NMSWorld {

    Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z);

    int getSpawnerDelay(CreatureSpawner creatureSpawner);

    void setSpawnerDelay(CreatureSpawner creatureSpawner, int spawnDelay);

    void setWorldBorder(SuperiorPlayer superiorPlayer, Island island);

    void setBiome(ChunkGenerator.BiomeGrid biomeGrid, Biome biome);

    Object getBlockData(Block block);

    void setBlocks(Chunk chunk, List<BlockData> blockDataList);

    void setBlock(Location location, Material material, byte data);

    ICachedBlock cacheBlock(Block block);

    CompoundTag readBlockStates(Location location);

    byte[] getLightLevels(Location location);

    CompoundTag readTileEntity(Location location);

    boolean isWaterLogged(Block block);

    int getDefaultAmount(Block block);

    void placeSign(Island island, Location location);

    void setSignLines(SignChangeEvent signChangeEvent, String[] lines);

    void playGeneratorSound(Location location);

    void playBreakAnimation(Block block);

    void playPlaceSound(Location location);

}
