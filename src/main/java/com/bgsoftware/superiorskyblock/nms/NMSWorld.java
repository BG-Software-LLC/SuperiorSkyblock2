package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SchematicBlock;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.generator.ChunkGenerator;

import java.util.List;
import java.util.function.IntFunction;

public interface NMSWorld {

    Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z);

    void listenSpawner(CreatureSpawner creatureSpawner, IntFunction<Integer> delayChangeCallback);

    void setWorldBorder(SuperiorPlayer superiorPlayer, Island island);

    Object getBlockData(Block block);

    void setBlocks(Chunk chunk, List<SchematicBlock> blockDataList);

    void setBlock(Location location, int combinedId);

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

    int getMinHeight(World world);

    void removeAntiXray(World world);

    ChunkGenerator createGenerator(SuperiorSkyblockPlugin plugin);

}
