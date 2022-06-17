package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class IslandWorldEventsListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public IslandWorldEventsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockRedstone(BlockRedstoneEvent e) {
        if (!plugin.getSettings().isDisableRedstoneOffline() && !plugin.getSettings().getAFKIntegrations().isDisableRedstone())
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if (island == null || island.isSpawn())
            return;

        if ((plugin.getSettings().isDisableRedstoneOffline() && island.getLastTimeUpdate() != -1) ||
                (plugin.getSettings().getAFKIntegrations().isDisableRedstone() &&
                        island.getAllPlayersInside().stream().allMatch(SuperiorPlayer::isAFK))) {
            e.setNewCurrent(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntitySpawn(CreatureSpawnEvent e) {
        if (!plugin.getSettings().getAFKIntegrations().isDisableSpawning() ||
                plugin.getServices().getHologramsService().isHologram(e.getEntity()))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island == null || island.isSpawn() || !island.getAllPlayersInside().stream().allMatch(SuperiorPlayer::isAFK))
            return;

        e.setCancelled(true);
    }

}
