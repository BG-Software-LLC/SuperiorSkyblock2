package com.bgsoftware.superiorskyblock.service;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleService;
import com.bgsoftware.superiorskyblock.api.service.hologram.HologramsService;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public final class ServicesHandler {

    private final SuperiorSkyblockPlugin plugin;

    private PlaceholdersService placeholdersService;
    private HologramsService hologramsService;
    private DragonBattleService dragonBattleService;

    public ServicesHandler(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerPlaceholdersService(PlaceholdersService placeholdersService) {
        this.placeholdersService = placeholdersService;
        Bukkit.getServicesManager().register(PlaceholdersService.class, placeholdersService, plugin, ServicePriority.Normal);
    }

    public void registerHologramsService(HologramsService hologramsService) {
        this.hologramsService = hologramsService;
        Bukkit.getServicesManager().register(HologramsService.class, hologramsService, plugin, ServicePriority.Normal);
    }

    public void registerEnderDragonService(DragonBattleService dragonBattleService) {
        this.dragonBattleService = dragonBattleService;
        Bukkit.getServicesManager().register(DragonBattleService.class, dragonBattleService, plugin, ServicePriority.Normal);
    }

    public PlaceholdersService getPlaceholdersService() {
        return placeholdersService;
    }

    public HologramsService getHologramsService() {
        return hologramsService;
    }

    public DragonBattleService getDragonBattleService() {
        return dragonBattleService;
    }

}
