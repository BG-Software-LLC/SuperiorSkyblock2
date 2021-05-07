package com.bgsoftware.superiorskyblock.modules;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.utils.FileUtils;

import java.io.File;

public abstract class BuiltinModule extends PluginModule {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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
        onPluginInit();
    }

    @Override
    protected void onPluginInit() {
        File configFile = createConfig();
        config = CommentedConfiguration.loadConfiguration(configFile);

        try {
            config.syncWithConfig(configFile, FileUtils.getResource("modules/" + getName() + "/config.yml"));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        updateConfig();
    }

    public File createConfig(){
        File configFile = new File(getDataFolder(), "config.yml");

        if(!configFile.exists())
            FileUtils.saveResource("modules/" + getName() + "/config.yml");

        return configFile;
    }

    public abstract void onEnable(SuperiorSkyblockPlugin plugin);

    public abstract void onDisable();

    public abstract boolean isEnabled();

    protected abstract void updateConfig();

}
