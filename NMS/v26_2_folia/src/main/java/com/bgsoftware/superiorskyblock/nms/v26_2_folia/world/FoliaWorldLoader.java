/*
 * Derived from CraftBukkit/Paper CraftServer#createWorld (26.2).
 * Copyright (C) Bukkit, Spigot and Paper contributors.
 * Licensed under the GNU General Public License, version 3.
 */
package com.bgsoftware.superiorskyblock.nms.v26_2_folia.world;

import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.JsonOps;
import io.papermc.paper.world.PaperWorldLoader;
import io.papermc.paper.world.migration.WorldFolderMigration;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class FoliaWorldLoader {

    private FoliaWorldLoader() {
    }

    public static CompletableFuture<World> create(String name, World.Environment environment, ChunkGenerator generator) {
        return BukkitExecutor.submit(() -> create(WorldCreator.name(name).environment(environment)
                .type(WorldType.FLAT).generator(generator)));
    }

    private static World create(WorldCreator creator) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        var console = server.getServer();
        Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");

        String name = creator.name();
        ChunkGenerator chunkGenerator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();

        World world = server.getWorld(name);
        World worldByKey = server.getWorld(creator.key());
        if (world != null || worldByKey != null) {
            if (world == worldByKey) {
                return world;
            }
            throw new IllegalArgumentException("Cannot create a world with key " + creator.key() + " and name " + name + " one (or both) already match a world that exists");
        }

        if (chunkGenerator == null) {
            chunkGenerator = server.getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = server.getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        };

        RegistryAccess registryAccess = console.registryAccess();
        final ResourceKey<net.minecraft.world.level.Level> dimensionKey = CraftNamespacedKey.toResourceKey(Registries.DIMENSION, creator.key());
        net.minecraft.core.Registry<LevelStem> levelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        final LevelStem configuredStem = levelStemRegistry.getValue(actualDimension);
        if (configuredStem == null) {
            throw new IllegalStateException("Missing configured level stem " + actualDimension);
        }
        try {
            WorldFolderMigration.migrateApiWorld(
                console.storageSource,
                registryAccess,
                name,
                actualDimension,
                dimensionKey
            );
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to migrate legacy world " + name, ex);
        }
        PaperWorldLoader.LoadedWorldData loadedWorldData = PaperWorldLoader.loadWorldData(
            console,
            dimensionKey,
            name
        );
        final PrimaryLevelData primaryLevelData = (PrimaryLevelData) console.getWorldData();
        WorldGenSettings worldGenSettings = LevelStorageSource.readExistingSavedData(console.storageSource, dimensionKey, registryAccess, WorldGenSettings.TYPE)
            .result()
            .orElse(null);
        RegistryAccess contextRegistryAccess = registryAccess;
        if (worldGenSettings == null) {
            WorldOptions worldOptions = new WorldOptions(creator.seed(), creator.generateStructures(), creator.bonusChest());

            String flatGenSettings = creator.generatorSettings();
            if (flatGenSettings.isEmpty()) {
                flatGenSettings = FlatLevelGeneratorSettings.CODEC.encodeStart(registryAccess.createSerializationContext(JsonOps.INSTANCE), FlatLevelGeneratorSettings.getDefault(
                    registryAccess.lookupOrThrow(Registries.BIOME),
                    registryAccess.lookupOrThrow(Registries.STRUCTURE_SET),
                    registryAccess.lookupOrThrow(Registries.PLACED_FEATURE)
                )).getOrThrow().toString();
            }
            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse(flatGenSettings), creator.type().name().toLowerCase(Locale.ROOT));
            WorldDimensions worldDimensions = properties.create(registryAccess);

            WorldDimensions.Complete complete = worldDimensions.bake(levelStemRegistry);
            if (complete.dimensions().getValue(actualDimension) == null) {
                throw new IllegalStateException("Missing generated level stem " + actualDimension + " for world " + name);
            }

            worldGenSettings = new WorldGenSettings(worldOptions, worldDimensions);
            contextRegistryAccess = complete.dimensionsRegistryAccess();
            loadedWorldData.levelOverrides().setHardcore(creator.hardcore());
            loadedWorldData = new PaperWorldLoader.LoadedWorldData(
                loadedWorldData.bukkitName(),
                loadedWorldData.uuid(),
                loadedWorldData.pdc(),
                loadedWorldData.levelOverrides()
            );
        }
        final WorldGenSettings genSettingsFinal = worldGenSettings;

        levelStemRegistry = contextRegistryAccess.lookupOrThrow(Registries.LEVEL_STEM);

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(console.storageSource, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, contextRegistryAccess, console.options.has("recreateRegionFiles"));
        }

        long biomeZoomSeed = BiomeManager.obfuscateSeed(genSettingsFinal.options().seed());
        LevelStem customStem = genSettingsFinal.dimensions().get(actualDimension).orElse(null);
        if (customStem == null) {
            customStem = levelStemRegistry.getValue(actualDimension);
        }
        if (customStem == null) {
            throw new IllegalStateException("Missing level stem for world " + name + " using key " + actualDimension);
        }

        WorldInfo worldInfo = new CraftWorldInfo(loadedWorldData.bukkitName(), CraftNamespacedKey.fromMinecraft(dimensionKey.identifier()), genSettingsFinal.options().seed(), primaryLevelData.enabledFeatures(), creator.environment(), customStem.type().value(), customStem.generator(), registryAccess, loadedWorldData.uuid());
        if (biomeProvider == null && chunkGenerator != null) {
            biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo);
        }

        final SavedDataStorage savedDataStorage = new SavedDataStorage(console.storageSource.getDimensionPath(dimensionKey).resolve(LevelResource.DATA.id()), console.getFixerUpper(), registryAccess);
        savedDataStorage.set(WorldGenSettings.TYPE, new WorldGenSettings(genSettingsFinal.options(), genSettingsFinal.dimensions()));
        List<CustomSpawner> list = ImmutableList.of(
            new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(savedDataStorage)
        );

        ServerLevel serverLevel = new ServerLevel(
            console,
            Util.backgroundExecutor(),
            console.storageSource,
            genSettingsFinal,
            dimensionKey,
            customStem,
            primaryLevelData.isDebugWorld(),
            biomeZoomSeed,
            creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(),
            true,
            actualDimension,
            creator.environment(),
            chunkGenerator,
            biomeProvider,
            savedDataStorage,
            loadedWorldData
        );

        if (server.getWorld(name) == null)
            throw new IllegalStateException("World registration failed for " + name);

        console.addLevel(serverLevel);
        console.initWorld(serverLevel, creator);

        serverLevel.setSpawnSettings(true);

        console.prepareLevel(serverLevel);

        return serverLevel.getWorld();
    }
}
