package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import skyblock.hassan.plugin.Main;
import skyblock.hassan.plugin.spawners.StackedSpawner;

public final class BlocksProvider_PvpingSpawners implements BlocksProvider{

    private Main main;

    public BlocksProvider_PvpingSpawners(){
        main = (Main) Bukkit.getPluginManager().getPlugin("PvpingSpawners");
    }

    @Override
    public Pair<Integer, EntityType> getSpawner(Location location) {
        int blockCount = -1;
        if(Bukkit.isPrimaryThread()){
            StackedSpawner stackedSpawner = main.getProps().getStackedSpawner(main, (CreatureSpawner) location.getBlock().getState());
            blockCount = stackedSpawner.getSize();
        }
        return new Pair<>(blockCount, null);
    }

}
