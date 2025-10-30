package com.bgsoftware.superiorskyblock.api.modules;

import java.io.File;

/**
 * All the data needed for a {@link PluginModule} to be initialized correctly.
 */
public interface ModuleInitializeData {

    /**
     * The data folder of the module.
     * Should return plugins/SuperiorSkyblock2/datastore/modules/{module-name}
     */
    File getDataFolder();

    /**
     * The folder of the module.
     * Should return plugins/SuperiorSkyblock2/modules/{module-name}
     */
    File getModuleFolder();

    /**
     * The logger of the module.
     */
    ModuleLogger getLogger();

}
