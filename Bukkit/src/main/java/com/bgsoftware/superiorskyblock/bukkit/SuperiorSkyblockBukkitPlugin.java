package com.bgsoftware.superiorskyblock.bukkit;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSHandlersFactory;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
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
import com.bgsoftware.superiorskyblock.bukkit.service.BukkitServicesHandler;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.NMSDialogs;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.bukkit.nms.NMSDragonFightChooser;
import com.bgsoftware.superiorskyblock.nms.NMSEntities;
import com.bgsoftware.superiorskyblock.nms.NMSHolograms;
import com.bgsoftware.superiorskyblock.nms.NMSPlayers;
import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.platform.IPlatform;
import com.bgsoftware.superiorskyblock.bukkit.platform.BukkitPlatform;
import com.bgsoftware.superiorskyblock.platform.event.GameEventsDispatcher;
import com.bgsoftware.superiorskyblock.service.BaseServicesHandler;
import org.bstats.bukkit.Metrics;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * The Bukkit entry point of the plugin. It creates the platform-agnostic {@link SuperiorSkyblockPlugin},
 * implements the platform-specific parts of it and forwards it the lifecycle callbacks of the server.
 * <p>
 * This class is also the {@link SuperiorSkyblock} instance that is provided to other plugins, modules
 * and missions - all of its methods are delegated to {@link SuperiorSkyblockPlugin}.
 */
public class SuperiorSkyblockBukkitPlugin extends JavaPlugin implements SuperiorSkyblock {

    /* NMS */
    @Nullable
    private NMSAlgorithms nmsAlgorithms;
    private NMSChunks nmsChunks;
    private Optional<NMSDialogs> nmsDialogs;
    private NMSDragonFight nmsDragonFight;
    private NMSEntities nmsEntities;
    private NMSHolograms nmsHolograms;
    private NMSPlayers nmsPlayers;
    private NMSTags nmsTags;
    private NMSWorld nmsWorld;

    /*
     * The platform must be created before the plugin itself, as the plugin relies on it while
     * it is being constructed.
     */
    private final BukkitPlatform platform = new BukkitPlatform(this);
    private final BukkitServicesHandler servicesHandler = new BukkitServicesHandler(this);

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
        public BaseServicesHandler getServices() {
            return SuperiorSkyblockBukkitPlugin.this.servicesHandler;
        }

        @Override
        public ClassLoader getPluginClassLoader() {
            return SuperiorSkyblockBukkitPlugin.this.getClassLoader();
        }

        @Override
        public String getFileName() {
            return SuperiorSkyblockBukkitPlugin.this.getFile().getName();
        }

        @Override
        protected boolean loadNMSAdapter() {
            try {
                JavaPlugin bukkitPlugin = getBukkitPlugin();
                INMSLoader nmsLoader = NMSHandlersFactory.createNMSLoader(bukkitPlugin, NMSConfiguration.forPlugin(bukkitPlugin));

                nmsAlgorithms = nmsLoader.loadNMSHandler(NMSAlgorithms.class);
                nmsChunks = nmsLoader.loadNMSHandler(NMSChunks.class);
                nmsEntities = nmsLoader.loadNMSHandler(NMSEntities.class);
                nmsHolograms = nmsLoader.loadNMSHandler(NMSHolograms.class);
                nmsPlayers = nmsLoader.loadNMSHandler(NMSPlayers.class);
                nmsTags = nmsLoader.loadNMSHandler(NMSTags.class);
                nmsWorld = nmsLoader.loadNMSHandler(NMSWorld.class);
                nmsDragonFight = new NMSDragonFightChooser(plugin, () -> nmsLoader.loadNMSHandler(NMSDragonFight.class));

                try {
                    nmsDialogs = Optional.of(nmsLoader.loadNMSHandler(NMSDialogs.class));
                } catch (NMSLoadException e) {
                    // Failed to load NMSDialogs
                    nmsDialogs = Optional.empty();
                }

                return true;
            } catch (NMSLoadException error) {
                new ManagerLoadException(error, "The plugin doesn't support your minecraft version.\n" + "Please try a different version.",
                        ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN).printStackTrace();

                return false;
            }
        }

        @Override
        public NMSAlgorithms getNMSAlgorithms() {
            return SuperiorSkyblockBukkitPlugin.this.nmsAlgorithms;
        }

        @Override
        public NMSChunks getNMSChunks() {
            return SuperiorSkyblockBukkitPlugin.this.nmsChunks;
        }

        @Override
        public Optional<NMSDialogs> getNMSDialogs() {
            return SuperiorSkyblockBukkitPlugin.this.nmsDialogs;
        }

        @Override
        public NMSDragonFight getNMSDragonFight() {
            return SuperiorSkyblockBukkitPlugin.this.nmsDragonFight;
        }

        @Override
        public NMSEntities getNMSEntities() {
            return SuperiorSkyblockBukkitPlugin.this.nmsEntities;
        }

        @Override
        public NMSHolograms getNMSHolograms() {
            return SuperiorSkyblockBukkitPlugin.this.nmsHolograms;
        }

        @Override
        public NMSPlayers getNMSPlayers() {
            return SuperiorSkyblockBukkitPlugin.this.nmsPlayers;
        }

        @Override
        public NMSTags getNMSTags() {
            return SuperiorSkyblockBukkitPlugin.this.nmsTags;
        }

        @Override
        public NMSWorld getNMSWorld() {
            return SuperiorSkyblockBukkitPlugin.this.nmsWorld;
        }
    };

    /*
     * JavaPlugin lifecycle
     */

    @Override
    public void onLoad() {
        this.plugin.onLoad();
        new Metrics(this, 4119);
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

    public PluginEventsDispatcher getPluginEventsDispatcher() {
        return this.plugin.getPluginEventsDispatcher();
    }

    public GameEventsDispatcher getGameEventsDispatcher() {
        return this.plugin.getGameEventsDispatcher();
    }

    public BaseServicesHandler getServices() {
        return this.plugin.getServices();
    }

    public NMSAlgorithms getNMSAlgorithms() {
        return this.nmsAlgorithms;
    }

    public NMSChunks getNMSChunks() {
        return this.nmsChunks;
    }

    public Optional<NMSDialogs> getNMSDialogs() {
        return this.nmsDialogs;
    }

    public NMSDragonFight getNMSDragonFight() {
        return this.nmsDragonFight;
    }

    public NMSEntities getNMSEntities() {
        return this.nmsEntities;
    }

    public NMSHolograms getNMSHolograms() {
        return this.nmsHolograms;
    }

    public NMSPlayers getNMSPlayers() {
        return this.nmsPlayers;
    }

    public NMSTags getNMSTags() {
        return this.nmsTags;
    }

    public NMSWorld getNMSWorld() {
        return this.nmsWorld;
    }

    public BukkitPlatform getPlatform() {
        return this.platform;
    }

}
