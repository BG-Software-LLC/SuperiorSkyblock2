package com.bgsoftware.superiorskyblock.module.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.module.ModuleData;
import com.google.common.base.Preconditions;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DefaultModulesContainer implements ModulesContainer {

    private final Map<String, PluginModule> modulesMap = new HashMap<>();
    private final Map<PluginModule, ModuleData> modulesData = new HashMap<>();

    @Override
    public void registerModule(PluginModule pluginModule, File modulesFolder, SuperiorSkyblockPlugin plugin) {
        String moduleName = pluginModule.getName().toLowerCase();

        Preconditions.checkState(!modulesMap.containsKey(moduleName), "PluginModule with the name " + moduleName + " already exists.");

        File dataFolder = new File(modulesFolder, pluginModule.getName());

        pluginModule.initModule(plugin, dataFolder);

        modulesMap.put(moduleName, pluginModule);
    }

    @Override
    public void unregisterModule(PluginModule pluginModule, SuperiorSkyblockPlugin plugin) {
        String moduleName = pluginModule.getName().toLowerCase();

        Preconditions.checkState(modulesMap.containsKey(moduleName), "PluginModule with the name " + moduleName + " is not registered in the plugin anymore.");

        SuperiorSkyblockPlugin.log("&cDisabling the module " + pluginModule.getName() + "...");

        pluginModule.onDisable(plugin);

        ModuleData moduleData = modulesData.remove(pluginModule);

        if (moduleData != null) {
            if (moduleData.getListeners() != null)
                Arrays.stream(moduleData.getListeners()).forEach(HandlerList::unregisterAll);

            if (moduleData.getCommands() != null)
                Arrays.stream(moduleData.getCommands()).forEach(plugin.getCommands()::unregisterCommand);

            if (moduleData.getAdminCommands() != null)
                Arrays.stream(moduleData.getAdminCommands()).forEach(plugin.getCommands()::unregisterAdminCommand);
        }

        pluginModule.disableModule();

        modulesMap.remove(moduleName);
    }

    @Nullable
    @Override
    public PluginModule getModule(String name) {
        return this.modulesMap.get(name.toLowerCase());
    }

    @Override
    public Collection<PluginModule> getModules() {
        return Collections.unmodifiableCollection(new ArrayList<>(modulesMap.values()));
    }

    @Override
    public void addModuleData(PluginModule pluginModule, ModuleData moduleData) {
        this.modulesData.put(pluginModule, moduleData);
    }

}
