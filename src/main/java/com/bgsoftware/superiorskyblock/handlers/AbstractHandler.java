package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;

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
