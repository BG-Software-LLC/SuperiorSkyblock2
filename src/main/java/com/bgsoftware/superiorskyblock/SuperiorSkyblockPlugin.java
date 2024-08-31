package com.bgsoftware.superiorskyblock;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.dependencies.DependenciesManager;
import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSHandlersFactory;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.common.updater.Updater;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLoadTime;
import com.bgsoftware.superiorskyblock.api.scripts.IScriptEngine;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandsManagerImpl;
import com.bgsoftware.superiorskyblock.commands.admin.AdminCommandsMap;
import com.bgsoftware.superiorskyblock.commands.player.PlayerCommandsMap;
import com.bgsoftware.superiorskyblock.config.SettingsManagerImpl;
import com.bgsoftware.superiorskyblock.core.PluginLoadingStage;
import com.bgsoftware.superiorskyblock.core.PluginReloadReason;
import com.bgsoftware.superiorskyblock.core.database.DataManager;
import com.bgsoftware.superiorskyblock.core.database.transaction.DatabaseTransactionsExecutor;
import com.bgsoftware.superiorskyblock.core.engine.EnginesFactory;
import com.bgsoftware.superiorskyblock.core.engine.NashornEngineDownloader;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.factory.FactoriesManagerImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.GlowEnchantment;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.key.KeysManagerImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.MenusManagerImpl;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.stackedblocks.StackedBlocksManagerImpl;
import com.bgsoftware.superiorskyblock.core.stackedblocks.container.DefaultStackedBlocksContainer;
import com.bgsoftware.superiorskyblock.core.task.CalcTask;
import com.bgsoftware.superiorskyblock.core.task.ShutdownTask;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.values.BlockValuesManagerImpl;
import com.bgsoftware.superiorskyblock.core.values.container.BlockValuesContainer;
import com.bgsoftware.superiorskyblock.external.ProvidersManagerImpl;
import com.bgsoftware.superiorskyblock.island.GridManagerImpl;
import com.bgsoftware.superiorskyblock.island.container.DefaultIslandsContainer;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.preview.DefaultIslandPreviews;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.purge.DefaultIslandsPurger;
import com.bgsoftware.superiorskyblock.island.role.RolesManagerImpl;
import com.bgsoftware.superiorskyblock.island.role.container.DefaultRolesContainer;
import com.bgsoftware.superiorskyblock.island.top.SortingComparators;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.island.upgrade.UpgradesManagerImpl;
import com.bgsoftware.superiorskyblock.island.upgrade.container.DefaultUpgradesContainer;
import com.bgsoftware.superiorskyblock.island.upgrade.loaders.PlaceholdersUpgradeCostLoader;
import com.bgsoftware.superiorskyblock.island.upgrade.loaders.VaultUpgradeCostLoader;
import com.bgsoftware.superiorskyblock.listener.BukkitListeners;
import com.bgsoftware.superiorskyblock.mission.MissionsManagerImpl;
import com.bgsoftware.superiorskyblock.mission.container.DefaultMissionsContainer;
import com.bgsoftware.superiorskyblock.module.ModulesManagerImpl;
import com.bgsoftware.superiorskyblock.module.container.DefaultModulesContainer;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFightChooser;
import com.bgsoftware.superiorskyblock.nms.NMSEntities;
import com.bgsoftware.superiorskyblock.nms.NMSHolograms;
import com.bgsoftware.superiorskyblock.nms.NMSPlayers;
import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.player.PlayersManagerImpl;
import com.bgsoftware.superiorskyblock.player.container.DefaultPlayersContainer;
import com.bgsoftware.superiorskyblock.player.respawn.RespawnActions;
import com.bgsoftware.superiorskyblock.service.ServicesHandler;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.bgsoftware.superiorskyblock.world.WorldGenerator;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.bgsoftware.superiorskyblock.world.schematic.SchematicsManagerImpl;
import com.bgsoftware.superiorskyblock.world.schematic.container.DefaultSchematicsContainer;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Optional;

public class SuperiorSkyblockPlugin extends JavaPlugin implements SuperiorSkyblock {

    private static SuperiorSkyblockPlugin plugin;

    /* Managers */
    private final DataManager dataHandler = new DataManager(this);
    private final FactoriesManagerImpl factoriesHandler = new FactoriesManagerImpl();
    private final GridManagerImpl gridHandler = new GridManagerImpl(this, new DefaultIslandsPurger(), new DefaultIslandPreviews());
    private final StackedBlocksManagerImpl stackedBlocksHandler = new StackedBlocksManagerImpl(this, new DefaultStackedBlocksContainer());
    private final BlockValuesManagerImpl blockValuesHandler = new BlockValuesManagerImpl(this, new BlockValuesContainer(), new BlockValuesContainer());
    private final SchematicsManagerImpl schematicsHandler = new SchematicsManagerImpl(this, new DefaultSchematicsContainer());
    private final PlayersManagerImpl playersHandler = new PlayersManagerImpl(this);
    private final RolesManagerImpl rolesHandler = new RolesManagerImpl(this, new DefaultRolesContainer());
    private final MissionsManagerImpl missionsHandler = new MissionsManagerImpl(this, new DefaultMissionsContainer());
    private final MenusManagerImpl menusHandler = new MenusManagerImpl(this);
    private final KeysManagerImpl keysHandler = new KeysManagerImpl(this);
    private final ProvidersManagerImpl providersHandler = new ProvidersManagerImpl(this);
    private final UpgradesManagerImpl upgradesHandler = new UpgradesManagerImpl(this, new DefaultUpgradesContainer());
    private final CommandsManagerImpl commandsHandler = new CommandsManagerImpl(this, new PlayerCommandsMap(this), new AdminCommandsMap(this));
    private final ModulesManagerImpl modulesHandler = new ModulesManagerImpl(this, new DefaultModulesContainer(this));
    private final ServicesHandler servicesHandler = new ServicesHandler(this);
    private final SettingsManagerImpl settingsHandler = new SettingsManagerImpl(this);

    /* Global handlers */
    private final Updater updater = new Updater(this, "superiorskyblock2");
    private final EventsBus eventsBus = new EventsBus(this);
    private final BukkitListeners bukkitListeners = new BukkitListeners(this);
    private IScriptEngine scriptEngine = EnginesFactory.createDefaultEngine();
    @Nullable
    private ChunkGenerator worldGenerator = null;

    /* NMS */
    @Nullable
    private NMSAlgorithms nmsAlgorithms;
    private NMSChunks nmsChunks;
    private NMSDragonFight nmsDragonFight;
    private NMSEntities nmsEntities;
    private NMSHolograms nmsHolograms;
    private NMSPlayers nmsPlayers;
    private NMSTags nmsTags;
    private NMSWorld nmsWorld;

    private PluginLoadingStage loadingStage = PluginLoadingStage.START;

    public static SuperiorSkyblockPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onLoad() {
        plugin = this;

        DependenciesManager.inject(this);

        bukkitListeners.registerListenerFailureFilter();

        try {
            SuperiorSkyblockAPI.setPluginInstance(this);
        } catch (UnsupportedOperationException error) {
            Log.error("The API instance was already initialized. This can be caused by a reload or another plugin initializing it.");
            return;
        }

        loadingStage = PluginLoadingStage.API_INITIALIZED;

        String serverVersion = Bukkit.getVersion();
        if (serverVersion.toLowerCase(Locale.ENGLISH).contains("arclight")) {
            Log.error("The plugin does not support this server software: " + serverVersion);
            return;
        }

        loadingStage = PluginLoadingStage.SUPPORTED_SERVER_SOFTWARE;

        if (!loadNMSAdapter()) {
            return;
        }

        loadingStage = PluginLoadingStage.NMS_INITIALIZED;

        Runtime.getRuntime().addShutdownHook(new ShutdownTask(this));

        IslandPrivileges.registerPrivileges();
        SortingTypes.registerSortingTypes();
        IslandFlags.registerFlags();
        RespawnActions.registerActions();
        Dimensions.registerDimensions();

        try {
            SortingComparators.initializeTopIslandMembersSorting();
        } catch (IllegalArgumentException error) {
            Log.error("The TopIslandMembersSorting was already initialized. This can be caused by a reload or another plugin initializing it.");
            return;
        }

        this.servicesHandler.loadDefaultServices(this);

        new Metrics(this, 4119);

        loadingStage = PluginLoadingStage.LOADED;

        DatabaseTransactionsExecutor.init();
    }

    @Override
    public void onEnable() {
        try {
            if (loadingStage != PluginLoadingStage.LOADED) {
                ManagerLoadException.handle(new ManagerLoadException("Failed to load " + getDescription().getName() + ".\n" +
                        "Failed on " + loadingStage.next() + "\n\n" +
                        "Shutting down the server...", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN));
                return;
            }

            loadingStage = PluginLoadingStage.START_ENABLE;

            BukkitExecutor.init(this);

            loadUpgradeCostLoaders();

            GlowEnchantment.registerGlowEnchantment(this);

            try {
                settingsHandler.loadData();
            } catch (ManagerLoadException ex) {
                if (!ManagerLoadException.handle(ex)) {
                    return;
                }
            }

            loadingStage = PluginLoadingStage.SETTINGS_INITIALIZED;

            modulesHandler.loadData();

            loadingStage = PluginLoadingStage.MODULES_INITIALIZED;

            commandsHandler.loadData();

            loadingStage = PluginLoadingStage.COMMANDS_INITIALIZED;

            modulesHandler.runModuleLifecycle(ModuleLoadTime.PLUGIN_INITIALIZE, false);

            EventsBus.PluginInitializeResult eventResult = eventsBus.callPluginInitializeEvent(this);
            this.playersHandler.setPlayersContainer(Optional.ofNullable(eventResult.getPlayersContainer()).orElse(new DefaultPlayersContainer()));
            this.gridHandler.setIslandsContainer(Optional.ofNullable(eventResult.getIslandsContainer()).orElse(new DefaultIslandsContainer(this)));

            modulesHandler.runModuleLifecycle(ModuleLoadTime.BEFORE_WORLD_CREATION, false);

            try {
                providersHandler.getWorldsProvider().prepareWorlds();
            } catch (RuntimeException ex) {
                ManagerLoadException handlerError = new ManagerLoadException(ex, ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
                Log.error(handlerError, "An error occurred while preparing worlds:");
                Bukkit.shutdown();
                return;
            }

            loadingStage = PluginLoadingStage.WORLDS_PREPARED;

            modulesHandler.runModuleLifecycle(ModuleLoadTime.NORMAL, false);

            try {
                reloadPlugin(PluginReloadReason.STARTUP);
            } catch (ManagerLoadException error) {
                ManagerLoadException.handle(error);
                return;
            }

            loadingStage = PluginLoadingStage.MANAGERS_INITIALIZED;

            try {
                bukkitListeners.registerListeners();
            } catch (RuntimeException ex) {
                ManagerLoadException handlerError = new ManagerLoadException("Cannot load plugin due to a missing event: " + ex.getMessage() + " - contact @Ome_R!", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
                Log.error(handlerError, "An error occurred while registering listeners:");
                Bukkit.shutdown();
                return;
            }

            loadingStage = PluginLoadingStage.EVENTS_INITIALIZED;

            ChunksProvider.start();

            loadingStage = PluginLoadingStage.CHUNKS_PROVIDER_INITIALIZED;

            if (updater.isOutdated()) {
                Log.info("");
                Log.info("A new version is available (v", updater.getLatestVersion(), ")!");
                Log.info("Version's description: \"", updater.getVersionDescription(), "\"");
                Log.info("");
            }

            // Calculate the maximum amount of islands that fit into the world.
            if (calculateMaxPossibleIslands() < 1000) {
                Log.warn("It seems like you configured your max-world-size in server.properties to be a small number (", nmsAlgorithms.getMaxWorldSize(), ").");
                Log.warn("This can lead to weird behaviors when new islands are generated beyond this limit.");
                Log.warn("Increase the value to for better experience (Default: 29999984)");
            }

            BukkitExecutor.sync(() -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SuperiorPlayer superiorPlayer = playersHandler.getSuperiorPlayer(player);
                    superiorPlayer.updateLastTimeStatus();
                    Island island = gridHandler.getIslandAt(superiorPlayer.getLocation());
                    Island playerIsland = superiorPlayer.getIsland();

                    if (superiorPlayer.hasIslandFlyEnabled()) {
                        if (island != null && island.hasPermission(superiorPlayer, IslandPrivileges.FLY)) {
                            player.setAllowFlight(true);
                            player.setFlying(true);
                        } else {
                            superiorPlayer.toggleIslandFly();
                        }
                    }

                    if (playerIsland != null)
                        playerIsland.setCurrentlyActive(true);

                    if (island != null)
                        island.setPlayerInside(superiorPlayer, true);
                }
            }, 1L);

            eventsBus.callPluginInitializedEvent(this);

            loadingStage = PluginLoadingStage.ENABLED;
        } catch (Throwable error) {
            Log.error(error, "An unexpected error occurred while enabling the plugin:");
            Bukkit.shutdown();
        }
    }

    @Override
    public void onDisable() {
        try {
            if (loadingStage.isAtLeast(PluginLoadingStage.START_ENABLE))
                BukkitExecutor.prepareDisable();

            if (loadingStage.isAtLeast(PluginLoadingStage.CHUNKS_PROVIDER_INITIALIZED))
                ChunksProvider.stop();

            if (loadingStage.isAtLeast(PluginLoadingStage.MANAGERS_INITIALIZED)) {
                dataHandler.saveDatabase(false);
                gridHandler.disablePlugin();

                for (Island island : gridHandler.getIslandsToPurge())
                    island.disbandIsland();

                playersHandler.savePlayers();
                gridHandler.saveIslands();
                stackedBlocksHandler.saveStackedBlocks();
            }

            if (loadingStage.isAtLeast(PluginLoadingStage.MODULES_INITIALIZED)) {
                modulesHandler.getModules().forEach(modulesHandler::unregisterModule);
            }

            // Shutdown task is running from another thread, causing closing of inventories to cause errors.
            // This check should prevent it.
            if (Bukkit.isPrimaryThread()) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    SuperiorPlayer superiorPlayer = playersHandler.getSuperiorPlayer(player);
                    player.closeInventory();
                    superiorPlayer.updateWorldBorder(null);
                    if (superiorPlayer.hasIslandFlyEnabled()) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                });
            }
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while disabling the plugin:");
        } finally {
            if (loadingStage.isAtLeast(PluginLoadingStage.MANAGERS_INITIALIZED)) {
                Log.info("Shutting down calculation task...");
                CalcTask.cancelTask();
            }

            if (loadingStage.isAtLeast(PluginLoadingStage.NMS_INITIALIZED))
                nmsChunks.shutdown();

            if (loadingStage.isAtLeast(PluginLoadingStage.START_ENABLE)) {
                Log.info("Shutting down executor");
                BukkitExecutor.close();
                Log.info("Shutting down database executor");
                DatabaseTransactionsExecutor.stop();
            }

            if (loadingStage.isAtLeast(PluginLoadingStage.MANAGERS_INITIALIZED)) {
                Log.info("Closing database. This may hang the server. Do not shut it down, or data may get lost.");
                dataHandler.closeConnection();
            }
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return WorldGenerator.getWorldGenerator(settingsHandler.getWorlds().getDefaultWorldDimension());
    }

    public Updater getUpdater() {
        return updater;
    }

    public ClassLoader getPluginClassLoader() {
        return super.getClassLoader();
    }

    private boolean loadNMSAdapter() {
        try {
            INMSLoader nmsLoader = NMSHandlersFactory.createNMSLoader(this, NMSConfiguration.forPlugin(this));

            this.nmsAlgorithms = nmsLoader.loadNMSHandler(NMSAlgorithms.class);
            this.nmsChunks = nmsLoader.loadNMSHandler(NMSChunks.class);
            this.nmsEntities = nmsLoader.loadNMSHandler(NMSEntities.class);
            this.nmsHolograms = nmsLoader.loadNMSHandler(NMSHolograms.class);
            this.nmsPlayers = nmsLoader.loadNMSHandler(NMSPlayers.class);
            this.nmsTags = nmsLoader.loadNMSHandler(NMSTags.class);
            this.nmsWorld = nmsLoader.loadNMSHandler(NMSWorld.class);
            this.nmsDragonFight = new NMSDragonFightChooser(plugin, () -> nmsLoader.loadNMSHandler(NMSDragonFight.class));

            return true;
        } catch (NMSLoadException error) {
            new ManagerLoadException(error, "The plugin doesn't support your minecraft version.\n" + "Please try a different version.",
                    ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN).printStackTrace();

            return false;
        }
    }

    private boolean checkScriptEngine() {
        return testScriptEngine() || (NashornEngineDownloader.downloadEngine(this) && testScriptEngine());
    }

    private boolean testScriptEngine() {
        try {
            scriptEngine.eval("1+1");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void reloadPlugin(PluginReloadReason reloadReason) throws ManagerLoadException {
        ItemSkulls.readTextures(this);

        if (reloadReason == PluginReloadReason.COMMAND) {
            // The reload was requested by a command. We want to reload the commands, settings and call the
            // module lifecycles that are not called regularly. If the reload was due to a startup, then
            // all of that is called already in the onEnable callback of the plugin.
            commandsHandler.loadData();

            settingsHandler.loadData();

            modulesHandler.runModuleLifecycle(ModuleLoadTime.PLUGIN_INITIALIZE, true);
            modulesHandler.runModuleLifecycle(ModuleLoadTime.BEFORE_WORLD_CREATION, true);
            modulesHandler.runModuleLifecycle(ModuleLoadTime.NORMAL, true);
        }

        if (!checkScriptEngine()) {
            throw new ManagerLoadException("It seems like the script engine of the plugin is corrupted.\n" + "This may occur by one of the following reasons:\n" + "1. You have a module/plugin that sets a custom script that doesn't work well.\n" + "2. You're using Java 16 without installing an external module engine.\n" + "If that's the case, check out the following link:\n" + "https://github.com/BG-Software-LLC/SuperiorSkyblock2-NashornEngine", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        blockValuesHandler.loadData();
        upgradesHandler.loadData();
        rolesHandler.loadData();

        Message.reload();

        if (reloadReason == PluginReloadReason.STARTUP) {
            playersHandler.loadData();
            gridHandler.loadData();
        } else {
            BukkitExecutor.sync(gridHandler::updateSpawn, 1L);
            gridHandler.syncUpgrades();
        }

        schematicsHandler.loadData();
        providersHandler.loadData();
        menusHandler.loadData();
        missionsHandler.loadData();

        if (reloadReason == PluginReloadReason.STARTUP) {
            dataHandler.loadData();
            stackedBlocksHandler.loadData();
        }

        modulesHandler.runModuleLifecycle(ModuleLoadTime.AFTER_MODULE_DATA_LOAD, reloadReason == PluginReloadReason.COMMAND);

        BukkitExecutor.sync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                SuperiorPlayer superiorPlayer = playersHandler.getSuperiorPlayer(player);
                Island island = gridHandler.getIslandAt(player.getLocation());
                superiorPlayer.updateWorldBorder(island);
                if (island != null) island.applyEffects(superiorPlayer);
            }
        });

        CalcTask.startTask();

        modulesHandler.runModuleLifecycle(ModuleLoadTime.AFTER_HANDLERS_LOADING, reloadReason == PluginReloadReason.COMMAND);

        if (reloadReason == PluginReloadReason.STARTUP) {
            modulesHandler.loadModulesData(this);
        }
    }

    @Override
    public GridManagerImpl getGrid() {
        return gridHandler;
    }

    @Override
    public StackedBlocksManagerImpl getStackedBlocks() {
        return stackedBlocksHandler;
    }

    @Override
    public BlockValuesManagerImpl getBlockValues() {
        return blockValuesHandler;
    }

    @Override
    public SchematicsManagerImpl getSchematics() {
        return schematicsHandler;
    }

    @Override
    public PlayersManagerImpl getPlayers() {
        return playersHandler;
    }

    @Override
    public RolesManagerImpl getRoles() {
        return rolesHandler;
    }

    @Override
    public MissionsManagerImpl getMissions() {
        return missionsHandler;
    }

    @Override
    public MenusManagerImpl getMenus() {
        return menusHandler;
    }

    @Override
    public KeysManagerImpl getKeys() {
        return keysHandler;
    }

    @Override
    public ProvidersManagerImpl getProviders() {
        return providersHandler;
    }

    @Override
    public UpgradesManagerImpl getUpgrades() {
        return upgradesHandler;
    }

    @Override
    public CommandsManagerImpl getCommands() {
        return commandsHandler;
    }

    @Override
    public SettingsManagerImpl getSettings() {
        return settingsHandler;
    }

    @Override
    public FactoriesManagerImpl getFactory() {
        return factoriesHandler;
    }

    @Override
    public ModulesManagerImpl getModules() {
        return modulesHandler;
    }

    @Override
    public IScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    @Override
    public void setScriptEngine(@Nullable IScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine == null ? EnginesFactory.createDefaultEngine() : scriptEngine;
    }

    public EventsBus getEventsBus() {
        return eventsBus;
    }

    public ServicesHandler getServices() {
        return servicesHandler;
    }

    public NMSAlgorithms getNMSAlgorithms() {
        return nmsAlgorithms;
    }

    public NMSChunks getNMSChunks() {
        return nmsChunks;
    }

    public NMSDragonFight getNMSDragonFight() {
        return nmsDragonFight;
    }

    public NMSEntities getNMSEntities() {
        return nmsEntities;
    }

    public NMSHolograms getNMSHolograms() {
        return nmsHolograms;
    }

    public NMSPlayers getNMSPlayers() {
        return nmsPlayers;
    }

    public NMSTags getNMSTags() {
        return nmsTags;
    }

    public NMSWorld getNMSWorld() {
        return nmsWorld;
    }

    public String getFileName() {
        return getFile().getName();
    }

    private void loadUpgradeCostLoaders() {
        upgradesHandler.registerUpgradeCostLoader("money", new VaultUpgradeCostLoader());
        upgradesHandler.registerUpgradeCostLoader("placeholders", new PlaceholdersUpgradeCostLoader());
    }

    private long calculateMaxPossibleIslands() {
        int islandDistance = settingsHandler.getMaxIslandSize() * 3;
        long worldDistance = nmsAlgorithms.getMaxWorldSize() * 2L;
        long islandsPerSide = worldDistance / islandDistance;
        return islandsPerSide * islandsPerSide;
    }

}
