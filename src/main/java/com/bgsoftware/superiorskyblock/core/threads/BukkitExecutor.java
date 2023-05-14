package com.bgsoftware.superiorskyblock.core.threads;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
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
    private static boolean syncBukkitCalls = false;

    private BukkitExecutor() {

    }

    public static void init(SuperiorSkyblockPlugin plugin) {
        BukkitExecutor.plugin = plugin;
        databaseExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock Database Thread").build());
    }

    public static void ensureMain(Consumer<ScheduledTask> runnable) {
        if (ensureNotShudown())
            return;

        sync(runnable);
    }

    public static ScheduledTask sync(Consumer<ScheduledTask> runnable) {
        return sync(runnable, 0);
    }

    public static ScheduledTask sync(Consumer<ScheduledTask> runnable, long delay) {
        if (ensureNotShudown())
            return null;
            // * 50 due to it being a ticket
            return Bukkit.getAsyncScheduler().runDelayed(plugin, runnable, delay * 50, TimeUnit.MILLISECONDS);
    }

    public static void data(Runnable runnable) {
        if (ensureNotShudown())
            return;

        databaseExecutor.execute(runnable);
    }

    public static boolean isDataThread() {
        return syncDatabaseCalls || Thread.currentThread().getName().equals("SuperiorSkyblock Database Thread");
    }

    public static void async(Consumer<ScheduledTask> runnable) {
        if (ensureNotShudown())
            return;

        Bukkit.getAsyncScheduler().runNow(plugin, runnable);
    }

    public static void async(Consumer<ScheduledTask> runnable, long delay) {
        if (ensureNotShudown())
            return;

        Bukkit.getAsyncScheduler().runDelayed(plugin, runnable, delay * 50, TimeUnit.MILLISECONDS);
    }

    public static void asyncTimer(Consumer<ScheduledTask> runnable, long delay) {
        if (ensureNotShudown())
            return;

        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, runnable, delay, delay * 50, TimeUnit.MILLISECONDS);
    }

    public static void timer(Consumer<ScheduledTask> runnable, long delay) {
        if (ensureNotShudown())
            return;

        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, runnable, delay, delay * 50, TimeUnit.MILLISECONDS);
    }

    public static NestedTask<Void> createTask() {
        return new NestedTask<Void>().complete();
    }

    public static void prepareDisable() {
        syncDatabaseCalls = true;
        syncBukkitCalls = true;
    }

    public static void close() {
        try {
            shutdown = true;
            Log.info("Shutting down database executor");
            shutdownAndAwaitTermination();
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while shutting down database executor:");
        }
    }

    private static boolean ensureNotShudown() {
        if (shutdown) {
            new RuntimeException("Tried to call BukkitExecutor after it was shut down").printStackTrace();
            return true;
        }

        return false;
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
            if (syncBukkitCalls) {
                nestedTask.value.complete(function.apply(value.join()));
            } else {
                value.whenComplete((value, ex) -> BukkitExecutor.ensureMain((a) -> nestedTask.value.complete(function.apply(value))));
            }
            return nestedTask;
        }

        public NestedTask<Void> runSync(Consumer<T> consumer) {
            NestedTask<Void> nestedTask = new NestedTask<>();
            if (syncBukkitCalls) {
                consumer.accept(value.join());
                nestedTask.value.complete(null);
            } else {
                value.whenComplete((value, ex) -> BukkitExecutor.ensureMain((a) -> {
                    consumer.accept(value);
                    nestedTask.value.complete(null);
                }));
            }
            return nestedTask;
        }

        public <R> NestedTask<R> runAsync(Function<T, R> function) {
            NestedTask<R> nestedTask = new NestedTask<>();
            if (syncBukkitCalls) {
                nestedTask.value.complete(function.apply(value.join()));
            } else {
                value.whenComplete((value, ex) -> BukkitExecutor.async((a) -> nestedTask.value.complete(function.apply(value))));
            }
            return nestedTask;
        }

        public NestedTask<Void> runAsync(Consumer<T> consumer) {
            NestedTask<Void> nestedTask = new NestedTask<>();
            if (syncBukkitCalls) {
                consumer.accept(value.join());
                nestedTask.value.complete(null);
            } else {
                value.whenComplete((value, ex) -> BukkitExecutor.async((a) -> {
                    consumer.accept(value);
                    nestedTask.value.complete(null);
                }));
            }
            return nestedTask;
        }

        private NestedTask<T> complete() {
            value.complete(null);
            return this;
        }

    }

}
