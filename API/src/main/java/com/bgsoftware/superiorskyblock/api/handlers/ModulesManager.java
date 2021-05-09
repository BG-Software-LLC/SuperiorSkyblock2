package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.modules.PluginModule;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface ModulesManager {

    /**
     * Register a new module to the plugin.
     * @param pluginModule The module to register.
     */
    void registerModule(PluginModule pluginModule);

    /**
     * Register a new module to the plugin from a file.
     * @param moduleFile The module to register.
     */
    PluginModule registerModule(File moduleFile) throws IOException, ReflectiveOperationException;

    /**
     * Unregister a module from the plugin.
     * @param pluginModule The module to unregister.
     */
    void unregisterModule(PluginModule pluginModule);

    /**
     * Get a module by its name.
     * @param name The name of the module.
     */
    @Nullable
    PluginModule getModule(String name);

    /**
     * Get all the active modules currently running.
     */
    Collection<PluginModule> getModules();

}
