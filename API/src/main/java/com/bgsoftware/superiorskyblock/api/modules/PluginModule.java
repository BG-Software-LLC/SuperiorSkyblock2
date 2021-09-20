package com.bgsoftware.superiorskyblock.api.modules;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

public abstract class PluginModule {

    private final String moduleName, authorName;

    private File dataFolder;
    private File moduleFile;
    private ClassLoader classLoader;
    private Logger logger;
    private ModuleResources moduleResources;

    private boolean initialized = false;

    protected PluginModule(String moduleName, String authorName) {
        this.moduleName = moduleName;
        this.authorName = authorName;
    }

    public abstract void onEnable(SuperiorSkyblock plugin);

    public abstract void onReload(SuperiorSkyblock plugin);

    public abstract void onDisable(SuperiorSkyblock plugin);

    public abstract Listener[] getModuleListeners(SuperiorSkyblock plugin);

    public abstract SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock plugin);

    public abstract SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock plugin);

    public ModuleLoadTime getLoadTime() {
        return ModuleLoadTime.NORMAL;
    }

    public final String getName() {
        return moduleName;
    }

    public final String getAuthor() {
        return authorName;
    }

    public final File getDataFolder() {
        return dataFolder;
    }

    @Nullable
    public final File getModuleFile() {
        return moduleFile;
    }

    @Nullable
    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    public final Logger getLogger() {
        return logger;
    }

    public final boolean isInitialized() {
        return initialized;
    }

    public final void saveResource(String resourceName) {
        if (this.moduleResources == null)
            throw new IllegalArgumentException("Cannot save resources for an uninitialized module.");

        moduleResources.saveResource(resourceName);
    }

    public final InputStream getResource(String resourceName) {
        if (this.moduleResources == null)
            throw new IllegalArgumentException("Cannot get resources for an uninitialized module.");

        return moduleResources.getResource(resourceName);
    }

    public final void initModule(SuperiorSkyblock plugin, File dataFolder) {
        if (initialized)
            throw new RuntimeException("The module " + moduleName + " was already initialized.");

        initialized = true;

        this.dataFolder = dataFolder;
        this.logger = new ModuleLogger(this);

        if (moduleFile != null && classLoader != null)
            this.moduleResources = new ModuleResources(this.moduleFile, this.dataFolder, this.classLoader);

        if (!dataFolder.exists() && !dataFolder.mkdirs())
            throw new RuntimeException("Cannot create module folder for " + moduleName + ".");

        onPluginInit(plugin);
    }

    public final void initModuleLoader(File moduleFile, ClassLoader classLoader) {
        if (initialized)
            throw new RuntimeException("The module " + moduleName + " was already initialized.");

        this.moduleFile = moduleFile;
        this.classLoader = classLoader;
    }

    public final void disableModule() {
        initialized = false;
    }

    protected void onPluginInit(SuperiorSkyblock plugin) {

    }

}