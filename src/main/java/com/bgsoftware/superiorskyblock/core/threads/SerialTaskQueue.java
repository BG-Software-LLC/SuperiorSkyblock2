package com.bgsoftware.superiorskyblock.core.threads;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SerialTaskQueue<K> {

    private final Map<K, CompletableFuture<?>> pending = new HashMap<>();

    public <T> CompletableFuture<T> submit(K key, Supplier<CompletableFuture<T>> task) {
        CompletableFuture<T> result;
        synchronized (pending) {
            CompletableFuture<?> previous = pending.get(key);
            if (previous == null)
                previous = CompletableFuture.completedFuture(null);
            result = previous.handle((ignored, error) -> null).thenComposeAsync(ignored -> task.get());
            pending.put(key, result);
        }
        result.whenComplete((ignored, error) -> {
            synchronized (pending) {
                pending.remove(key, result);
            }
        });
        return result.thenApply(value -> value);
    }

}
