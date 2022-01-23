package com.bgsoftware.superiorskyblock.nms.v1_18_R1.generator;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

@SuppressWarnings({"unused", "NullableProblems"})
public final class IslandsGeneratorImpl extends IslandsGenerator {

    private final SuperiorSkyblockPlugin plugin;
    private final EnumMap<World.Environment, Biome> biomeEnumMap = new EnumMap<>(World.Environment.class);

    public IslandsGeneratorImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        {
            Biome targetBiome;
            try {
                targetBiome = Biome.valueOf(plugin.getSettings().getWorlds().getNormal().getBiome().toUpperCase());
            } catch (IllegalArgumentException error) {
                targetBiome = Biome.PLAINS;
            }
            
            biomeEnumMap.put(World.Environment.NORMAL, targetBiome == Biome.CUSTOM ? Biome.PLAINS : targetBiome);
        }

        {
            Biome targetBiome;
            try {
                targetBiome = Biome.valueOf(plugin.getSettings().getWorlds().getNether().getBiome().toUpperCase());
            } catch (IllegalArgumentException error) {
                targetBiome = Biome.NETHER_WASTES;
            }
            biomeEnumMap.put(World.Environment.NETHER, targetBiome == Biome.CUSTOM ? Biome.NETHER_WASTES : targetBiome);
        }

        {
            Biome targetBiome;
            try {
                targetBiome = Biome.valueOf(plugin.getSettings().getWorlds().getEnd().getBiome().toUpperCase());
            } catch (IllegalArgumentException error) {
                targetBiome = Biome.THE_END;
            }
            biomeEnumMap.put(World.Environment.THE_END, targetBiome == Biome.CUSTOM ? Biome.THE_END : targetBiome);
        }

    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                @NotNull ChunkData chunkData) {
        if (chunkX == 0 && chunkZ == 0 && worldInfo.getEnvironment() ==
                plugin.getSettings().getWorlds().getDefaultWorld()) {
            chunkData.setBlock(0, 99, 0, Material.BEDROCK);
        }
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return new BiomeProvider() {
            @Override
            public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
                return biomeEnumMap.getOrDefault(worldInfo.getEnvironment(), Biome.PLAINS);
            }

            @Override
            public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
                return new ArrayList<>(biomeEnumMap.values());
            }

        };
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Collections.emptyList();
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, 100, 0);
    }

}
