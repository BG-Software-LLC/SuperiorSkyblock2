package com.bgsoftware.superiorskyblock.tasks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public final class CropsTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static BukkitTask task;
    private static int random;

    private CropsTask(){
    }

    public static void startTask(){
        if(task != null)
            cancelTask();

        int interval = plugin.getSettings().cropsInterval;
        CropsTask.random = ThreadLocalRandom.current().nextInt();
        CropsTask.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () ->
            random = plugin.getNMSBlocks().tickIslands(random), interval, interval);
    }

    public static void cancelTask(){
        task.cancel();
    }

}
