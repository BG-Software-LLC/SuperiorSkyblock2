package com.bgsoftware.superiorskyblock.module.generators;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminAddGenerator;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminClearGenerator;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminSetGenerator;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.generators.listeners.GeneratorsListener;
import org.bukkit.event.Listener;

import java.io.File;

public class GeneratorsModule extends BuiltinModule {

    private boolean enabled = true;
    private boolean matchGeneratorWorld = true;

    public GeneratorsModule() {
        super("generators");
    }

    @Override
    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        super.onPluginInit(plugin);

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(configFile);

        if (config.contains("generators")) {
            super.config.set("enabled", config.getBoolean("generators"));
            config.set("generators", null);

            File moduleConfigFile = new File(getModuleFolder(), "config.yml");

            try {
                super.config.save(moduleConfigFile);
                config.save(configFile);
            } catch (Exception error) {
                Log.entering("GeneratorsModule", "onPluginInit", "ENTER");
                Log.error(error, "An error occurred while saving config file:");
            }
        }

    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public void onDisable(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public void loadData(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new Listener[]{new GeneratorsListener(plugin, this)};
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
    public boolean isEnabled() {
        return enabled && isInitialized();
    }

    @Override
    protected void updateConfig(SuperiorSkyblockPlugin plugin) {
        enabled = config.getBoolean("enabled");
        matchGeneratorWorld = config.getBoolean("match-generator-world");
    }

    public boolean isMatchGeneratorWorld() {
        return matchGeneratorWorld;
    }

}
