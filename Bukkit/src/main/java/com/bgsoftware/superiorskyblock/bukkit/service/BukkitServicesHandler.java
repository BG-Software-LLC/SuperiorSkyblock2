package com.bgsoftware.superiorskyblock.bukkit.service;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.bukkit.SuperiorSkyblockBukkitPlugin;
import com.bgsoftware.superiorskyblock.bukkit.service.placeholders.BukkitPlaceholdersService;
import com.bgsoftware.superiorskyblock.service.AbstractServicesHandler;
import com.bgsoftware.superiorskyblock.service.IService;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public class BukkitServicesHandler extends AbstractServicesHandler {

    private final SuperiorSkyblockBukkitPlugin plugin;

    public BukkitServicesHandler(SuperiorSkyblockBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void loadDefaultServices(SuperiorSkyblockPlugin plugin) {
        super.loadDefaultServices(plugin);
        registerService(new BukkitPlaceholdersService());
    }

    @Override
    protected <T extends IService> void registerService(T serviceImpl) {
        Class apiClass = serviceImpl.getAPIClass();

        Preconditions.checkArgument(!services.containsKey(apiClass), "Service for class " + apiClass + " already exists.");

        services.put(apiClass, serviceImpl);
        Bukkit.getServicesManager().register(apiClass, serviceImpl, plugin, ServicePriority.Normal);
    }

}
