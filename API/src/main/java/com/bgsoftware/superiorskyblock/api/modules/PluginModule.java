package com.bgsoftware.superiorskyblock.api.modules;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;

import java.io.File;

public abstract class PluginModule {

    private final String moduleName, authorName;
    private File dataFolder;

    protected PluginModule(String moduleName, String authorName){
        this.moduleName = moduleName;
        this.authorName = authorName;
    }

    public abstract void onEnable(SuperiorSkyblock plugin);

    public abstract void onDisable();

    public final String getName() {
        return moduleName;
    }

    public final String getAuthor() {
        return authorName;
    }

    public final File getDataFolder(){
        return dataFolder;
    }

    public final void initModule(File dataFolder){
        if(moduleName != null)
            throw new RuntimeException("This module was already initialized.");

        this.dataFolder = dataFolder;
    }

}
