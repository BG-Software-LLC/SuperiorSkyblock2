package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddSpawnerRates;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetSpawnerRates;
import com.bgsoftware.superiorskyblock.structure.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.threads.Executor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class UpgradeTypeSpawnerRates implements IUpgradeType {

    private static final List<ISuperiorCommand> commands = Arrays.asList(new CmdAdminAddSpawnerRates(),
            new CmdAdminSetSpawnerRates());

    private final Collection<UUID> alreadyTrackedSpawning = AutoRemovalCollection.newHashSet(10L * 50, TimeUnit.MILLISECONDS);

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

    public void handleSpawnerSpawn(@Nullable CreatureSpawner creatureSpawner) {
        if (creatureSpawner == null || creatureSpawner.getLocation() == null)
            return;

        Island island = plugin.getGrid().getIslandAt(creatureSpawner.getLocation());

        if (island == null)
            return;

        double spawnerRatesMultiplier = island.getSpawnerRatesMultiplier();

        if (spawnerRatesMultiplier > 1 && alreadyTrackedSpawning.add(island.getOwner().getUniqueId())) {
            Executor.sync(() -> {
                int spawnDelay = plugin.getNMSWorld().getSpawnerDelay(creatureSpawner);
                if (spawnDelay > 0) {
                    plugin.getNMSWorld().setSpawnerDelay(creatureSpawner,
                            (int) Math.round(spawnDelay / spawnerRatesMultiplier));
                }
            }, 5L);
        }
    }

    private final class SpawnerRatesListener implements Listener {


        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawn(SpawnerSpawnEvent e) {
            handleSpawnerSpawn(e.getSpawner());
        }

    }

}
