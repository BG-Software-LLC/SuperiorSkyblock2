package com.bgsoftware.superiorskyblock.tasks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public final class CropsTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static BukkitTask task;
    private static int random;

    private CropsTask(){
        CropsTask.random = ThreadLocalRandom.current().nextInt();
        if(ServerVersion.isAtLeast(ServerVersion.v1_14)){
            CropsTask.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (World.Environment env : World.Environment.values())
                    random = tickWorld(plugin.getGrid().getIslandsWorld(env), random);
            }, 5L, 5L);
        }
        else {
            CropsTask.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                for (World.Environment env : World.Environment.values())
                    random = tickWorld(plugin.getGrid().getIslandsWorld(env), random);
            }, 5L, 5L);
        }
    }

    private int tickWorld(World world, int random){
        if(world != null)
            return plugin.getNMSBlocks().tickWorld(world, random);

        return random;
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
