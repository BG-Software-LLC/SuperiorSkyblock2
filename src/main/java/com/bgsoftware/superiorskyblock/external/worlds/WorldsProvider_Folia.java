package com.bgsoftware.superiorskyblock.external.worlds;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import com.bgsoftware.superiorskyblock.world.WorldGenerator;

import java.util.concurrent.CompletableFuture;

public class WorldsProvider_Folia extends WorldsProvider_Default {

    private final SuperiorSkyblockPlugin plugin;
    private volatile boolean prepared;

    public WorldsProvider_Folia(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void prepareWorlds() {
        if (!this.prepared)
            throw new IllegalStateException("Folia worlds must be prepared asynchronously");
    }

    @Override
    public CompletableFuture<Void> prepareWorldsAsync() {
        if (this.prepared)
            return CompletableFuture.completedFuture(null);
        Difficulty difficulty = Difficulty.valueOf(this.plugin.getSettings().getWorlds().getDifficulty());
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (Dimension dimension : Dimension.values()) {
            SettingsManager.Worlds.DimensionConfig config = this.plugin.getSettings().getWorlds().getDimensionConfig(dimension);
            if (config == null || !config.isEnabled())
                continue;
            future = future.thenCompose(ignored -> loadWorld(config.getName(), dimension)
                    .thenAccept(world -> registerWorld(world, difficulty, dimension)));
        }
        return future.thenRun(() -> this.prepared = true);
    }

    private CompletableFuture<World> loadWorld(String name, Dimension dimension) {
        World existing = Bukkit.getWorld(name);
        if (existing != null)
            return CompletableFuture.completedFuture(existing);
        return this.plugin.getNMSWorld().createWorldAsync(name, dimension.getEnvironment(),
                WorldGenerator.getWorldGenerator(dimension));
    }
}
