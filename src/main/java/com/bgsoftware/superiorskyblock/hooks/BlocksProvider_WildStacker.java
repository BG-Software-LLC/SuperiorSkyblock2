package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.events.BarrelPlaceEvent;
import xyz.wildseries.wildstacker.api.events.BarrelStackEvent;
import xyz.wildseries.wildstacker.api.events.BarrelUnstackEvent;
import xyz.wildseries.wildstacker.api.events.SpawnerPlaceEvent;
import xyz.wildseries.wildstacker.api.events.SpawnerStackEvent;
import xyz.wildseries.wildstacker.api.events.SpawnerUnstackEvent;
import xyz.wildseries.wildstacker.api.objects.StackedBarrel;
import xyz.wildseries.wildstacker.api.objects.StackedObject;

@SuppressWarnings("unused")
public class BlocksProvider_WildStacker implements BlocksProvider, Listener {

    private static WildStackerPlugin stacker = WildStackerPlugin.getPlugin();
    private static SuperiorSkyblock plugin = SuperiorSkyblock.getPlugin();

    public BlocksProvider_WildStacker(){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public int getBlockCount(Location location) {
        StackedObject stackedObject = stacker.getDataHandler().CACHED_OBJECTS.get(location);
        return stackedObject == null ? 1 : Math.max(stackedObject.getStackAmount(), 1);
    }

    @Override
    public Key getBlockKey(Location location, Key def) {
        StackedObject stackedObject = stacker.getDataHandler().CACHED_OBJECTS.get(location);
        return !(stackedObject instanceof StackedBarrel) ? def : Key.of(((StackedBarrel) stackedObject).getBarrelItem(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBarrelPlace(BarrelPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
        if(island != null)
            island.handleBlockPlace(Key.of(e.getBarrel().getBarrelItem(1)), e.getBarrel().getStackAmount());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBarrelStack(BarrelStackEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
        if(island != null)
            island.handleBlockPlace(Key.of(e.getBarrel().getBarrelItem(1)), e.getTarget().getStackAmount());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBarrelUnstack(BarrelUnstackEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
        if(island != null)
            island.handleBlockBreak(Key.of(e.getBarrel().getBarrelItem(1)), e.getAmount());
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
