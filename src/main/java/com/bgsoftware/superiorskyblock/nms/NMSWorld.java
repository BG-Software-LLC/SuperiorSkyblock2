package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.bridge.PistonPushReaction;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.world.SignType;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.function.IntFunction;

public interface NMSWorld {

    Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z);

    void listenSpawner(Location location, IntFunction<Integer> delayChangeCallback);

    default void replaceTrialBlockPlayerDetector(Island island, Location location) {
        // Does not exist.
    }

    default void replaceSculkSensorListener(Island island, Location location) {
        // Does not exist.
    }

    void setWorldBorder(SuperiorPlayer superiorPlayer, Island island);

    Object getBlockData(Block block);

    void setBlock(Location location, int combinedId);

    ICachedBlock cacheBlock(Block block);

    boolean isWaterLogged(Block block);

    default SignType getSignType(Block block) {
        return getSignType(getBlockData(block));
    }

    SignType getSignType(Object sign);

    PistonPushReaction getPistonReaction(Block block);

    int getDefaultAmount(Block block);

    int getDefaultAmount(BlockState blockState);

    boolean canPlayerSuffocate(Block block);

    void placeSign(Island island, Location location);

    void playGeneratorSound(Location location);

    void playBreakAnimation(Block block);

    void playPlaceSound(Location location);

    int getMinHeight(World world);

    void removeAntiXray(World world);

    IslandsGenerator createGenerator(Dimension dimension);

    WorldEditSession createEditSession(World world);

    WorldEditSession createPartialEditSession(Dimension dimension);

    ChunkReader createChunkReader(Chunk chunk);

}
