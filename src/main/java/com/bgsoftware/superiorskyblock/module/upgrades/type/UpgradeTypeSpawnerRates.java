package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddSpawnerRates;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetSpawnerRates;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpgradeTypeSpawnerRates implements IUpgradeType {

    private static final List<ISuperiorCommand> commands = Arrays.asList(new CmdAdminAddSpawnerRates(),
            new CmdAdminSetSpawnerRates());

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeSpawnerRates(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Listener getListener() {
        return new SpawnerRatesListener();
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return commands;
    }

    public void handleSpawnerPlace(CreatureSpawner creatureSpawner) {
        Island island = plugin.getGrid().getIslandAt(creatureSpawner.getLocation());

        if (island == null)
            return;

        // We want to replace the spawner in a delay so other plugins that might change the spawner will be taken in action as well.
        BukkitExecutor.sync(() -> plugin.getNMSWorld().listenSpawner(creatureSpawner,
                spawnDelay -> calculateNewSpawnerDelay(island, spawnDelay)), 20L);
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
                handleSpawnerPlace((CreatureSpawner) e.getBlock().getState());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onChunkLoad(ChunkLoadEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getChunk());

            if (island == null)
                return;

            List<CreatureSpawner> creatureSpawners = new ArrayList<>();

            for (BlockState tileEntity : e.getChunk().getTileEntities()) {
                if (tileEntity instanceof CreatureSpawner) {
                    creatureSpawners.add((CreatureSpawner) tileEntity);
                }
            }

            if (!creatureSpawners.isEmpty()) {
                // We want to replace the spawner in a delay so other plugins that might change the spawner will be taken in action as well.
                BukkitExecutor.sync(() -> {
                    if (e.getChunk().isLoaded()) {
                        creatureSpawners.forEach(creatureSpawner -> {
                            plugin.getNMSWorld().listenSpawner(creatureSpawner, spawnDelay -> calculateNewSpawnerDelay(island, spawnDelay));
                        });
                    }
                }, 20L);
            }

        }

    }

}
