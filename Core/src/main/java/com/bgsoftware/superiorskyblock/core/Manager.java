package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;

public abstract class Manager {

    protected final SuperiorSkyblockPlugin plugin;

    protected Manager(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void loadData() throws ManagerLoadException;

}
