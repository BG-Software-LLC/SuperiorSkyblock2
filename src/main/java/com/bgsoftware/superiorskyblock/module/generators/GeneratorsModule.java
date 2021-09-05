package com.bgsoftware.superiorskyblock.module.generators;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminAddGenerator;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminClearGenerator;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminSetGenerator;
import com.bgsoftware.superiorskyblock.module.generators.listeners.GeneratorsListener;
import org.bukkit.event.Listener;

import java.io.File;

public final class GeneratorsModule extends BuiltinModule {

    private boolean enabled = true;

    public GeneratorsModule(){
        super("generators");
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
    }

    @Override
    public void onDisable(SuperiorSkyblockPlugin plugin) {
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new Listener[] {new GeneratorsListener(plugin, this)};
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new SuperiorCommand[]{new CmdAdminAddGenerator(), new CmdAdminClearGenerator(), new CmdAdminSetGenerator()};
    }

    @Override
    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        super.onPluginInit(plugin);

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

    @Override
    public boolean isEnabled() {
        return enabled && isInitialized();
    }

    @Override
    protected void updateConfig(SuperiorSkyblockPlugin plugin){
        enabled = config.getBoolean("enabled");
    }

}
