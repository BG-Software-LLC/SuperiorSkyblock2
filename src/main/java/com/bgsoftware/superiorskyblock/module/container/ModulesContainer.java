package com.bgsoftware.superiorskyblock.module.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.module.ModuleData;

import java.io.File;
import java.util.Collection;

public interface ModulesContainer {

    void registerModule(PluginModule pluginModule, File modulesFolder, File modulesDataFolder);

    void unregisterModule(PluginModule pluginModule);

    @Nullable
    PluginModule getModule(String name);

    Collection<PluginModule> getModules();

    void addModuleData(PluginModule pluginModule, ModuleData moduleData);

}
