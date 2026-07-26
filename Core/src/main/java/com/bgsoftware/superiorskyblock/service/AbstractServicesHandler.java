package com.bgsoftware.superiorskyblock.service;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.service.bossbar.BossBarsServiceImpl;
import com.bgsoftware.superiorskyblock.service.dragon.DragonBattleServiceImpl;
import com.bgsoftware.superiorskyblock.service.hologram.HologramsServiceImpl;
import com.bgsoftware.superiorskyblock.service.message.MessagesServiceImpl;
import com.bgsoftware.superiorskyblock.service.portals.PortalsManagerServiceImpl;
import com.bgsoftware.superiorskyblock.service.region.RegionManagerServiceImpl;
import com.bgsoftware.superiorskyblock.service.stackedblocks.StackedBlocksInteractionServiceImpl;
import com.bgsoftware.superiorskyblock.service.world.WorldRecordServiceImpl;

import java.util.IdentityHashMap;
import java.util.Map;

public abstract class AbstractServicesHandler {

    protected final Map<Class<?>, IService> services = new IdentityHashMap<>();

    public <T> T getService(Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null)
            throw new RuntimeException("Tried to get service of invalid class: " + serviceClass);

        return serviceClass.cast(service);
    }

    public void loadDefaultServices(SuperiorSkyblockPlugin plugin) {
        registerService(new HologramsServiceImpl(plugin));
        registerService(new DragonBattleServiceImpl(plugin));
        registerService(new BossBarsServiceImpl(plugin));
        registerService(new MessagesServiceImpl(plugin));
        registerService(new PortalsManagerServiceImpl(plugin));
        registerService(new RegionManagerServiceImpl(plugin));
        registerService(new StackedBlocksInteractionServiceImpl(plugin));
        registerService(new WorldRecordServiceImpl(plugin));
    }

    protected abstract <T extends IService> void registerService(T serviceImpl);

}
