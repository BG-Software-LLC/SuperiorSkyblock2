package com.bgsoftware.superiorskyblock.nms.v1_16_R3.generator;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BiomeStorage;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.Registry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings({"unused", "NullableProblems"})
public class IslandsGeneratorImpl extends IslandsGenerator {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(
            BiomeStorage.class, BiomeBase[].class, "h");
    private static final ReflectField<Registry<BiomeBase>> BIOME_REGISTRY = new ReflectField<>(
            BiomeStorage.class, Registry.class, "registry", "g");
    private static final ReflectField<BiomeStorage> BIOME_STORAGE = new ReflectField<>(
            new ClassInfo("generator.CustomChunkGenerator$CustomBiomeGrid", ClassInfo.PackageType.CRAFTBUKKIT),
            BiomeStorage.class, "biome");

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
        BiomeStorage biomeStorage = BIOME_STORAGE.get(biomeGrid);
        BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(biomeStorage);

        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase((IRegistry<BiomeBase>) BIOME_REGISTRY.get(biomeStorage), biome);

        if (biomeBases == null)
            return;

        Arrays.fill(biomeBases, biomeBase);
    }

}
