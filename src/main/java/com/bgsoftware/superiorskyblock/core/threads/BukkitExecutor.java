package com.bgsoftware.superiorskyblock.core.threads;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

public class BukkitExecutor {

    private static SuperiorSkyblockPlugin plugin;
    private static State state = State.RUNNING;

    private static final AtomicLong ACTIVE_TASKS_COUNT = new AtomicLong(0);
    private static final CountDownLatch WAITABLE = new CountDownLatch(1);

    private BukkitExecutor() {

    }

    public static void init(SuperiorSkyblockPlugin plugin) {
        BukkitExecutor.plugin = plugin;
    }

    public static void ensureMain(Runnable runnable) {
        if (ensureNotShudown())
            return;

        if (state != State.PREPARE_SHUTDOWN && !Bukkit.isPrimaryThread()) {
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

        if (state == State.PREPARE_SHUTDOWN) {
            runnable.run();
            return null;
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    public static void async(Runnable runnable) {
        if (ensureNotShudown())
            return;

        if (state == State.PREPARE_SHUTDOWN) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static void async(Runnable runnable, long delay) {
        if (ensureNotShudown())
            return;

        if (state == State.PREPARE_SHUTDOWN) {
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

    public static void prepareShutdown() {
        state = State.PREPARE_SHUTDOWN;
    }

    public static void close(SuperiorSkyblockPlugin plugin) {
        // Waiting for all active tasks to finish

        try {
            WAITABLE.await();
        } catch (Throwable ignored) {
        }

        if (ACTIVE_TASKS_COUNT.get() != 0) {
            new RuntimeException("Not all active tasks finished").printStackTrace();
        }

        state = State.SHUTDOWN;
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private static boolean ensureNotShudown() {
        if (state == State.SHUTDOWN) {
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
            ensureNotShudown();

            NestedTask<R> nestedTask = new NestedTask<>();
            if (state == State.PREPARE_SHUTDOWN) {
                nestedTask.value.complete(function.apply(value.join()));
            } else {
                nestedTask.onCreate();
                value.whenComplete((value, ex) -> BukkitExecutor.ensureMain(() -> {
                    nestedTask.value.complete(function.apply(value));
                    onComplete();
                }));
            }
            return nestedTask;
        }

        public NestedTask<Void> runSync(Consumer<T> consumer) {
            ensureNotShudown();

            NestedTask<Void> nestedTask = new NestedTask<>();
            if (state == State.PREPARE_SHUTDOWN) {
                consumer.accept(value.join());
                nestedTask.value.complete(null);
            } else {
                nestedTask.onCreate();
                value.whenComplete((value, ex) -> BukkitExecutor.ensureMain(() -> {
                    consumer.accept(value);
                    nestedTask.value.complete(null);
                    onComplete();
                }));
            }
            return nestedTask;
        }

        public <R> NestedTask<R> runAsync(Function<T, R> function) {
            ensureNotShudown();

            NestedTask<R> nestedTask = new NestedTask<>();
            if (state == State.PREPARE_SHUTDOWN) {
                nestedTask.value.complete(function.apply(value.join()));
            } else {
                nestedTask.onCreate();
                value.whenComplete((value, ex) -> BukkitExecutor.async(() -> {
                    nestedTask.value.complete(function.apply(value));
                    onComplete();
                }));
            }
            return nestedTask;
        }

        public NestedTask<Void> runAsync(Consumer<T> consumer) {
            ensureNotShudown();

            NestedTask<Void> nestedTask = new NestedTask<>();
            if (state == State.PREPARE_SHUTDOWN) {
                consumer.accept(value.join());
                nestedTask.value.complete(null);
            } else {
                nestedTask.onCreate();
                value.whenComplete((value, ex) -> BukkitExecutor.async(() -> {
                    consumer.accept(value);
                    nestedTask.value.complete(null);
                    onComplete();
                }));
            }
            return nestedTask;
        }

        private NestedTask<T> complete() {
            value.complete(null);
            return this;
        }

        private void onCreate() {
            ACTIVE_TASKS_COUNT.incrementAndGet();
        }

        private void onComplete() {
            long current = ACTIVE_TASKS_COUNT.decrementAndGet();
            if (current == 0) {
                WAITABLE.countDown();
            }
        }

    }

    private enum State {

        RUNNING,
        PREPARE_SHUTDOWN,
        SHUTDOWN

    }

}
