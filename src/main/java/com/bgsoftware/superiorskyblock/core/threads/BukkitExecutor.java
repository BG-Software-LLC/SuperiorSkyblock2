package com.bgsoftware.superiorskyblock.core.threads;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.platform.TaskScheduler;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BukkitExecutor {

    private static final int DEFAULT_SHUTDOWN_TIMEOUT = 1000 * 20;
    private static final int SHUTDOWN_INTERVAL_WAIT_TIME = 100;

    public static final Executor SYNC_EXECUTOR = runnable -> ensureMain(runnable);
    public static final Executor ASYNC_EXECUTOR = runnable -> ensureAsync(runnable);

    private static SuperiorSkyblockPlugin plugin;
    private static volatile State state = State.RUNNING;

    private static final AtomicLong ACTIVE_TASKS_COUNT = new AtomicLong(0);
    private static final Map<CompletableFuture<?>, AtomicBoolean> PENDING_SUBMISSIONS = new ConcurrentHashMap<>();

    private BukkitExecutor() {

    }

    public static void init(SuperiorSkyblockPlugin plugin) {
        BukkitExecutor.plugin = plugin;
        state = State.RUNNING;
    }

    @Nullable
    public static BukkitTask ensureMain(Runnable runnable) {
        if (ensureNotShudown())
            return null;

        if (state != State.PREPARE_SHUTDOWN && !scheduler().isGlobalThread()) {
            return sync(runnable);
        } else {
            runnable.run();
            return null;
        }
    }

    @Nullable
    public static BukkitTask ensureAsync(Runnable runnable) {
        if (ensureNotShudown())
            return null;

        if (state != State.PREPARE_SHUTDOWN && scheduler().isTickThread()) {
            return async(runnable);
        } else {
            runnable.run();
            return null;
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
            return scheduler().global(runnable, delay, 0L);
        }
    }

    public static BukkitTask async(Runnable runnable) {
        if (ensureNotShudown())
            return null;

        if (state == State.PREPARE_SHUTDOWN) {
            runnable.run();
            return null;
        } else {
            return scheduler().async(runnable, 0L, 0L);
        }
    }

    public static BukkitTask async(Runnable runnable, long delay) {
        if (ensureNotShudown())
            return null;

        if (state == State.PREPARE_SHUTDOWN) {
            runnable.run();
            return null;
        } else {
            return scheduler().async(runnable, delay, 0L);
        }
    }

    public static void asyncTimer(Runnable runnable, long delay) {
        if (ensureNotShudown())
            return;

        scheduler().async(runnable, delay, delay);
    }

    public static void timer(Runnable runnable, long delay) {
        if (ensureNotShudown())
            return;

        scheduler().global(runnable, delay, delay);
    }

    public static boolean isFolia() {
        return scheduler().isFolia();
    }

    public static boolean isOwned(Entity entity) {
        return scheduler().isOwned(entity);
    }

    public static boolean isOwned(Location location) {
        return scheduler().isOwned(location);
    }

    public static BukkitTask sync(Entity entity, Runnable runnable) {
        return sync(entity, runnable, 0L);
    }

    public static BukkitTask sync(Entity entity, Runnable runnable, long delay) {
        return ensureNotShudown() ? null : scheduler().entity(entity, runnable, null, delay, 0L);
    }

    public static BukkitTask sync(Location location, Runnable runnable) {
        return sync(location, runnable, 0L);
    }

    public static BukkitTask sync(Location location, Runnable runnable, long delay) {
        return ensureNotShudown() ? null : scheduler().region(location, runnable, delay, 0L);
    }

    public static BukkitTask ensureMain(Entity entity, Runnable runnable) {
        if (ensureNotShudown() || entity == null)
            return null;
        if (isOwned(entity)) {
            runnable.run();
            return null;
        }
        return sync(entity, runnable);
    }

    public static BukkitTask ensureMain(Location location, Runnable runnable) {
        if (ensureNotShudown())
            return null;
        if (isOwned(location)) {
            runnable.run();
            return null;
        }
        return sync(location, runnable);
    }

    public static BukkitTask timer(Entity entity, Runnable runnable, long period) {
        return ensureNotShudown() ? null : scheduler().entity(entity, runnable, null, period, period);
    }

    public static BukkitTask timer(Location location, Runnable runnable, long period) {
        return ensureNotShudown() ? null : scheduler().region(location, runnable, period, period);
    }

    public static <T> CompletableFuture<T> submit(Supplier<T> supplier) {
        return submit(null, null, supplier);
    }

    public static <T> CompletableFuture<T> submit(Entity entity, Supplier<T> supplier) {
        if (entity == null)
            throw new IllegalArgumentException("Entity cannot be null");
        return submit(entity, null, supplier);
    }

    public static <T> CompletableFuture<T> submit(@Nullable Location location, Supplier<T> supplier) {
        return submit(null, location, supplier);
    }

    private static <T> CompletableFuture<T> submit(@Nullable Entity entity, @Nullable Location location,
                                                   Supplier<T> supplier) {
        CompletableFuture<T> result = new CompletableFuture<>();
        if (state != State.RUNNING) {
            result.completeExceptionally(new IllegalStateException("Plugin is stopping"));
            return result;
        }
        AtomicBoolean claimed = new AtomicBoolean();
        PENDING_SUBMISSIONS.put(result, claimed);
        result.whenComplete((value, error) -> PENDING_SUBMISSIONS.remove(result));
        try {
            Runnable task = () -> {
                if (!claimed.compareAndSet(false, true) || result.isDone())
                    return;
                try {
                    result.complete(supplier.get());
                } catch (Throwable error) {
                    result.completeExceptionally(error);
                }
            };
            if (entity != null) {
                if (isOwned(entity)) {
                    task.run();
                } else {
                    scheduler().entity(entity, task, () -> {
                        if (claimed.compareAndSet(false, true))
                            result.completeExceptionally(new CancellationException("Entity retired before the task started"));
                    }, 0L, 0L);
                }
            } else if (location != null) {
                ensureMain(location, task);
            } else {
                ensureMain(task);
            }
        } catch (Throwable error) {
            result.completeExceptionally(error);
        }
        if (state != State.RUNNING && claimed.compareAndSet(false, true))
            result.completeExceptionally(new CancellationException("Plugin is stopping"));
        return result;
    }

    private static TaskScheduler scheduler() {
        return plugin.getTaskScheduler();
    }

    public static NestedTask<Void> createTask() {
        return new NestedTask<Void>().complete();
    }

    public static void prepareShutdown() {
        state = State.PREPARE_SHUTDOWN;
    }

    public static void close(SuperiorSkyblockPlugin plugin) {
        // Waiting for all active tasks to finish

        Log.info("This can take up to " + (DEFAULT_SHUTDOWN_TIMEOUT / 1000) + " seconds to complete");

        long timeoutLeft = DEFAULT_SHUTDOWN_TIMEOUT;

        while (ACTIVE_TASKS_COUNT.get() != 0 && timeoutLeft > 0) {
            try {
                Thread.sleep(SHUTDOWN_INTERVAL_WAIT_TIME);
                timeoutLeft -= SHUTDOWN_INTERVAL_WAIT_TIME;
            } catch (Throwable ignored) {
            }
        }

        if (ACTIVE_TASKS_COUNT.get() != 0) {
            new RuntimeException("Not all active tasks finished").printStackTrace();
        }

        state = State.SHUTDOWN;
        PENDING_SUBMISSIONS.forEach((result, claimed) -> {
            if (claimed.compareAndSet(false, true))
                result.completeExceptionally(new CancellationException("Plugin stopped before the region task started"));
        });
        scheduler().cancelTasks();
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
                onCreate();
                value.whenCompleteAsync((v, ex) -> {
                    try {
                        nestedTask.value.complete(function.apply(v));
                    } finally {
                        onComplete();
                    }
                }, SYNC_EXECUTOR);
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
                onCreate();
                value.whenCompleteAsync((v, ex) -> {
                    try {
                        consumer.accept(v);
                        nestedTask.value.complete(null);
                    } finally {
                        onComplete();
                    }
                }, SYNC_EXECUTOR);
            }
            return nestedTask;
        }

        public <R> NestedTask<R> runAsync(Function<T, R> function) {
            ensureNotShudown();

            NestedTask<R> nestedTask = new NestedTask<>();
            if (state == State.PREPARE_SHUTDOWN) {
                nestedTask.value.complete(function.apply(value.join()));
            } else {
                onCreate();
                value.whenCompleteAsync((v, ex) -> {
                    try {
                        nestedTask.value.complete(function.apply(v));
                    } finally {
                        onComplete();
                    }
                }, ASYNC_EXECUTOR);
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
                onCreate();
                value.whenCompleteAsync((v, ex) -> {
                    try {
                        consumer.accept(v);
                        nestedTask.value.complete(null);
                    } finally {
                        onComplete();
                    }
                }, ASYNC_EXECUTOR);
            }
            return nestedTask;
        }

        private NestedTask<T> complete() {
            value.complete(null);
            return this;
        }

        private static void onCreate() {
            long curr = ACTIVE_TASKS_COUNT.incrementAndGet();
            Log.debug(Debug.TRACK_TASK, curr);
        }

        private static void onComplete() {
            long curr = ACTIVE_TASKS_COUNT.decrementAndGet();
            Log.debug(Debug.TRACK_TASK, curr);
            if (curr < 0) {
                new RuntimeException("Active tasks count is less than 0").printStackTrace();
                ACTIVE_TASKS_COUNT.set(0);
            }
        }

    }

    private enum State {

        RUNNING,
        PREPARE_SHUTDOWN,
        SHUTDOWN

    }

}
