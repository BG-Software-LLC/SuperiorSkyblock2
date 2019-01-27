package com.ome_r.superiorskyblock.hooks;

import com.ome_r.superiorskyblock.utils.key.Key;
import org.bukkit.Location;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedBarrel;
import xyz.wildseries.wildstacker.api.objects.StackedObject;

public class BlocksProvider_WildStacker implements BlocksProvider{

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    @Override
    public int getBlockCount(Location location) {
        StackedObject stackedObject = plugin.getDataHandler().CACHED_OBJECTS.get(location);
        return stackedObject == null ? 1 : Math.max(stackedObject.getStackAmount(), 1);
    }

    @Override
    public Key getBlockKey(Location location, Key def) {
        StackedObject stackedObject = plugin.getDataHandler().CACHED_OBJECTS.get(location);
        return !(stackedObject instanceof StackedBarrel) ? def : Key.of(((StackedBarrel) stackedObject).getBarrelItem(1));
    }
}
