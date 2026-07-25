package com.bgsoftware.superiorskyblock;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.handlers.CommandsManager;
import com.bgsoftware.superiorskyblock.api.handlers.FactoriesManager;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.handlers.MissionsManager;
import com.bgsoftware.superiorskyblock.api.handlers.ModulesManager;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.handlers.ProvidersManager;
import com.bgsoftware.superiorskyblock.api.handlers.RolesManager;
import com.bgsoftware.superiorskyblock.api.handlers.SchematicManager;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.handlers.UpgradesManager;
import com.bgsoftware.superiorskyblock.api.platform.IEventsDispatcher;
import com.bgsoftware.superiorskyblock.api.scripts.IScriptEngine;
import com.bgsoftware.superiorskyblock.platform.IPlatform;
import com.bgsoftware.superiorskyblock.platform.bukkit.BukkitPlatform;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The Bukkit entry point of the plugin. It creates the platform-agnostic {@link SuperiorSkyblockPlugin},
 * implements the platform-specific parts of it and forwards it the lifecycle callbacks of the server.
 * <p>
 * This class is also the {@link SuperiorSkyblock} instance that is provided to other plugins, modules
 * and missions - all of its methods are delegated to {@link SuperiorSkyblockPlugin}.
 */
public class SuperiorSkyblockBukkitPlugin extends JavaPlugin implements SuperiorSkyblock {

    /*
     * The platform must be created before the plugin itself, as the plugin relies on it while
     * it is being constructed.
     */
    private final IPlatform platform = new BukkitPlatform(this);
    private final SuperiorSkyblockPlugin plugin = new SuperiorSkyblockPlugin() {

        @Override
        public IPlatform getPlatform() {
            return SuperiorSkyblockBukkitPlugin.this.platform;
        }

        @Override
        public JavaPlugin getBukkitPlugin() {
            return SuperiorSkyblockBukkitPlugin.this;
        }

        @Override
        public SuperiorSkyblock getApi() {
            return SuperiorSkyblockBukkitPlugin.this;
        }

        @Override
        public ClassLoader getPluginClassLoader() {
            return SuperiorSkyblockBukkitPlugin.this.getClassLoader();
        }

        @Override
        public String getFileName() {
            return SuperiorSkyblockBukkitPlugin.this.getFile().getName();
        }

    };

    /*
     * JavaPlugin lifecycle
     */

    @Override
    public void onLoad() {
        this.plugin.onLoad();
    }

    @Override
    public void onEnable() {
        this.plugin.onEnable();
    }

    @Override
    public void onDisable() {
        this.plugin.onDisable();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return this.plugin.getDefaultWorldGenerator(worldName, id);
    }

    /*
     * SuperiorSkyblock
     */

    @Override
    public GridManager getGrid() {
        return this.plugin.getGrid();
    }

    @Override
    public StackedBlocksManager getStackedBlocks() {
        return this.plugin.getStackedBlocks();
    }

    @Override
    public BlockValuesManager getBlockValues() {
        return this.plugin.getBlockValues();
    }

    @Override
    public SchematicManager getSchematics() {
        return this.plugin.getSchematics();
    }

    @Override
    public PlayersManager getPlayers() {
        return this.plugin.getPlayers();
    }

    @Override
    public RolesManager getRoles() {
        return this.plugin.getRoles();
    }

    @Override
    public MissionsManager getMissions() {
        return this.plugin.getMissions();
    }

    @Override
    public MenusManager getMenus() {
        return this.plugin.getMenus();
    }

    @Override
    public KeysManager getKeys() {
        return this.plugin.getKeys();
    }

    @Override
    public ProvidersManager getProviders() {
        return this.plugin.getProviders();
    }

    @Override
    public UpgradesManager getUpgrades() {
        return this.plugin.getUpgrades();
    }

    @Override
    public CommandsManager getCommands() {
        return this.plugin.getCommands();
    }

    @Override
    public SettingsManager getSettings() {
        return this.plugin.getSettings();
    }

    @Override
    public FactoriesManager getFactory() {
        return this.plugin.getFactory();
    }

    @Override
    public ModulesManager getModules() {
        return this.plugin.getModules();
    }

    @Override
    public IScriptEngine getScriptEngine() {
        return this.plugin.getScriptEngine();
    }

    @Override
    public void setScriptEngine(@Nullable IScriptEngine scriptEngine) {
        this.plugin.setScriptEngine(scriptEngine);
    }

    @Nullable
    @Override
    public IEventsDispatcher getEventsDispatcher() {
        return this.plugin.getEventsDispatcher();
    }

    @Override
    public void setEventsDispatcher(@Nullable IEventsDispatcher eventsDispatcher) {
        this.plugin.setEventsDispatcher(eventsDispatcher);
    }

}
