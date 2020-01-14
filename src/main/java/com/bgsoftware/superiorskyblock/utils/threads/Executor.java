package com.bgsoftware.superiorskyblock.utils.threads;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Executor {

    private static final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock Thread %d").build());
    private static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock Database Thread %d").build());
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void ensureMain(Runnable runnable){
        if(!Bukkit.isPrimaryThread()){
            sync(runnable);
        }
        else{
            runnable.run();
        }
    }

    public static BukkitTask sync(Runnable runnable){
        return sync(runnable, 0);
    }

    public static BukkitTask sync(Runnable runnable, long delay){
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static void data(Runnable runnable){
        databaseExecutor.execute(runnable);
    }

    public static boolean isDataThread(){
        return Thread.currentThread().getName().contains("SuperiorSkyblock Database Thread");
    }

    public static void async(Runnable runnable){
        if(Bukkit.isPrimaryThread()){
            executor.execute(runnable);
        }
        else{
            runnable.run();
        }
    }

    public static void async(Runnable runnable, long delay){
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static void close(){
        try{
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
            databaseExecutor.shutdown();
            databaseExecutor.awaitTermination(1, TimeUnit.MINUTES);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
