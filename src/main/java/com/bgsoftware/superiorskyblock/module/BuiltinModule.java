package com.bgsoftware.superiorskyblock.module;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLogger;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.core.io.Resources;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Objects;

public abstract class BuiltinModule<T extends IModuleConfiguration> extends PluginModule {

    protected T configuration;

    public BuiltinModule(String moduleName) {
        super(moduleName, "Ome_R");
    }

    @Override
    public final void onEnable(SuperiorSkyblock plugin) {
        onEnable((SuperiorSkyblockPlugin) plugin);
    }

    protected abstract void onEnable(SuperiorSkyblockPlugin plugin);

    @Override
    public final void onReload(SuperiorSkyblock plugin) {
        onReload((SuperiorSkyblockPlugin) plugin);
    }

    protected void onReload(SuperiorSkyblockPlugin plugin) {
        onPluginInit(plugin);
    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        onDisable((SuperiorSkyblockPlugin) plugin);
    }

    protected abstract void onDisable(SuperiorSkyblockPlugin plugin);

    @Override
    public void loadData(SuperiorSkyblock plugin) {
        loadData((SuperiorSkyblockPlugin) plugin);
    }

    protected abstract void loadData(SuperiorSkyblockPlugin plugin);

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblock plugin) {
        return !isEnabled() ? null : getModuleListeners((SuperiorSkyblockPlugin) plugin);
    }

    protected abstract Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin);

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock plugin) {
        return !isEnabled() ? null : getSuperiorCommands((SuperiorSkyblockPlugin) plugin);
    }

    protected abstract SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin);

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock plugin) {
        return !isEnabled() ? null : getSuperiorAdminCommands((SuperiorSkyblockPlugin) plugin);
    }

    protected abstract SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin);

    public final T getConfiguration() {
        return Objects.requireNonNull(this.configuration);
    }

    @Override
    protected void onPluginInit(SuperiorSkyblock plugin) {
        onPluginInit((SuperiorSkyblockPlugin) plugin);
    }

    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        File configFile = new File(getModuleFolder(), "config.yml");
        boolean firstTime = false;

        if (!configFile.exists()) {
            Resources.saveResource("modules/" + getName() + "/config.yml");
            firstTime = true;
        }

        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(configFile);

        boolean commitChanges = onConfigCreate(plugin, config, firstTime);
        if (commitChanges) {
            try {
                config.save(configFile);
            } catch (Exception error) {
                this.logger().e("An error occurred while saving config file for module " + getName() + ":", error);
            }
        }

        try {
            config.syncWithConfig(configFile,
                    Resources.getResource("modules/" + getName() + "/config.yml"),
                    getIgnoredSections());
        } catch (Exception error) {
            this.logger().e("An error occurred while loading config file:", error);
        }


        this.configuration = createConfigFile(config);
    }

    protected ModuleLogger logger() {
        return (ModuleLogger) super.getLogger();
    }

    protected boolean onConfigCreate(SuperiorSkyblockPlugin plugin, CommentedConfiguration config, boolean firstTime) {
        return false;
    }

    public boolean isEnabled() {
        return this.configuration.isEnabled() && isInitialized();
    }

    protected abstract T createConfigFile(CommentedConfiguration config);

    protected String[] getIgnoredSections() {
        return new String[0];
    }

}
