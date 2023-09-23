package com.bgsoftware.superiorskyblock.service;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.service.bossbar.BossBarsServiceImpl;
import com.bgsoftware.superiorskyblock.service.dragon.DragonBattleServiceImpl;
import com.bgsoftware.superiorskyblock.service.hologram.HologramsServiceImpl;
import com.bgsoftware.superiorskyblock.service.message.MessagesServiceImpl;
import com.bgsoftware.superiorskyblock.service.placeholders.PlaceholdersServiceImpl;
import com.bgsoftware.superiorskyblock.service.portals.PortalsManagerServiceImpl;
import com.bgsoftware.superiorskyblock.service.region.RegionManagerServiceImpl;
import com.bgsoftware.superiorskyblock.service.stackedblocks.StackedBlocksInteractionServiceImpl;
import com.bgsoftware.superiorskyblock.service.world.WorldRecordServiceImpl;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import java.util.IdentityHashMap;
import java.util.Map;

public class ServicesHandler {

    private final Map<Class<?>, IService> services = new IdentityHashMap<>();

    private final SuperiorSkyblockPlugin plugin;

    public ServicesHandler(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public <T> T getService(Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null)
            throw new RuntimeException("Tried to get service of invalid class: " + serviceClass);

        return serviceClass.cast(service);
    }

    public void loadDefaultServices(SuperiorSkyblockPlugin plugin) {
        registerService(new PlaceholdersServiceImpl());
        registerService(new HologramsServiceImpl(plugin));
        registerService(new DragonBattleServiceImpl(plugin));
        registerService(new BossBarsServiceImpl(plugin));
        registerService(new MessagesServiceImpl());
        registerService(new PortalsManagerServiceImpl(plugin));
        registerService(new RegionManagerServiceImpl(plugin));
        registerService(new StackedBlocksInteractionServiceImpl(plugin));
        registerService(new WorldRecordServiceImpl(plugin));
    }

    private <T extends IService> void registerService(T serviceImpl) {
        Class apiClass = serviceImpl.getAPIClass();

        Preconditions.checkArgument(!services.containsKey(apiClass), "Service for class " + apiClass + " already exists.");

        services.put(apiClass, serviceImpl);
        Bukkit.getServicesManager().register(apiClass, serviceImpl, plugin, ServicePriority.Normal);
    }

}
