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
            try{
                consumer.accept(completableFuture.get());
            }catch (Exception ex){
                onFailure.accept(completableFuture, ex);
            }
        }
    }

}
