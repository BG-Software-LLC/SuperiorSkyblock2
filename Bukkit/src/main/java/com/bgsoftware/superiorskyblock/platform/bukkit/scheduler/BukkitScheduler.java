package com.bgsoftware.superiorskyblock.platform.bukkit.scheduler;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.platform.scheduler.NestedTask;
import com.bgsoftware.superiorskyblock.platform.scheduler.IScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitScheduler implements IScheduler {

    private final JavaPlugin plugin;

    public BukkitScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Nullable
    @Override
    public Object runSync(Runnable task) {
        return runSync(task, 0);
    }

    @Nullable
    @Override
    public Object runSync(Runnable task, long delayTicks) {
        if (NestedTask.ensureNotShutdown())
            return null;

        if (NestedTask.isPrepareShutdown()) {
            task.run();
            return null;
        }

        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Nullable
    @Override
    public Object runAsync(Runnable task) {
        if (NestedTask.ensureNotShutdown())
            return null;

        if (NestedTask.isPrepareShutdown()) {
            task.run();
            return null;
        }

        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Nullable
    @Override
    public Object runAsync(Runnable task, long delayTicks) {
        if (NestedTask.ensureNotShutdown())
            return null;

        if (NestedTask.isPrepareShutdown()) {
            task.run();
            return null;
        }

        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    @Nullable
    @Override
    public Object runSyncTimer(Runnable task, long delayTicks, long periodTicks) {
        if (NestedTask.ensureNotShutdown())
            return null;

        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    @Nullable
    @Override
    public Object runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        if (NestedTask.ensureNotShutdown())
            return null;

        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    @Nullable
    @Override
    public Object ensureMain(Runnable task) {
        if (NestedTask.ensureNotShutdown())
            return null;

        if (!NestedTask.isPrepareShutdown() && !Bukkit.isPrimaryThread()) {
            return runSync(task);
        } else {
            task.run();
            return null;
        }
    }

    @Nullable
    @Override
    public Object ensureAsync(Runnable task) {
        if (NestedTask.ensureNotShutdown())
            return null;

        if (!NestedTask.isPrepareShutdown() && Bukkit.isPrimaryThread()) {
            return runAsync(task);
        } else {
            task.run();
            return null;
        }
    }

    @Override
    public void cancelTask(@Nullable Object task) {
        if (task instanceof BukkitTask)
            ((BukkitTask) task).cancel();
    }

    @Override
    public NestedTask<Void> createTask() {
        return NestedTask.create();
    }

    @Override
    public void prepareShutdown() {
        NestedTask.prepareShutdown();
    }

    @Override
    public void shutdown() {
        NestedTask.awaitActiveTasks();
        Bukkit.getScheduler().cancelTasks(plugin);
    }

}
