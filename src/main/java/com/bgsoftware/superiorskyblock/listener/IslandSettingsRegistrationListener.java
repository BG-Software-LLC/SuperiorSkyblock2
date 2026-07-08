package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandFlagRegisterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandPrivilegeRegisterEvent;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.service.region.RegionManagerServiceImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IslandSettingsRegistrationListener implements Listener {

    public IslandSettingsRegistrationListener(SuperiorSkyblockPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onIslandFlagRegister(IslandFlagRegisterEvent event) {
        SIsland.onIslandFlagRegister(event.getIslandFlag());
        SpawnIsland.onIslandFlagRegister(event.getIslandFlag());
    }

    @EventHandler
    public void onIslandPrivilegeRegister(IslandPrivilegeRegisterEvent event) {
        SpawnIsland.onIslandPrivilegeRegister(event.getIslandPrivilege());
        RegionManagerServiceImpl.onIslandPrivilegeRegister(event.getIslandPrivilege());
    }

}
