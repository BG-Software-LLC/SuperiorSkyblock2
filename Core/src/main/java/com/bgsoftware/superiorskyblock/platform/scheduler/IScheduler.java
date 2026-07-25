package com.bgsoftware.superiorskyblock.platform.scheduler;

import com.bgsoftware.common.annotations.Nullable;

public interface IScheduler {

    boolean isMainThread();

    @Nullable
    Object runSync(Runnable task);

    @Nullable
    Object runSync(Runnable task, long delayTicks);

    @Nullable
    Object runAsync(Runnable task);

    @Nullable
    Object runAsync(Runnable task, long delayTicks);

    @Nullable
    Object runSyncTimer(Runnable task, long delayTicks, long periodTicks);

    @Nullable
    Object runAsyncTimer(Runnable task, long delayTicks, long periodTicks);

    @Nullable
    Object ensureMain(Runnable task);

    @Nullable
    Object ensureAsync(Runnable task);

    void cancelTask(@Nullable Object task);

    NestedTask<Void> createTask();

    void prepareShutdown();

    void shutdown();

}
