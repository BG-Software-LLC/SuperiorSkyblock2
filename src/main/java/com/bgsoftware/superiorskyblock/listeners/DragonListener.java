package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;

public final class DragonListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public DragonListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnderDragonChangePhase(EnderDragonChangePhaseEvent e){
        Executor.sync(() -> plugin.getNMSDragonFight().setDragonPhase(e.getEntity(), e.getNewPhase()), 1L);
    }

}
