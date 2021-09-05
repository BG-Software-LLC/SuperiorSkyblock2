package com.bgsoftware.superiorskyblock.world.generator;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public final class IslandsGenerator extends ChunkGenerator {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Biome NETHER_BIOME = getNetherBiome();
    private final World.Environment defaultWorldEnvironment;

    public IslandsGenerator(World.Environment defaultWorldEnvironment) {
        this.defaultWorldEnvironment = defaultWorldEnvironment;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, 100, 0);
    }

    public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes) {
        byte[][] blockSections = new byte[world.getMaxHeight() / 16][];

        if (world.getEnvironment() == World.Environment.NORMAL) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    biomes.setBiome(x, z, Biome.PLAINS);
                }
            }
        }

        if (chunkX == 0 && chunkZ == 0 && world.getEnvironment() == defaultWorldEnvironment) {
            setBlock(blockSections, 0, 99, 0, 7);
        }

        return blockSections;
    }

    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes) {
        ChunkData chunkData = createChunkData(world);

        switch (world.getEnvironment()) {
            case NETHER: {
                if (NETHER_BIOME != null)
                    plugin.getNMSWorld().setBiome(biomes, NETHER_BIOME);
                break;
            }
            case NORMAL: {
                plugin.getNMSWorld().setBiome(biomes, Biome.PLAINS);
                break;
            }
        }

        if (chunkX == 0 && chunkZ == 0 && world.getEnvironment() == defaultWorldEnvironment) {
            chunkData.setBlock(0, 99, 0, Material.BEDROCK);
        }

        return chunkData;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return new ArrayList<>();
    }

    @SuppressWarnings("SameParameterValue")
    private void setBlock(byte[][] blocks, int x, int y, int z, int blockId) {
        if (blocks[y >> 4] == null)
            blocks[y >> 4] = new byte[4096];

        blocks[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = (byte) blockId;
    }

    private static Biome getNetherBiome() {
        try {
            return Biome.valueOf("NETHER_WASTES");
        } catch (Throwable ex) {
            return null;
        }
    }

}