package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLoadTime;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface ModulesManager {

    /**
     * Register a new module to the plugin.
     *
     * @param pluginModule The module to register.
     */
    void registerModule(PluginModule pluginModule);

    /**
     * Register a new module to the plugin from a file.
     *
     * @param moduleFile The module to register.
     */
    PluginModule registerModule(File moduleFile) throws IOException, ReflectiveOperationException;

    /**
     * Unregister a module from the plugin.
     *
     * @param pluginModule The module to unregister.
     */
    void unregisterModule(PluginModule pluginModule);

    /**
     * Get a module by its name.
     *
     * @param name The name of the module.
     */
    @Nullable
    PluginModule getModule(String name);

    /**
     * Get all the active modules currently running.
     */
    Collection<PluginModule> getModules();

    /**
     * Enable a specific module.
     *
     * @param pluginModule The module to load.
     */
    void enableModule(PluginModule pluginModule);

    /**
     * Enable all modules with a specific module load time.
     *
     * @param moduleLoadTime The module load time to load modules with.
     */
    void enableModules(ModuleLoadTime moduleLoadTime);

}
