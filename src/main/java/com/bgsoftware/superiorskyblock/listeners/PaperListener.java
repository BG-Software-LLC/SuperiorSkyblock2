package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.structure.AutoRemovalCollection;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SpongeAbsorbEvent;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public final class PaperListener implements Listener {

    private final Collection<Location> alreadySpongeAbosrbCalled = AutoRemovalCollection.newArrayList(5L * 50, TimeUnit.MILLISECONDS);
    private final SuperiorSkyblockPlugin plugin;

    public PaperListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent e) {
        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpongeAbsorbMonitor(SpongeAbsorbEvent e) {
        Location location = e.getBlock().getLocation();

        if (alreadySpongeAbosrbCalled.contains(location))
            return;

        Island island = plugin.getGrid().getIslandAt(location);

        if (island == null)
            return;

        island.handleBlockBreak(e.getBlock(), 1);
        island.handleBlockPlace(ConstantKeys.WET_SPONGE, 1);

        alreadySpongeAbosrbCalled.add(location);
    }

}
