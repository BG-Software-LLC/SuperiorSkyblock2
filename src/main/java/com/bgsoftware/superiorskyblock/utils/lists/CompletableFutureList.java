package com.bgsoftware.superiorskyblock.utils.lists;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class CompletableFutureList<E> extends ArrayList<CompletableFuture<E>> {

    public CompletableFutureList(){

    }

    public CompletableFutureList(ArrayList<CompletableFuture<E>> other){
        super(other);
    }

    public void forEachCompleted(Consumer<? super E> consumer, Consumer<Throwable> onFailure){
        try {
            CompletableFuture.allOf(toArray(new CompletableFuture[0])).thenRun(() -> {
                for (CompletableFuture<E> completableFuture : this) {
                    E result = completableFuture.getNow(null);
                    assert result != null; // Result cannot be null as all CompletableFutures must be completed by now.
                    consumer.accept(result);
                }
            }).exceptionally(error -> {
                onFailure.accept(error);
                return null;
            }).get(10, TimeUnit.SECONDS);
        }catch (Throwable error){
            onFailure.accept(error);
        }
    }

}
