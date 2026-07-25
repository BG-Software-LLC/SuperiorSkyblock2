package com.bgsoftware.superiorskyblock.bukkit.platform;

import com.bgsoftware.superiorskyblock.platform.IPlatform;
import com.bgsoftware.superiorskyblock.bukkit.platform.scheduler.BukkitScheduler;
import com.bgsoftware.superiorskyblock.bukkit.platform.server.BukkitServerManager;
import com.bgsoftware.superiorskyblock.platform.scheduler.IScheduler;
import com.bgsoftware.superiorskyblock.platform.server.IServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlatform implements IPlatform {

    private final BukkitScheduler scheduler;
    private final BukkitServerManager serverManager;

    public BukkitPlatform(JavaPlugin bukkitPlugin) {
        this.scheduler = new BukkitScheduler(bukkitPlugin);
        this.serverManager = new BukkitServerManager();
    }

    @Override
    public IScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public IServerManager getServerManager() {
        return serverManager;
    }

}
