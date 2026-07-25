package com.bgsoftware.superiorskyblock.platform.scheduler;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A chainable async task built on {@link CompletableFuture}, decoupled from any platform. Its
 * continuations are dispatched through {@link IScheduler}; the class also owns the global
 * task-lifecycle accounting ({@link #ACTIVE_TASKS_COUNT}, {@link State}) that the scheduler drains
 * on shutdown.
 */
public class NestedTask<T> {

    private static final int DEFAULT_SHUTDOWN_TIMEOUT = 1000 * 20;
    private static final int SHUTDOWN_INTERVAL_WAIT_TIME = 100;

    private static State state = State.RUNNING;

    private static final AtomicLong ACTIVE_TASKS_COUNT = new AtomicLong(0);

    private final CompletableFuture<T> value = new CompletableFuture<>();

    private NestedTask() {
    }

    public static NestedTask<Void> create() {
        return new NestedTask<Void>().complete();
    }

    private static IScheduler scheduler() {
        return SuperiorSkyblockPlugin.getPlugin().getPlatform().getScheduler();
    }

    public static void prepareShutdown() {
        state = State.PREPARE_SHUTDOWN;
    }

    /**
     * Waits for all active nested-task continuations to drain, then marks the executor as shut down.
     * Called by the scheduler's {@code shutdown()} before cancelling the server's scheduled tasks.
     */
    public static void awaitActiveTasks() {
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
    }

    public static boolean isPrepareShutdown() {
        return state == State.PREPARE_SHUTDOWN;
    }

    public static boolean ensureNotShutdown() {
        if (state == State.SHUTDOWN) {
            new RuntimeException("Tried to schedule a task after the scheduler was shut down").printStackTrace();
            return true;
        }

        return false;
    }

    public <R> NestedTask<R> runSync(Function<T, R> function) {
        ensureNotShutdown();

        NestedTask<R> nestedTask = new NestedTask<>();
        if (state == State.PREPARE_SHUTDOWN) {
            nestedTask.value.complete(function.apply(value.join()));
        } else {
            onCreate();
            value.whenComplete((value, ex) -> scheduler().ensureMain(() -> {
                try {
                    nestedTask.value.complete(function.apply(value));
                } finally {
                    onComplete();
                }
            }));
        }
        return nestedTask;
    }

    public NestedTask<Void> runSync(Consumer<T> consumer) {
        ensureNotShutdown();

        NestedTask<Void> nestedTask = new NestedTask<>();
        if (state == State.PREPARE_SHUTDOWN) {
            consumer.accept(value.join());
            nestedTask.value.complete(null);
        } else {
            onCreate();
            value.whenComplete((value, ex) -> scheduler().ensureMain(() -> {
                try {
                    consumer.accept(value);
                    nestedTask.value.complete(null);
                } finally {
                    onComplete();
                }
            }));
        }
        return nestedTask;
    }

    public <R> NestedTask<R> runAsync(Function<T, R> function) {
        ensureNotShutdown();

        NestedTask<R> nestedTask = new NestedTask<>();
        if (state == State.PREPARE_SHUTDOWN) {
            nestedTask.value.complete(function.apply(value.join()));
        } else {
            onCreate();
            value.whenComplete((value, ex) -> scheduler().runAsync(() -> {
                try {
                    nestedTask.value.complete(function.apply(value));
                } finally {
                    onComplete();
                }
            }));
        }
        return nestedTask;
    }

    public NestedTask<Void> runAsync(Consumer<T> consumer) {
        ensureNotShutdown();

        NestedTask<Void> nestedTask = new NestedTask<>();
        if (state == State.PREPARE_SHUTDOWN) {
            consumer.accept(value.join());
            nestedTask.value.complete(null);
        } else {
            onCreate();
            value.whenComplete((value, ex) -> scheduler().runAsync(() -> {
                try {
                    consumer.accept(value);
                    nestedTask.value.complete(null);
                } finally {
                    onComplete();
                }
            }));
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

    private enum State {

        RUNNING,
        PREPARE_SHUTDOWN,
        SHUTDOWN

    }

}
