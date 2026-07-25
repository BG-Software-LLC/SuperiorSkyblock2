package com.bgsoftware.superiorskyblock.core.threads;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import javax.annotation.Nullable;
import java.util.concurrent.CountDownLatch;

public class SynchronizedTasks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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
        plugin.getPlatform().getScheduler().ensureAsync(this::waitAllAsyncInternal);
    }

    private void waitAllAsyncInternal() {
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
