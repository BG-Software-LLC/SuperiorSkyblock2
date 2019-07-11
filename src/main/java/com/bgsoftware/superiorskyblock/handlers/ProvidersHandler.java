package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_EpicSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_PvpingSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_SilkSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_WildStacker;
import com.bgsoftware.superiorskyblock.hooks.FAWEHook;
import com.bgsoftware.superiorskyblock.hooks.LeaderHeadsHook;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_MergedSpawner;
import com.bgsoftware.superiorskyblock.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public final class ProvidersHandler {

    private BlocksProvider spawnersProvider;

    public ProvidersHandler(SuperiorSkyblockPlugin plugin){
        if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit"))
            FAWEHook.register();
        if(Bukkit.getPluginManager().isPluginEnabled("LeaderHeads"))
            LeaderHeadsHook.register();

        String spawnersProvider = plugin.getSettings().spawnersProvider;

        if(Bukkit.getPluginManager().isPluginEnabled("MergedSpawner") &&
                (spawnersProvider.equalsIgnoreCase("MergedSpawner") || spawnersProvider.equalsIgnoreCase("Auto")))
            this.spawnersProvider = new BlocksProvider_MergedSpawner();
        else if(Bukkit.getPluginManager().isPluginEnabled("WildStacker") &&
                (spawnersProvider.equalsIgnoreCase("WildStacker") || spawnersProvider.equalsIgnoreCase("Auto")))
            this.spawnersProvider = new BlocksProvider_WildStacker();
        else if(Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("CandC_9_12") &&
                (spawnersProvider.equalsIgnoreCase("SilkSpawners") || spawnersProvider.equalsIgnoreCase("Auto")))
            this.spawnersProvider = new BlocksProvider_SilkSpawners();
        else if(Bukkit.getPluginManager().isPluginEnabled("PvpingSpawners") &&
                (spawnersProvider.equalsIgnoreCase("PvpingSpawners") || spawnersProvider.equalsIgnoreCase("Auto")))
            this.spawnersProvider = new BlocksProvider_PvpingSpawners();
        else if(Bukkit.getPluginManager().isPluginEnabled("EpicSpawners") &&
                (spawnersProvider.equalsIgnoreCase("EpicSpawners") || spawnersProvider.equalsIgnoreCase("Auto")))
            this.spawnersProvider = new BlocksProvider_EpicSpawners();
        else this.spawnersProvider = new BlocksProvider_Default();

        PlaceholderHook.register(plugin);
    }

    public Pair<Integer, EntityType> getSpawner(Location location){
        return spawnersProvider.getSpawner(location);
    }

    public Pair<Integer, Material> getBlock(Location location){
        return spawnersProvider.getBlock(location);
    }

}
