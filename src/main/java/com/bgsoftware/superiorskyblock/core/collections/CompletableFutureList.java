package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CompletableFutureList<E> extends ArrayList<CompletableFuture<E>> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public CompletableFutureList() {
    }

    public void forEachCompleted(Consumer<? super E> consumer, Consumer<Throwable> onFailure) {
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(toArray(new CompletableFuture[0])).thenRun(() -> {
            for (CompletableFuture<E> completableFuture : this) {
                E result = completableFuture.getNow(null);
                assert result != null; // Result cannot be null as all CompletableFutures must be completed by now.
                consumer.accept(result);
            }
        }).exceptionally(error -> {
            onFailure.accept(error);
            return null;
        });

        if (plugin.getSettings().getRecalcTaskTimeout() <= 0L) {
            allTasks.join();
        } else try {
            allTasks.get(plugin.getSettings().getRecalcTaskTimeout(), TimeUnit.SECONDS);
        } catch (Throwable error) {
            Log.error(error, "An unexpected error occurred while waiting for tasks to complete:");
        }
    }

}
