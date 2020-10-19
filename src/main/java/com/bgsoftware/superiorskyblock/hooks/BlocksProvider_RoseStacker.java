package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.event.SpawnerUnstackEvent;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public final class BlocksProvider_RoseStacker implements BlocksProvider {

    private static boolean registered = false;

    public BlocksProvider_RoseStacker(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new BlocksProvider_RoseStacker.StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;
            SuperiorSkyblockPlugin.log("Using RoseStacker as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        int blockCount = -1;
        if(Bukkit.isPrimaryThread()){
            StackedSpawner stackedSpawner = RoseStackerAPI.getInstance().getStackedSpawner(location.getBlock());
            blockCount = stackedSpawner == null ? 1 : stackedSpawner.getStackSize();
        }
        return new Pair<>(blockCount, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        return StackerUtils.getStackedItemEntityType(itemStack).name();
    }

    public static boolean isRegistered(){
        return registered;
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerStackEvent e){
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if(island != null) {
                Key spawnerKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getStack().getSpawner().getSpawnedType());
                island.handleBlockPlace(spawnerKey, e.getIncreaseAmount());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerUnstackEvent e){
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if(island != null) {
                Key spawnerKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getStack().getSpawner().getSpawnedType());
                island.handleBlockBreak(spawnerKey, e.getDecreaseAmount());
            }
        }

    }

}
