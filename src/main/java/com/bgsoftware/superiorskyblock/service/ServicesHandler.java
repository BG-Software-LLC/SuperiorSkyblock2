package com.bgsoftware.superiorskyblock.service;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public final class ServicesHandler {

    private final SuperiorSkyblockPlugin plugin;

    private PlaceholdersService placeholdersService;

    public ServicesHandler(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerPlaceholdersService(PlaceholdersService placeholdersService) {
        this.placeholdersService = placeholdersService;
        Bukkit.getServicesManager().register(PlaceholdersService.class, placeholdersService, plugin, ServicePriority.Normal);
    }

    public PlaceholdersService getPlaceholdersService() {
        return placeholdersService;
    }

}
