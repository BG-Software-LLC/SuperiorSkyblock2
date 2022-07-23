package com.bgsoftware.superiorskyblock.module.upgrades.listeners;

import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeSpawnerRates;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;

public class WildStackerListener implements Listener {

    @Nullable
    private final UpgradeTypeSpawnerRates spawnerRates = BuiltinModules.UPGRADES
            .getEnabledUpgradeType(UpgradeTypeSpawnerRates.class);

    @EventHandler
    public void onWildStackerStackSpawn(SpawnerPlaceEvent e) {
        if (spawnerRates != null)
            spawnerRates.handleSpawnerPlace(e.getSpawner().getSpawner());
    }

}
