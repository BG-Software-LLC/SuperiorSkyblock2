package com.bgsoftware.superiorskyblock.module.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.module.ModuleData;
import com.google.common.base.Preconditions;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DefaultModulesContainer implements ModulesContainer {

    private final Map<String, PluginModule> modulesMap = new HashMap<>();
    private final Map<PluginModule, ModuleData> modulesData = new HashMap<>();

    private final SuperiorSkyblockPlugin plugin;

    public DefaultModulesContainer(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerModule(PluginModule pluginModule, File modulesFolder, File modulesDataFolder) {
        String moduleName = pluginModule.getName().toLowerCase(Locale.ENGLISH);

        Preconditions.checkState(!modulesMap.containsKey(moduleName), "PluginModule with the name " + moduleName + " already exists.");

        File dataFolder = new File(modulesDataFolder, pluginModule.getName());
        File moduleFolder = new File(modulesFolder, pluginModule.getName());

        try {
            pluginModule.initModule(plugin, moduleFolder, dataFolder);
        } catch (Throwable error) {
            Log.error("An unexpected error occurred while initializing the module ", pluginModule.getName(), ".");
            Log.error(error, "Contact ", pluginModule.getAuthor(), " regarding this, this has nothing to do with the plugin.");
            return;
        }

        modulesMap.put(moduleName, pluginModule);
    }

    @Override
    public void unregisterModule(PluginModule pluginModule) {
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

        modulesMap.remove(pluginModule.getName().toLowerCase(Locale.ENGLISH));
    }

    @Nullable
    @Override
    public PluginModule getModule(String name) {
        return this.modulesMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Collection<PluginModule> getModules() {
        return new SequentialListBuilder<PluginModule>().build(modulesMap.values());
    }

    @Override
    public void addModuleData(PluginModule pluginModule, ModuleData moduleData) {
        this.modulesData.put(pluginModule, moduleData);
    }

}
