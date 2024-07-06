package com.bgsoftware.superiorskyblock.core.threads;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class BukkitExecutor {

    private static SuperiorSkyblockPlugin plugin;
    private static boolean shutdown = false;
    private static boolean syncBukkitCalls = false;

    private BukkitExecutor() {

    }

    public static void init(SuperiorSkyblockPlugin plugin) {
        BukkitExecutor.plugin = plugin;
    }

    public static void ensureMain(Runnable runnable) {
        if (ensureNotShudown())
            return;

        if (!syncBukkitCalls && !Bukkit.isPrimaryThread()) {
            sync(runnable);
        } else {
            runnable.run();
        }
    }

    public static BukkitTask sync(Runnable runnable) {
        return sync(runnable, 0);
    }

    public static BukkitTask sync(Runnable runnable, long delay) {
        if (ensureNotShudown())
            return null;

        if (syncBukkitCalls) {
            runnable.run();
            return null;
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    public static void async(Runnable runnable) {
        if (ensureNotShudown())
            return;

        if (syncBukkitCalls) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static void async(Runnable runnable, long delay) {
        if (ensureNotShudown())
            return;

        if (syncBukkitCalls) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
        }
    }

    public static void asyncTimer(Runnable runnable, long delay) {
        if (ensureNotShudown())
            return;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, delay);
    }

    public static void timer(Runnable runnable, long delay) {
        if (ensureNotShudown())
            return;

        Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, delay);
    }

    public static NestedTask<Void> createTask() {
        return new NestedTask<Void>().complete();
    }

    public static void prepareDisable() {
        syncBukkitCalls = true;
    }

    public static void close() {
        shutdown = true;
    }

    private static boolean ensureNotShudown() {
        if (shutdown) {
            new RuntimeException("Tried to call BukkitExecutor after it was shut down").printStackTrace();
            return true;
        }

        return false;
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
                value.whenComplete((value, ex) -> BukkitExecutor.ensureMain(() -> nestedTask.value.complete(function.apply(value))));
            }
            return nestedTask;
        }

        public NestedTask<Void> runSync(Consumer<T> consumer) {
            NestedTask<Void> nestedTask = new NestedTask<>();
            if (syncBukkitCalls) {
                consumer.accept(value.join());
                nestedTask.value.complete(null);
            } else {
                value.whenComplete((value, ex) -> BukkitExecutor.ensureMain(() -> {
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
                value.whenComplete((value, ex) -> BukkitExecutor.async(() -> nestedTask.value.complete(function.apply(value))));
            }
            return nestedTask;
        }

        public NestedTask<Void> runAsync(Consumer<T> consumer) {
            NestedTask<Void> nestedTask = new NestedTask<>();
            if (syncBukkitCalls) {
                consumer.accept(value.join());
                nestedTask.value.complete(null);
            } else {
                value.whenComplete((value, ex) -> BukkitExecutor.async(() -> {
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
