package com.bgsoftware.superiorskyblock.module.upgrades.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class WildStackerListener implements Listener {

    @EventHandler
    public void onWildStackerStackSpawn(com.bgsoftware.wildstacker.api.events.SpawnerStackedEntitySpawnEvent e) {
        UpgradesListener.IMP.handleSpawnerSpawn(e.getSpawner());
    }

}
