package com.bgsoftware.superiorskyblock.module.generators;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminAddGenerator;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminClearGenerator;
import com.bgsoftware.superiorskyblock.module.generators.commands.CmdAdminSetGenerator;
import com.bgsoftware.superiorskyblock.module.generators.listeners.GeneratorsListener;
import org.bukkit.event.Listener;

import java.io.File;

public class GeneratorsModule extends BuiltinModule<GeneratorsModule.Configuration> {

    public GeneratorsModule() {
        super("generators");
    }

    @Override
    protected boolean onConfigCreate(SuperiorSkyblockPlugin plugin, CommentedConfiguration config, boolean firstTime) {
        File oldConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (!oldConfigFile.exists())
            return false;

        CommentedConfiguration oldConfig = CommentedConfiguration.loadConfiguration(oldConfigFile);
        boolean updatedConfig = false;

        if (oldConfig.contains("generators")) {
            config.set("enabled", oldConfig.getBoolean("generators"));
        }

        return updatedConfig;
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
        return new Listener[]{new GeneratorsListener(plugin)};
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdAdminAddGenerator(), new CmdAdminClearGenerator(), new CmdAdminSetGenerator()};
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config);
    }

    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final boolean matchGeneratorWorld;

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled");
            this.matchGeneratorWorld = config.getBoolean("match-generator-world");
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        public boolean isMatchGeneratorWorld() {
            return this.matchGeneratorWorld;
        }

    }

}
