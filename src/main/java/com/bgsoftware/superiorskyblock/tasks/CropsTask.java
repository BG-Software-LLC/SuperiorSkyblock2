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
        CropsTask.random = ThreadLocalRandom.current().nextInt();
        CropsTask.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () ->
                random = plugin.getNMSBlocks().tickIslands(random), 5L, 5L);
    }

    public static void startTask(){
        if(task != null)
            cancelTask();

        new CropsTask();
    }

    public static void cancelTask(){
        task.cancel();
    }

}
