package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.LazyWorldsProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.function.Consumer;

public class IslandWorlds {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void accessIslandWorldsAsync(Island island, boolean loadWorld, Consumer<Either<World, Throwable>> consumer) {
        for (Dimension dimension : Dimension.values()) {
            if (plugin.getProviders().getWorldsProvider().isDimensionEnabled(dimension) && island.wasSchematicGenerated(dimension)) {
                accessIslandWorldAsync(island, dimension, loadWorld, consumer);
            }
        }
    }

    public static void accessIslandWorldAsync(Island island, Dimension dimension, boolean loadWorld, Consumer<Either<World, Throwable>> consumer) {
        WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, dimension);
        if (worldInfo == null) {
            consumer.accept(Either.right(new NullPointerException("Cannot find world for dimension " + dimension.getName())));
            return;
        }

        World world = Bukkit.getWorld(worldInfo.getName());
        if (world != null) {
            consumer.accept(Either.left(world));
            return;
        }

        if (!loadWorld) {
            consumer.accept(Either.right(new NullPointerException("World is not loaded for dimension " + dimension.getName())));
            return;
        }

        WorldsProvider worldsProvider = plugin.getProviders().getWorldsProvider();
        if (worldsProvider instanceof LazyWorldsProvider) {
            ((LazyWorldsProvider) worldsProvider).prepareWorld(island, dimension, () ->
                    loadedWorldCallback(worldInfo, consumer));
        } else {
            loadedWorldCallback(worldInfo, consumer);
        }
    }

    public static Location setWorldToLocation(Island island, Dimension dimension, Location location) {
        WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, dimension);
        World world = Bukkit.getWorld(worldInfo.getName());
        if (world != null) {
            location = location.clone();
            location.setWorld(world);
        } else {
            WorldsProvider worldsProvider = plugin.getProviders().getWorldsProvider();
            if (worldsProvider instanceof LazyWorldsProvider) {
                location = new LazyWorldLocation(worldInfo.getName(), location.getX(), location.getY(),
                        location.getZ(), location.getYaw(), location.getPitch());
            } else {
                location = location.clone();
                world = plugin.getGrid().getIslandsWorld(island, dimension);
                location.setWorld(world);
            }
        }

        return location;
    }

    private static void loadedWorldCallback(WorldInfo worldInfo, Consumer<Either<World, Throwable>> consumer) {
        World world = Bukkit.getWorld(worldInfo.getName());
        if (world != null) {
            consumer.accept(Either.left(world));
        } else {
            consumer.accept(Either.right(new NullPointerException("World does not exist")));
        }
    }

    private IslandWorlds() {

    }

}
