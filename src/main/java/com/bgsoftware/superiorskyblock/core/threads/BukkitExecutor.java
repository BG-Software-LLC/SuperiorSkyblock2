package com.bgsoftware.superiorskyblock.core.threads;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class BukkitExecutor {

    private static SuperiorSkyblockPlugin plugin;
    private static ExecutorService databaseExecutor;
    private static boolean shutdown = false;
    private static boolean syncDatabaseCalls = false;

    private BukkitExecutor() {

    }

    public static void init(SuperiorSkyblockPlugin plugin) {
        BukkitExecutor.plugin = plugin;
        databaseExecutor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock Database Thread %d").build());
    }

    public static void ensureMain(Runnable runnable) {
        if (shutdown)
            return;

        if (!Bukkit.isPrimaryThread()) {
            sync(runnable);
        } else {
            runnable.run();
        }
    }

    public static BukkitTask sync(Runnable runnable) {
        if (shutdown)
            return null;

        return sync(runnable, 0);
    }

    public static BukkitTask sync(Runnable runnable, long delay) {
        if (shutdown)
            return null;

        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static void data(Runnable runnable) {
        if (shutdown)
            return;

        if (syncDatabaseCalls) {
            runnable.run();
        } else {
            databaseExecutor.execute(runnable);
        }
    }

    public static boolean isDataThread() {
        return syncDatabaseCalls || Thread.currentThread().getName().contains("SuperiorSkyblock Database Thread");
    }

    public static void async(Runnable runnable) {
        if (shutdown)
            return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static void async(Runnable runnable, long delay) {
        if (shutdown)
            return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static void asyncTimer(Runnable runnable, long delay) {
        if (shutdown)
            return;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, delay);
    }

    public static void timer(Runnable runnable, long delay) {
        if (shutdown)
            return;

        Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, delay);
    }

    public static NestedTask<Void> createTask() {
        return new NestedTask<Void>().complete();
    }

    public static void syncDatabaseCalls() {
        syncDatabaseCalls = true;
    }

    public static void close() {
        try {
            shutdown = true;
            SuperiorSkyblockPlugin.log("Shutting down database executor");
            shutdownAndAwaitTermination();
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }
    }

    private static void shutdownAndAwaitTermination() {
        databaseExecutor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!databaseExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                databaseExecutor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!databaseExecutor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            databaseExecutor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public static class NestedTask<T> {

        private final CompletableFuture<T> value = new CompletableFuture<>();

        NestedTask() {
        }

        public <R> NestedTask<R> runSync(Function<T, R> function) {
            NestedTask<R> nestedTask = new NestedTask<>();
            value.whenComplete((value, ex) -> BukkitExecutor.ensureMain(() -> nestedTask.value.complete(function.apply(value))));
            return nestedTask;
        }

        public NestedTask<Void> runSync(Consumer<T> consumer) {
            NestedTask<Void> nestedTask = new NestedTask<>();
            value.whenComplete((value, ex) -> BukkitExecutor.ensureMain(() -> {
                consumer.accept(value);
                nestedTask.value.complete(null);
            }));
            return nestedTask;
        }

        public <R> NestedTask<R> runAsync(Function<T, R> function) {
            NestedTask<R> nestedTask = new NestedTask<>();
            value.whenComplete((value, ex) -> BukkitExecutor.async(() -> nestedTask.value.complete(function.apply(value))));
            return nestedTask;
        }

        public NestedTask<Void> runAsync(Consumer<T> consumer) {
            NestedTask<Void> nestedTask = new NestedTask<>();
            value.whenComplete((value, ex) -> BukkitExecutor.async(() -> {
                consumer.accept(value);
                nestedTask.value.complete(null);
            }));
            return nestedTask;
        }

        private NestedTask<T> complete() {
            value.complete(null);
            return this;
        }

    }

}
