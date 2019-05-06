package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class BlocksProvider_WildStacker implements BlocksProvider, Listener {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public BlocksProvider_WildStacker(){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public int getBlockCount(Location location) {
        StackedObject stackedObject = WildStackerAPI.getWildStacker().getSystemManager().getStackedBarrel(location);
        return stackedObject == null ? 1 : Math.max(stackedObject.getStackAmount(), 1);
    }

    @Override
    public Key getBlockKey(Location location, Key def) {
        StackedObject stackedObject = WildStackerAPI.getWildStacker().getSystemManager().getStackedBarrel(location);
        return SKey.of(((StackedBarrel) stackedObject).getBarrelItem(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBarrelPlace(BarrelPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
        if(island != null)
            island.handleBlockPlace(SKey.of(e.getBarrel().getBarrelItem(1)), e.getBarrel().getStackAmount());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBarrelStack(BarrelStackEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
        if(island != null)
            island.handleBlockPlace(SKey.of(e.getBarrel().getBarrelItem(1)), e.getTarget().getStackAmount());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBarrelUnstack(BarrelUnstackEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
        if(island != null)
            island.handleBlockBreak(SKey.of(e.getBarrel().getBarrelItem(1)), e.getAmount());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerPlace(SpawnerPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
        if(island != null)
            island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), e.getSpawner().getStackAmount() - 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerStack(SpawnerStackEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
        if(island != null)
            island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), e.getTarget().getStackAmount());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerUnstack(SpawnerUnstackEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
        if(island != null)
            island.handleBlockBreak(e.getSpawner().getLocation().getBlock(), e.getAmount());
    }

}
