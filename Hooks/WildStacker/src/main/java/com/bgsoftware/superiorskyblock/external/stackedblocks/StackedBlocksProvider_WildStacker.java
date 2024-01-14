package com.bgsoftware.superiorskyblock.external.stackedblocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.hooks.StackedBlocksSnapshotProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.external.WildStackerSnapshotsContainer;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.stream.Collectors;

public class StackedBlocksProvider_WildStacker implements StackedBlocksProvider_AutoDetect, StackedBlocksSnapshotProvider {

    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;
    private final LazyReference<RegionManagerService> regionManagerService = new LazyReference<RegionManagerService>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };

    public StackedBlocksProvider_WildStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), plugin);
            registered = true;

            SuperiorSkyblockAPI.getBlockValues().registerKeyParser(new CustomKeyParser() {

                private final SystemManager systemManager = WildStackerAPI.getWildStacker().getSystemManager();

                @Override
                public Key getCustomKey(Location location) {
                    return systemManager.isStackedBarrel(location) ?
                            Key.of(systemManager.getStackedBarrel(location).getBarrelItem(1)) :
                            Key.ofMaterialAndData("CAULDRON");
                }

                @Override
                public boolean isCustomKey(Key key) {
                    return false;
                }

            }, ConstantKeys.CAULDRON);

            Log.info("Using WildStacker as a stacked-blocks provider.");
        }
    }

    @Override
    public Collection<Pair<Key, Integer>> getBlocks(World world, int chunkX, int chunkZ) {
        StackedSnapshot stackedSnapshot = WildStackerSnapshotsContainer.getSnapshot(ChunkPosition.of(world, chunkX, chunkZ));

        try {
            return stackedSnapshot.getAllBarrelsItems().values().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> new Pair<>(Key.of(entry.getValue()), entry.getKey()))
                    .collect(Collectors.toSet());
        } catch (Throwable ex) {
            return stackedSnapshot.getAllBarrels().values().stream()
                    .map(entry -> new Pair<>(Key.of(entry.getValue(), (short) 0), entry.getKey()))
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void takeSnapshot(Chunk chunk) {
        WildStackerSnapshotsContainer.takeSnapshot(chunk);
    }

    @Override
    public void releaseSnapshot(World world, int chunkX, int chunkZ) {
        WildStackerSnapshotsContainer.releaseSnapshot(ChunkPosition.of(world, chunkX, chunkZ));
    }

    @SuppressWarnings("unused")
    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelPlace(BarrelPlaceEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            if (island == null)
                return;

            Key blockKey = Key.of(e.getBarrel().getBarrelItem(1));
            int increaseAmount = e.getBarrel().getStackAmount();

            if (island.hasReachedBlockLimit(blockKey, increaseAmount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            } else {
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelStack(BarrelStackEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            if (island == null)
                return;

            Key blockKey = Key.of(e.getTarget().getBarrelItem(1));
            int increaseAmount = e.getTarget().getStackAmount();

            if (island.hasReachedBlockLimit(blockKey, increaseAmount)) {
                e.setCancelled(true);
            } else {
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBarrelUnstackOnOtherIsland(BarrelUnstackEvent e) {
            Entity unstackSource = e.getUnstackSource();

            if (!(unstackSource instanceof Player))
                return;

            Player player = (Player) unstackSource;

            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if (island == null)
                return;

            if (!island.hasPermission(player, IslandPrivileges.BREAK)) {
                e.setCancelled(true);
                Message.PROTECTION.send(player);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelUnstack(BarrelUnstackEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if (island != null)
                island.handleBlockBreak(Key.of(e.getBarrel().getBarrelItem(1)), e.getAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelPlace(BarrelPlaceInventoryEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            if (island == null)
                return;

            Key blockKey = Key.of(e.getBarrel().getBarrelItem(1));
            int increaseAmount = e.getIncreaseAmount();

            if (island.hasReachedBlockLimit(blockKey, increaseAmount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            } else {
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

    }

}