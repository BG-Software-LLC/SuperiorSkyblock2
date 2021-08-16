package com.bgsoftware.superiorskyblock.utils.lists;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class CompletableFutureList<E> extends ArrayList<CompletableFuture<E>> {

    public CompletableFutureList(){

    }

    public CompletableFutureList(ArrayList<CompletableFuture<E>> other){
        super(other);
    }

    public void forEachCompleted(Consumer<? super E> consumer, Consumer<Throwable> onFailure){
        for(CompletableFuture<E> completableFuture : this){
            completableFuture.whenComplete((result, error) -> {
                if(error == null)
                    consumer.accept(result);
                else
                    onFailure.accept(error);
            });
        }
        try {
            CompletableFuture.allOf(toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        }catch (Exception ex){
            onFailure.accept(ex);
        }
    }

}
