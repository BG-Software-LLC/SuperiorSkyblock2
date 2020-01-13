package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.ProvidersManager;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_EpicSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_PvpingSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_SilkSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_WildStacker;
import com.bgsoftware.superiorskyblock.hooks.LeaderHeadsHook;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_MergedSpawner;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class ProvidersHandler implements ProvidersManager {

    private SpawnersProvider spawnersProvider;

    public ProvidersHandler(SuperiorSkyblockPlugin plugin){
        Executor.sync(() -> {
            if(Bukkit.getPluginManager().isPluginEnabled("LeaderHeads"))
                LeaderHeadsHook.register();

            String spawnersProvider = plugin.getSettings().spawnersProvider;

            if(Bukkit.getPluginManager().isPluginEnabled("MergedSpawner") &&
                    (spawnersProvider.equalsIgnoreCase("MergedSpawner") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                setSpawnersProvider(new BlocksProvider_MergedSpawner());
            }
            else if(Bukkit.getPluginManager().isPluginEnabled("WildStacker") &&
                    (spawnersProvider.equalsIgnoreCase("WildStacker") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                setSpawnersProvider(new BlocksProvider_WildStacker());
            }
            else if(Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                    Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("CandC_9_12") &&
                    (spawnersProvider.equalsIgnoreCase("SilkSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                setSpawnersProvider(new BlocksProvider_SilkSpawners());
            }
            else if(Bukkit.getPluginManager().isPluginEnabled("PvpingSpawners") &&
                    (spawnersProvider.equalsIgnoreCase("PvpingSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                setSpawnersProvider(new BlocksProvider_PvpingSpawners());
            }
            else if(Bukkit.getPluginManager().isPluginEnabled("EpicSpawners") &&
                    (spawnersProvider.equalsIgnoreCase("EpicSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                setSpawnersProvider(new BlocksProvider_EpicSpawners());
            }
            else {
                setSpawnersProvider(new BlocksProvider_Default());
            }
        });

        PlaceholderHook.register(plugin);
    }

    @Override
    public void setSpawnersProvider(SpawnersProvider spawnersProvider){
        if(spawnersProvider == null)
            throw new IllegalArgumentException("SpawnersProvider cannot be null.");

        this.spawnersProvider = spawnersProvider;
    }

    public Pair<Integer, EntityType> getSpawner(Location location){
        return spawnersProvider.getSpawner(location);
    }

    public Key getSpawnerKey(ItemStack itemStack){
        return spawnersProvider instanceof BlocksProvider ? ((BlocksProvider) spawnersProvider).getSpawnerKey(itemStack) : Key.of(itemStack);
    }

    public Pair<Integer, Material> getBlock(Location location){
        return spawnersProvider instanceof BlocksProvider ? ((BlocksProvider) spawnersProvider).getBlock(location) : null;
    }

}
