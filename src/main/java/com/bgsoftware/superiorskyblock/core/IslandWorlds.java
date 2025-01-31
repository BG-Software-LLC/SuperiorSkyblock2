package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.LazyWorldsProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.World;

import java.util.function.Consumer;

public class IslandWorlds {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void accessIslandWorldAsync(Island island, Dimension dimension, Consumer<Either<World, Throwable>> consumer) {
        WorldsProvider worldsProvider = plugin.getProviders().getWorldsProvider();
        if (worldsProvider instanceof LazyWorldsProvider) {
            ((LazyWorldsProvider) worldsProvider).prepareWorld(island, dimension, () ->
                    loadedWorldCallback(island, dimension, consumer));
        } else {
            loadedWorldCallback(island, dimension, consumer);
        }
    }

    private static void loadedWorldCallback(Island island, Dimension dimension, Consumer<Either<World, Throwable>> consumer) {
        World world = plugin.getGrid().getIslandsWorld(island, dimension);
        if (world != null) {
            consumer.accept(Either.left(world));
        } else {
            consumer.accept(Either.right(new NullPointerException("World does not exist")));
        }
    }

    private IslandWorlds() {

    }

}
