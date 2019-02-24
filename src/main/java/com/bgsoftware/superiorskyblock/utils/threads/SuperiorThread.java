package com.bgsoftware.superiorskyblock.utils.threads;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class SuperiorThread {

    private static final Executor executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock Thread").build());

    private Runnable runnable;

    public SuperiorThread(Runnable runnable){
        this.runnable = runnable;
    }

    public void start(){
        executor.execute(runnable);
    }

}
