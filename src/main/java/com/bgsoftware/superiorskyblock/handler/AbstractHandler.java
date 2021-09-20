package com.bgsoftware.superiorskyblock.handler;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

public abstract class AbstractHandler {

    protected final SuperiorSkyblockPlugin plugin;

    protected AbstractHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    public abstract void loadData();

    public void loadDataWithException() throws HandlerLoadException {
        loadData();
    }

}
