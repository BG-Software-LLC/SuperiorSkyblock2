package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddSpawnerRates;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetSpawnerRates;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpgradeTypeSpawnerRates implements IUpgradeType {

    private static final List<ISuperiorCommand> commands = Arrays.asList(new CmdAdminAddSpawnerRates(),
            new CmdAdminSetSpawnerRates());

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeSpawnerRates(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<Listener> getListeners() {
        return Collections.singletonList(new SpawnerRatesListener());
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return commands;
    }

    public void handleSpawnerPlace(Block block) {
        Location location = block.getLocation();
        Island island = plugin.getGrid().getIslandAt(location);

        if (island == null)
            return;

        // We want to replace the spawner in a delay so other plugins that might change the spawner will be taken in action as well.
        BukkitExecutor.sync(() -> {
            if (block.getType() == Materials.SPAWNER.toBukkitType())
                plugin.getNMSWorld().listenSpawner(location, spawnDelay -> calculateNewSpawnerDelay(island, spawnDelay));
        }, 20L);
    }

    private int calculateNewSpawnerDelay(Island island, int spawnDelay) {
        double spawnerRatesMultiplier = island.getSpawnerRatesMultiplier();
        if (spawnerRatesMultiplier > 1) {
            return (int) Math.round(spawnDelay / spawnerRatesMultiplier);
        } else {
            return spawnDelay;
        }
    }

    private class SpawnerRatesListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlace(BlockPlaceEvent e) {
            if (e.getBlock().getType() == Materials.SPAWNER.toBukkitType())
                handleSpawnerPlace(e.getBlock());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onChunkLoad(ChunkLoadEvent e) {
            List<Island> chunkIslands = plugin.getGrid().getIslandsAt(e.getChunk());
            chunkIslands.forEach(island -> handleIslandChunkLoad(island, e.getChunk()));
        }

        private void handleIslandChunkLoad(Island island, Chunk chunk) {
            List<Location> blockEntities = plugin.getNMSChunks().getBlockEntities(chunk);

            if (blockEntities.isEmpty())
                return;

            // We want to replace the spawner in a delay so other plugins that might change the spawner will be taken in action as well.
            // Block entities that are not spawners will not be touched.
            BukkitExecutor.sync(() -> {
                if (chunk.isLoaded()) {
                    blockEntities.forEach(blockEntity -> {
                        plugin.getNMSWorld().listenSpawner(blockEntity, spawnDelay -> calculateNewSpawnerDelay(island, spawnDelay));
                    });
                }
            }, 20L);
        }

    }

}
