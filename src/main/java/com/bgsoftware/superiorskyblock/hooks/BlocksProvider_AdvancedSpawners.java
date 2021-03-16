package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.google.common.base.Preconditions;
import gcspawners.ASAPI;
import gcspawners.AdvancedSpawnerPlaceEvent;
import gcspawners.AdvancedSpawnersBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public final class BlocksProvider_AdvancedSpawners implements BlocksProvider {

    private static boolean registered = false;

    public BlocksProvider_AdvancedSpawners(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new BlocksProvider_AdvancedSpawners.StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;
            SuperiorSkyblockPlugin.log("Using AdvancedSpawners as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        return !Bukkit.isPrimaryThread() ? new Pair<>(-1, null) :
                new Pair<>(ASAPI.getSpawnerAmount(location), ASAPI.getSpawnerType(location).toUpperCase());
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        return ASAPI.getSpawnerType(itemStack).toUpperCase();
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(AdvancedSpawnerPlaceEvent e){
            Location location = e.getSpawner().getLocation();

            Island island = plugin.getGrid().getIslandAt(location);

            if(island != null)
                island.handleBlockPlace(Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getEntityType().toUpperCase()), e.getCountPlaced());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(AdvancedSpawnersBreakEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockBreak(Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getEntityType().toUpperCase()), e.getCountBroken());
        }

    }

}
