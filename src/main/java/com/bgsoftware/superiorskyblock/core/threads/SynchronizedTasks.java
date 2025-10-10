package com.bgsoftware.superiorskyblock.core.threads;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.concurrent.CountDownLatch;

public class SynchronizedTasks {

    @Nullable
    private final CountDownLatch countDownLatch;
    @Nullable
    private final Runnable onFinishCallback;

    public SynchronizedTasks(int count, @Nullable Runnable onFinishCallback) {
        this.countDownLatch = count <= 0 ? null : new CountDownLatch(count);
        this.onFinishCallback = onFinishCallback;
    }

    public void notifyTaskComplete() {
        if (this.countDownLatch != null)
            this.countDownLatch.countDown();
    }

    public void waitAllAsync() {
        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(this::waitAllAsync);
            return;
        }

        if (this.countDownLatch != null) {
            try {
                this.countDownLatch.await();
            } catch (InterruptedException error) {
                throw new RuntimeException(error);
            }
        }

        if (this.onFinishCallback != null)
            this.onFinishCallback.run();
    }

}
