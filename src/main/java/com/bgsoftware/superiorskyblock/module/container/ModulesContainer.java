package com.bgsoftware.superiorskyblock.module.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.module.ModuleData;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;

public interface ModulesContainer {

    void registerModule(PluginModule pluginModule, File modulesFolder, File modulesDataFolder, SuperiorSkyblockPlugin plugin);

    void unregisterModule(PluginModule pluginModule, SuperiorSkyblockPlugin plugin);

    @Nullable
    PluginModule getModule(String name);

    Collection<PluginModule> getModules();

    void addModuleData(PluginModule pluginModule, ModuleData moduleData);

}
