package com.bgsoftware.superiorskyblock.nms.v1_12_R1.generator;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.server.v1_12_R1.BiomeBase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class IslandsGeneratorImpl extends IslandsGenerator {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(
            new ClassInfo("generator.CustomChunkGenerator$CustomBiomeGrid", ClassInfo.PackageType.CRAFTBUKKIT),
            BiomeBase[].class, "biome");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Dimension dimension;

    public IslandsGeneratorImpl(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
        ChunkData chunkData = createChunkData(world);

        Biome targetBiome = IslandUtils.getDefaultWorldBiome(this.dimension);

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

    private static void setBiome(ChunkGenerator.BiomeGrid biomeGrid, Biome biome) {
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);

        BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(biomeGrid);

        if (biomeBases == null)
            return;

        Arrays.fill(biomeBases, biomeBase);
    }

}
