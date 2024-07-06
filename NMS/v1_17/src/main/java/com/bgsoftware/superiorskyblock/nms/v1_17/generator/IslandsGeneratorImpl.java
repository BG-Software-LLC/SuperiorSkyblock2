package com.bgsoftware.superiorskyblock.nms.v1_17.generator;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.generator.BlockPopulator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings({"unused", "NullableProblems"})
public class IslandsGeneratorImpl extends IslandsGenerator {

    private static final ReflectField<ChunkBiomeContainer> CHUNK_BIOME_CONTAINER = new ReflectField<>(
            new ClassInfo("generator.CustomChunkGenerator$CustomBiomeGrid", ClassInfo.PackageType.CRAFTBUKKIT),
            ChunkBiomeContainer.class, "biome");
    private static final ReflectField<Biome[]> BIOME_BASE_ARRAY = new ReflectField<>(
            ChunkBiomeContainer.class, Biome[].class, "f");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Dimension dimension;

    public IslandsGeneratorImpl(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
        ChunkData chunkData = createChunkData(world);

        org.bukkit.block.Biome targetBiome = IslandUtils.getDefaultWorldBiome(this.dimension);

        setBiome(biomeGrid, targetBiome);

        if (chunkX == 0 && chunkZ == 0 && this.dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension()) {
            chunkData.setBlock(0, 99, 0, Material.BEDROCK);
        }

        return chunkData;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Collections.emptyList();
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, 100, 0);
    }

    private static void setBiome(BiomeGrid biomeGrid, org.bukkit.block.Biome bukkitBiome) {
        ChunkBiomeContainer chunkBiomeContainer = CHUNK_BIOME_CONTAINER.get(biomeGrid);
        Biome[] biomes = BIOME_BASE_ARRAY.get(chunkBiomeContainer);
        Biome biome = CraftBlock.biomeToBiomeBase((Registry<Biome>) chunkBiomeContainer.biomeRegistry, bukkitBiome);
        if (biome != null)
            Arrays.fill(biomes, biome);
    }

}
