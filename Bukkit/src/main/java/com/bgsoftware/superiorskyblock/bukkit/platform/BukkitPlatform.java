package com.bgsoftware.superiorskyblock.bukkit.platform;

import com.bgsoftware.superiorskyblock.bukkit.SuperiorSkyblockBukkitPlugin;
import com.bgsoftware.superiorskyblock.bukkit.event.BukkitEventsManager;
import com.bgsoftware.superiorskyblock.bukkit.platform.scheduler.BukkitScheduler;
import com.bgsoftware.superiorskyblock.bukkit.platform.server.BukkitServerManager;
import com.bgsoftware.superiorskyblock.platform.IPlatform;
import com.bgsoftware.superiorskyblock.platform.event.IEventsManager;
import com.bgsoftware.superiorskyblock.platform.scheduler.IScheduler;
import com.bgsoftware.superiorskyblock.platform.server.IServerManager;

public class BukkitPlatform implements IPlatform {

    private final BukkitScheduler scheduler;
    private final BukkitServerManager serverManager;
    private final BukkitEventsManager eventsManager;

    public BukkitPlatform(SuperiorSkyblockBukkitPlugin bukkitPlugin) {
        this.scheduler = new BukkitScheduler(bukkitPlugin);
        this.serverManager = new BukkitServerManager();
        this.eventsManager = new BukkitEventsManager(bukkitPlugin);
    }

    @Override
    public IScheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public IServerManager getServerManager() {
        return this.serverManager;
    }

    @Override
    public IEventsManager getEventsManager() {
        return this.eventsManager;
    }

}
