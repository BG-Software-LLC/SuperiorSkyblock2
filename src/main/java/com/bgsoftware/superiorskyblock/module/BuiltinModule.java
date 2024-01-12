package com.bgsoftware.superiorskyblock.module;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.core.io.Resources;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.event.Listener;

import java.io.File;

public abstract class BuiltinModule extends PluginModule {

    protected CommentedConfiguration config = null;

    public BuiltinModule(String moduleName) {
        super(moduleName, "Ome_R");
    }

    @Override
    public final void onEnable(SuperiorSkyblock plugin) {
        onEnable((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public final void onReload(SuperiorSkyblock plugin) {
        onReload((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        onDisable((SuperiorSkyblockPlugin) plugin);
    }

    public void loadData(SuperiorSkyblock plugin) {
        loadData((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblock plugin) {
        return getModuleListeners((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock plugin) {
        return getSuperiorCommands((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock plugin) {
        return getSuperiorAdminCommands((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    protected void onPluginInit(SuperiorSkyblock plugin) {
        onPluginInit((SuperiorSkyblockPlugin) plugin);
    }

    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        File configFile = createConfig();
        config = CommentedConfiguration.loadConfiguration(configFile);

        try {
            config.syncWithConfig(configFile,
                    Resources.getResource("modules/" + getName() + "/config.yml"),
                    getIgnoredSections());
        } catch (Exception error) {
            Log.entering(getClass().getName(), "onPluginInit", "ENTER", "");
            Log.error(error, "An error occurred while loading config file:");
        }

        updateConfig(plugin);
    }

    public File createConfig() {
        File configFile = new File(getModuleFolder(), "config.yml");

        if (!configFile.exists())
            Resources.saveResource("modules/" + getName() + "/config.yml");

        return configFile;
    }

    public abstract void onEnable(SuperiorSkyblockPlugin plugin);

    public void onReload(SuperiorSkyblockPlugin plugin) {
        onPluginInit(plugin);
    }

    public abstract void onDisable(SuperiorSkyblockPlugin plugin);

    public abstract void loadData(SuperiorSkyblockPlugin plugin);

    public abstract Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin);

    public abstract SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin);

    public abstract SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin);

    public abstract boolean isEnabled();

    protected abstract void updateConfig(SuperiorSkyblockPlugin plugin);

    protected String[] getIgnoredSections() {
        return new String[0];
    }

}
