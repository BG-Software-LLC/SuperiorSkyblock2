package com.bgsoftware.superiorskyblock.core.threads;

import com.bgsoftware.superiorskyblock.api.platform.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitTaskScheduler implements TaskScheduler {

    private final Plugin plugin;

    public BukkitTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return false;
    }

    @Override
    public boolean isTickThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isGlobalThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isOwned(Entity entity) {
        return entity != null && Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isOwned(Location location) {
        return location != null && Bukkit.isPrimaryThread();
    }

    @Override
    public BukkitTask global(Runnable task, long delay, long period) {
        return period > 0 ? Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period) :
                Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    @Override
    public BukkitTask entity(Entity entity, Runnable task, Runnable retired, long delay, long period) {
        if (entity == null) {
            if (retired != null)
                retired.run();
            return null;
        }
        return global(task, delay, period);
    }

    @Override
    public BukkitTask region(Location location, Runnable task, long delay, long period) {
        return global(task, delay, period);
    }

    @Override
    public BukkitTask async(Runnable task, long delay, long period) {
        return period > 0 ? Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period) :
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

}
