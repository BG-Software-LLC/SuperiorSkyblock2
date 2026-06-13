package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.PreStackedSpawnerSpawnEvent;
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

import java.util.List;

public class SpawnersProvider_RoseStacker1_5 implements SpawnersProvider_AutoDetect {

    private static final ReflectMethod<EntityType> GET_STACKED_ITEM_ENTITY_TYPE =
            new ReflectMethod<>(StackerUtils.class, "getStackedItemEntityType", ItemStack.class);

    private final SuperiorSkyblockPlugin plugin;

    public SpawnersProvider_RoseStacker1_5(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new StackerListener(), plugin);
        Log.info("Using RoseStacker as a spawners provider.");
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
                Key blockKey = Keys.of(e.getStack().getBlock());

                int increaseAmount = e.getStack().getStackSize() + e.getIncreaseAmount() > e.getStack().getStackSettings().getMaxStackSize() ?
                        e.getStack().getStackSettings().getMaxStackSize() - e.getStack().getStackSize() : e.getIncreaseAmount();
                int newBlocksCount = e.isNew() ? Math.max(1, increaseAmount - 1) : increaseAmount;

                if (island.hasReachedBlockLimit(blockKey, newBlocksCount)) {
                    e.setCancelled(true);
                } else {
                    island.handleBlockPlace(blockKey, newBlocksCount);
                }
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

        // RoseStacker spawns mobs through a custom NMS path and, by default
        // (bypass-region-spawning-rules: true), does not fire a Bukkit CreatureSpawnEvent.
        // It also merges spawns directly into nearby stacks without spawning a new entity.
        // Both cases bypass SuperiorSkyblock's CreatureSpawnEvent-based spawner-spawning flag.
        // PreStackedSpawnerSpawnEvent is cancellable and gates every spawn path, so we enforce
        // the spawner-spawning island flag here instead.
        // Note: this event may be fired asynchronously (spawn-async: true), so we only read
        // thread-safe data (cached spawner tile, grid lookup, island settings).
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onStackedSpawnerSpawn(PreStackedSpawnerSpawnEvent e) {
            StackedSpawner stackedSpawner = e.getStack();
            Location location = stackedSpawner.getLocation();

            Island island = plugin.getGrid().getIslandAt(location);
            if (island == null)
                return;

            if (!plugin.getSettings().getSpawn().isProtected() && island.isSpawn())
                return;

            EntityType spawnedType = stackedSpawner.getSpawnerTile().getSpawnedType();
            if (spawnedType == null)
                return;

            List<EntityCategory> entityCategories = plugin.getSettings().getEntityCategoriesMap().getCategories(Keys.of(spawnedType));
            for (EntityCategory entityCategory : entityCategories) {
                IslandFlag islandFlag = entityCategory.getSpawnerSpawningIslandFlag();
                if (islandFlag != null && !island.hasSettingsEnabled(islandFlag)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

    }

}
