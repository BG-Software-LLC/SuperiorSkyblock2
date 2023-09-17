package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.event.SpawnerUnstackEvent;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class SpawnersProvider_RoseStacker implements SpawnersProvider_AutoDetect {

    private static final ReflectMethod<EntityType> GET_STACKED_ITEM_ENTITY_TYPE =
            new ReflectMethod<>(StackerUtils.class, "getStackedItemEntityType", ItemStack.class);
    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;

    public SpawnersProvider_RoseStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), plugin);
            registered = true;
            Log.info("Using RoseStacker as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        int blockCount = -1;

        if (Bukkit.isPrimaryThread()) {
            StackedSpawner stackedSpawner = RoseStackerAPI.getInstance().getStackedSpawner(location.getBlock());
            blockCount = stackedSpawner == null ? 1 : stackedSpawner.getStackSize();
        }

        return new Pair<>(blockCount, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        EntityType entityType = GET_STACKED_ITEM_ENTITY_TYPE.isValid() ?
                GET_STACKED_ITEM_ENTITY_TYPE.invoke(null, itemStack) : ItemUtils.getStackedItemEntityType(itemStack);
        return entityType == null ? null : entityType.name();
    }

    @SuppressWarnings("unused")
    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerStackEvent e) {
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null) {
                island.handleBlockPlace(Keys.of(e.getStack().getBlock()), e.getIncreaseAmount());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerUnstackEvent e) {
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null) {
                island.handleBlockBreak(Keys.of(e.getStack().getBlock()), e.getDecreaseAmount());
            }
        }

    }

}
