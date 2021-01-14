package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@SuppressWarnings("unused")
public final class TutorialListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public TutorialListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e){
        if(!e.getPlayer().isOp() || !plugin.isTutorialRunning())
            return;

        Executor.sync(() -> Bukkit.dispatchCommand(e.getPlayer(), "sbt 1"), 60L);
    }

}
