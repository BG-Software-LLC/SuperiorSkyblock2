package com.bgsoftware.superiorskyblock.modules.generators;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.modules.BuiltinModule;
import com.bgsoftware.superiorskyblock.modules.generators.listeners.GeneratorsListener;
import org.bukkit.Bukkit;

import java.io.File;

public final class GeneratorsModule extends BuiltinModule {

    public boolean enabled = true;

    public GeneratorsModule(){
        super("Generators");
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new GeneratorsListener(plugin, this), plugin);
    }

    @Override
    public void onDisable() {

    }

    @Override
    protected void onPluginInit() {
        super.onPluginInit();

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(configFile);

        if(config.contains("generators")){
            super.config.set("enabled", config.getBoolean("generators"));
            config.set("generators", null);

            File moduleConfigFile = new File(getDataFolder(), "config.yml");

            try{
                super.config.save(moduleConfigFile);
                config.save(configFile);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }

    protected void updateConfig(){
        enabled = config.getBoolean("enabled");
    }

}
