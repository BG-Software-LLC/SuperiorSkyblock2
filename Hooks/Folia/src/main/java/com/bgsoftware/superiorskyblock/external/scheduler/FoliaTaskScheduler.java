package com.bgsoftware.superiorskyblock.external.scheduler;

import com.bgsoftware.superiorskyblock.api.platform.TaskScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;

public class FoliaTaskScheduler implements TaskScheduler {

    private final Plugin plugin;
    private final Set<ScheduledTask> regionalTasks = ConcurrentHashMap.newKeySet();
    private final AtomicInteger taskIds = new AtomicInteger();
    private final Map<ScheduledTask, Runnable> entityRetirements = new ConcurrentHashMap<>();
    private volatile boolean stopping;

    public FoliaTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return true;
    }

    @Override
    public boolean isTickThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isGlobalThread() {
        return Bukkit.isGlobalTickThread();
    }

    @Override
    public boolean isOwned(Entity entity) {
        return entity != null && Bukkit.isOwnedByCurrentRegion(entity);
    }

    @Override
    public boolean isOwned(Location location) {
        return location != null && location.getWorld() != null && Bukkit.isOwnedByCurrentRegion(location);
    }

    @Override
    public BukkitTask global(Runnable task, long delay, long period) {
        ensureRunning();
        Consumer<ScheduledTask> callback = scheduled -> task.run();
        ScheduledTask scheduled = period > 0 ?
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, callback, Math.max(1L, delay), period) :
                Bukkit.getGlobalRegionScheduler().runDelayed(plugin, callback, Math.max(1L, delay));
        if (stopping)
            scheduled.cancel();
        return new Task(scheduled, true);
    }

    @Override
    public BukkitTask entity(Entity entity, Runnable task, Runnable retired, long delay, long period) {
        if (entity == null || stopping) {
            runRetired(retired);
            return null;
        }
        AtomicReference<ScheduledTask> reference = new AtomicReference<>();
        AtomicBoolean retiredCalled = new AtomicBoolean();
        Runnable onRetired = () -> {
            if (!retiredCalled.compareAndSet(false, true))
                return;
            ScheduledTask scheduled = reference.get();
            if (scheduled != null) {
                regionalTasks.remove(scheduled);
                entityRetirements.remove(scheduled);
            }
            runRetired(retired);
        };
        Consumer<ScheduledTask> callback = scheduled -> {
            if (period == 0) {
                regionalTasks.remove(scheduled);
                entityRetirements.remove(scheduled);
            }
            try {
                task.run();
            } finally {
                if (period == 0) {
                    regionalTasks.remove(scheduled);
                    entityRetirements.remove(scheduled);
                } else if (scheduled.isCancelled()) {
                    onRetired.run();
                }
            }
        };
        ScheduledTask scheduled;
        try {
            scheduled = period > 0 ?
                    entity.getScheduler().runAtFixedRate(plugin, callback, onRetired, Math.max(1L, delay), period) :
                    entity.getScheduler().runDelayed(plugin, callback, onRetired, Math.max(1L, delay));
        } catch (RuntimeException | Error error) {
            onRetired.run();
            throw error;
        }
        if (scheduled == null) {
            onRetired.run();
            return null;
        }
        reference.set(scheduled);
        regionalTasks.add(scheduled);
        entityRetirements.put(scheduled, onRetired);
        if (stopping || scheduled.isCancelled()) {
            cancelTask(scheduled);
        } else if (scheduled.getExecutionState() == ScheduledTask.ExecutionState.FINISHED || retiredCalled.get()) {
            regionalTasks.remove(scheduled);
            entityRetirements.remove(scheduled);
        }
        return new Task(scheduled, true);
    }

    @Override
    public BukkitTask region(Location location, Runnable task, long delay, long period) {
        ensureRunning();
        Consumer<ScheduledTask> callback = scheduled -> {
            if (period == 0)
                regionalTasks.remove(scheduled);
            task.run();
        };
        ScheduledTask scheduled = period > 0 ?
                Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, callback, Math.max(1L, delay), period) :
                Bukkit.getRegionScheduler().runDelayed(plugin, location, callback, Math.max(1L, delay));
        regionalTasks.add(scheduled);
        if (stopping)
            cancelTask(scheduled);
        else if (scheduled.isCancelled() || scheduled.getExecutionState() == ScheduledTask.ExecutionState.FINISHED)
            regionalTasks.remove(scheduled);
        return new Task(scheduled, true);
    }

    @Override
    public BukkitTask async(Runnable task, long delay, long period) {
        ensureRunning();
        Consumer<ScheduledTask> callback = scheduled -> task.run();
        ScheduledTask scheduled = period > 0 ?
                Bukkit.getAsyncScheduler().runAtFixedRate(plugin, callback, delay * 50L, period * 50L, TimeUnit.MILLISECONDS) :
                delay > 0 ? Bukkit.getAsyncScheduler().runDelayed(plugin, callback, delay * 50L, TimeUnit.MILLISECONDS) :
                        Bukkit.getAsyncScheduler().runNow(plugin, callback);
        if (stopping)
            scheduled.cancel();
        return new Task(scheduled, false);
    }

    @Override
    public void cancelTasks() {
        stopping = true;
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
        regionalTasks.forEach(this::cancelTask);
        regionalTasks.clear();
    }

    private void ensureRunning() {
        if (stopping)
            throw new IllegalStateException("Scheduler is stopping");
    }

    private void runRetired(Runnable retired) {
        if (retired == null)
            return;
        try {
            retired.run();
        } catch (Throwable error) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retire scheduled entity task", error);
        }
    }

    private void cancelTask(ScheduledTask task) {
        ScheduledTask.CancelledState result = task.cancel();
        regionalTasks.remove(task);
        if (result == ScheduledTask.CancelledState.CANCELLED_BY_CALLER ||
                result == ScheduledTask.CancelledState.CANCELLED_ALREADY) {
            Runnable retired = entityRetirements.remove(task);
            if (retired != null)
                retired.run();
        } else if (result == ScheduledTask.CancelledState.ALREADY_EXECUTED) {
            entityRetirements.remove(task);
        }
    }

    private final class Task implements BukkitTask {

        private final ScheduledTask task;
        private final boolean sync;
        private final int id = taskIds.incrementAndGet();

        private Task(ScheduledTask task, boolean sync) {
            this.task = task;
            this.sync = sync;
        }

        @Override
        public int getTaskId() {
            return id;
        }

        @Override
        public Plugin getOwner() {
            return plugin;
        }

        @Override
        public boolean isSync() {
            return sync;
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public void cancel() {
            cancelTask(task);
        }
    }

}
