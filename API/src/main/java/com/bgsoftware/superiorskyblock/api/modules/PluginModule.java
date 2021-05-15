package com.bgsoftware.superiorskyblock.api.modules;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.logging.Logger;

public abstract class PluginModule {

    private final String moduleName, authorName;
    private boolean initialized = false;
    private File dataFolder;
    private Logger logger;

    protected PluginModule(String moduleName, String authorName){
        this.moduleName = moduleName;
        this.authorName = authorName;
    }

    public abstract void onEnable(SuperiorSkyblock plugin);

    public abstract void onReload(SuperiorSkyblock plugin);

    public abstract void onDisable(SuperiorSkyblock plugin);

    public abstract Listener[] getModuleListeners(SuperiorSkyblock plugin);

    public abstract SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock plugin);

    public abstract SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock plugin);

    public final String getName() {
        return moduleName;
    }

    public final String getAuthor() {
        return authorName;
    }

    public final File getDataFolder(){
        return dataFolder;
    }

    public final Logger getLogger(){
        return logger;
    }

    public final boolean isInitialized(){
        return initialized;
    }

    public final void initModule(SuperiorSkyblock plugin, File dataFolder){
        if(initialized)
            throw new RuntimeException("The module " + moduleName + " was already initialized.");

        initialized = true;

        this.dataFolder = dataFolder;
        this.logger = new ModuleLogger(this);

        if(!dataFolder.exists() && !dataFolder.mkdirs())
            throw new RuntimeException("Cannot create module folder for " + moduleName + ".");

        onPluginInit(plugin);
    }

    public final void disableModule(){
        initialized = false;
    }

    protected void onPluginInit(SuperiorSkyblock plugin){

    }

}