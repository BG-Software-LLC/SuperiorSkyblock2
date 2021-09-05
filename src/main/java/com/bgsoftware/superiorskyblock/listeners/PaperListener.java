package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SpongeAbsorbEvent;

@SuppressWarnings("unused")
public final class PaperListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public PaperListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent e){
        if(plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
            e.setCancelled(true);
    }

}
