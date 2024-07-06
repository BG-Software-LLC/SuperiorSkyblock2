package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.io.FileClassLoader;
import com.bgsoftware.superiorskyblock.core.io.JarFiles;
import com.bgsoftware.superiorskyblock.core.io.loader.FilesLookup;
import com.bgsoftware.superiorskyblock.core.io.loader.FilesLookupFactory;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;

public class WorldGenerator {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final EnumerateMap<Dimension, IslandsGenerator> defaultWorldGenerators = new EnumerateMap<>(Dimension.values());
    @Nullable
    private static ChunkGenerator customWorldGenerator;
    private static boolean loadedCustomWorldGenerator = false;

    public static ChunkGenerator getWorldGenerator(Dimension dimension) {
        if (!loadedCustomWorldGenerator && customWorldGenerator == null)
            customWorldGenerator = loadGeneratorFromFile();

        if (customWorldGenerator != null)
            return customWorldGenerator;

        return defaultWorldGenerators.computeIfAbsent(dimension, d -> plugin.getNMSWorld().createGenerator(d));
    }

    @Nullable
    private static ChunkGenerator loadGeneratorFromFile() {
        if (loadedCustomWorldGenerator)
            return null;

        loadedCustomWorldGenerator = true;

        File generatorFolder = new File(plugin.getDataFolder(), "world-generator");

        if (!generatorFolder.isDirectory()) {
            generatorFolder.delete();
        }

        if (!generatorFolder.exists()) {
            generatorFolder.mkdirs();
            return null;
        }

        File[] generatorsFilesList = generatorFolder.listFiles();
        if (generatorsFilesList == null || generatorsFilesList.length == 0) {
            return null;
        }

        try (FilesLookup filesLookup = FilesLookupFactory.getInstance().lookupFolder(generatorFolder)) {
            for (File file : generatorsFilesList) {
                String fileName = file.getName();
                if (!fileName.endsWith(".jar"))
                    continue;

                file = filesLookup.getFile(fileName);

                FileClassLoader classLoader = new FileClassLoader(file, plugin.getPluginClassLoader());

                //noinspection deprecation
                Class<?> generatorClass = JarFiles.getClass(file.toURL(), ChunkGenerator.class, classLoader).getLeft();

                if (generatorClass != null) {
                    for (Constructor<?> constructor : generatorClass.getConstructors()) {
                        if (constructor.getParameterCount() == 0) {
                            return (ChunkGenerator) generatorClass.newInstance();
                        } else if (constructor.getParameterTypes()[0].equals(JavaPlugin.class) || constructor.getParameterTypes()[0].equals(SuperiorSkyblock.class)) {
                            return (ChunkGenerator) constructor.newInstance(plugin);
                        }
                    }
                }
            }
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while loading the generator:");
        }

        return null;
    }

    private WorldGenerator() {

    }

}
