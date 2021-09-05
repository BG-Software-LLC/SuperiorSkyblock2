package com.bgsoftware.superiorskyblock.module;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.event.Listener;

import java.io.File;

public abstract class BuiltinModule extends PluginModule {

    protected CommentedConfiguration config = null;

    public BuiltinModule(String moduleName){
        super(moduleName, "Ome_R");
    }

    @Override
    public final void onEnable(SuperiorSkyblock plugin) {
        onEnable((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public final void onReload(SuperiorSkyblock plugin) {
        onPluginInit(plugin);
    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        onDisable((SuperiorSkyblockPlugin) plugin);
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
                    FileUtils.getResource("modules/" + getName() + "/config.yml"),
                    getIgnoredSections());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        updateConfig(plugin);
    }

    public File createConfig(){
        File configFile = new File(getDataFolder(), "config.yml");

        if(!configFile.exists())
            FileUtils.saveResource("modules/" + getName() + "/config.yml");

        return configFile;
    }

    public abstract void onEnable(SuperiorSkyblockPlugin plugin);

    public abstract void onDisable(SuperiorSkyblockPlugin plugin);

    public abstract Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin);

    public abstract SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin);

    public abstract SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin);

    public abstract boolean isEnabled();

    protected abstract void updateConfig(SuperiorSkyblockPlugin plugin);

    protected String[] getIgnoredSections(){
        return new String[0];
    }

}
