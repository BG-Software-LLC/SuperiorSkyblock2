package com.bgsoftware.superiorskyblock.bukkit.service;

import com.bgsoftware.superiorskyblock.bukkit.SuperiorSkyblockBukkitPlugin;
import com.bgsoftware.superiorskyblock.service.BaseServicesHandler;
import com.bgsoftware.superiorskyblock.service.IService;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public class BukkitServicesHandler extends BaseServicesHandler {

    private final SuperiorSkyblockBukkitPlugin plugin;

    public BukkitServicesHandler(SuperiorSkyblockBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected <T extends IService> void registerService(T serviceImpl) {
        Class apiClass = serviceImpl.getAPIClass();

        Preconditions.checkArgument(!services.containsKey(apiClass), "Service for class " + apiClass + " already exists.");

        services.put(apiClass, serviceImpl);
        Bukkit.getServicesManager().register(apiClass, serviceImpl, plugin, ServicePriority.Normal);
    }

}
