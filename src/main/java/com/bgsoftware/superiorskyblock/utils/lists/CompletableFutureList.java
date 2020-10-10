package com.bgsoftware.superiorskyblock.utils.lists;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class CompletableFutureList<E> extends ArrayList<CompletableFuture<E>> {

    public CompletableFutureList(){

    }

    public CompletableFutureList(ArrayList<CompletableFuture<E>> other){
        super(other);
    }

    public void forEachCompleted(Consumer<? super E> consumer, BiConsumer<CompletableFuture<E>, Throwable> onFailure){
        for(CompletableFuture<E> completableFuture : this){
            completableFuture.whenComplete((e, ex) -> {
                if(ex == null)
                    consumer.accept(e);
                else
                    onFailure.accept(completableFuture, ex);
            });
        }
        CompletableFuture.allOf(toArray(new CompletableFuture[0])).join();
    }

}
