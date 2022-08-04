package com.bgsoftware.superiorskyblock;

import com.bgsoftware.common.updater.Updater;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLoadTime;
import com.bgsoftware.superiorskyblock.api.scripts.IScriptEngine;
import com.bgsoftware.superiorskyblock.api.world.event.WorldEventsManager;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandsManagerImpl;
import com.bgsoftware.superiorskyblock.commands.admin.AdminCommandsMap;
import com.bgsoftware.superiorskyblock.commands.player.PlayerCommandsMap;
import com.bgsoftware.superiorskyblock.config.SettingsManagerImpl;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.database.DataManager;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.engine.NashornEngine;
import com.bgsoftware.superiorskyblock.core.engine.NashornEngineDownloader;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.factory.FactoriesManagerImpl;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.JarFiles;
import com.bgsoftware.superiorskyblock.core.itemstack.GlowEnchantment;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.key.KeysManagerImpl;
import com.bgsoftware.superiorskyblock.core.menu.MenusManagerImpl;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.stackedblocks.StackedBlocksManagerImpl;
import com.bgsoftware.superiorskyblock.core.stackedblocks.container.DefaultStackedBlocksContainer;
import com.bgsoftware.superiorskyblock.core.task.CalcTask;
import com.bgsoftware.superiorskyblock.core.task.ShutdownTask;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.values.BlockValuesManagerImpl;
import com.bgsoftware.superiorskyblock.core.values.container.BlockLevelsContainer;
import com.bgsoftware.superiorskyblock.core.values.container.BlockWorthValuesContainer;
import com.bgsoftware.superiorskyblock.core.values.container.GeneralBlockValuesContainer;
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
import com.bgsoftware.superiorskyblock.listener.ChunksListener;
import com.bgsoftware.superiorskyblock.mission.MissionsManagerImpl;
import com.bgsoftware.superiorskyblock.mission.container.DefaultMissionsContainer;
import com.bgsoftware.superiorskyblock.module.ModulesManagerImpl;
import com.bgsoftware.superiorskyblock.module.container.DefaultModulesContainer;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFightImpl;
import com.bgsoftware.superiorskyblock.nms.NMSEntities;
import com.bgsoftware.superiorskyblock.nms.NMSHolograms;
import com.bgsoftware.superiorskyblock.nms.NMSPlayers;
import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.player.PlayersManagerImpl;
import com.bgsoftware.superiorskyblock.player.container.DefaultPlayersContainer;
import com.bgsoftware.superiorskyblock.service.ServicesHandler;
import com.bgsoftware.superiorskyblock.service.bossbar.BossBarsServiceImpl;
import com.bgsoftware.superiorskyblock.service.dragon.DragonBattleServiceImpl;
import com.bgsoftware.superiorskyblock.service.hologram.HologramsServiceImpl;
import com.bgsoftware.superiorskyblock.service.message.MessagesServiceImpl;
import com.bgsoftware.superiorskyblock.service.placeholders.PlaceholdersServiceImpl;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.bgsoftware.superiorskyblock.world.schematic.SchematicsManagerImpl;
import com.bgsoftware.superiorskyblock.world.schematic.container.DefaultSchematicsContainer;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Optional;

public class SuperiorSkyblockPlugin extends JavaPlugin implements SuperiorSkyblock {

    private static SuperiorSkyblockPlugin plugin;
    private final Updater updater = new Updater(this, "superiorskyblock2");

    private final DataManager dataHandler = new DataManager(this);
    private final FactoriesManagerImpl factoriesHandler = new FactoriesManagerImpl();
    private final GridManagerImpl gridHandler = new GridManagerImpl(this,
            new DefaultIslandsPurger(), new DefaultIslandPreviews());
    private final StackedBlocksManagerImpl stackedBlocksHandler = new StackedBlocksManagerImpl(this,
            new DefaultStackedBlocksContainer());
    private final BlockValuesManagerImpl blockValuesHandler = new BlockValuesManagerImpl(this,
            new BlockWorthValuesContainer(), new BlockLevelsContainer(),
            new GeneralBlockValuesContainer(), new GeneralBlockValuesContainer());
    private final SchematicsManagerImpl schematicsHandler = new SchematicsManagerImpl(this,
            new DefaultSchematicsContainer());
    private final PlayersManagerImpl playersHandler = new PlayersManagerImpl(this);
    private final RolesManagerImpl rolesHandler = new RolesManagerImpl(this,
            new DefaultRolesContainer());
    private final MissionsManagerImpl missionsHandler = new MissionsManagerImpl(this,
            new DefaultMissionsContainer());
    private final MenusManagerImpl menusHandler = new MenusManagerImpl(this);
    private final KeysManagerImpl keysHandler = new KeysManagerImpl(this);
    private final ProvidersManagerImpl providersHandler = new ProvidersManagerImpl(this);
    private final UpgradesManagerImpl upgradesHandler = new UpgradesManagerImpl(this,
            new DefaultUpgradesContainer());
    private final CommandsManagerImpl commandsHandler = new CommandsManagerImpl(this,
            new PlayerCommandsMap(this), new AdminCommandsMap(this));
    private final ModulesManagerImpl modulesHandler = new ModulesManagerImpl(this,
            new DefaultModulesContainer());
    private final ServicesHandler servicesHandler = new ServicesHandler(this);
    // The only handler that is initialized is this one, therefore it's not final.
    // This is to prevent it's fields to be non-finals.
    private SettingsManagerImpl settingsHandler = null;
    private IScriptEngine scriptEngine = NashornEngine.getInstance();

    private final EventsBus eventsBus = new EventsBus(this);

    private final BukkitListeners bukkitListeners = new BukkitListeners(this);

    private NMSAlgorithms nmsAlgorithms;
    private NMSChunks nmsChunks;
    private NMSDragonFight nmsDragonFight;
    private NMSEntities nmsEntities;
    private NMSHolograms nmsHolograms;
    private NMSPlayers nmsPlayers;
    private NMSTags nmsTags;
    private NMSWorld nmsWorld;

    private ChunkGenerator worldGenerator = null;

    private boolean shouldEnable = true;

    public static void log(String message) {
        //plugin.pluginDebugger.debug(ChatColor.stripColor(message));
        message = Formatters.COLOR_FORMATTER.format(message);
        if (message.contains(ChatColor.COLOR_CHAR + ""))
            Bukkit.getConsoleSender().sendMessage(ChatColor.getLastColors(message.substring(0, 2)) + "[" + plugin.getDescription().getName() + "] " + message);
        else
            plugin.getLogger().info(message);
    }

    public static SuperiorSkyblockPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onLoad() {
        plugin = this;

        new Metrics(this, 4119);

        bukkitListeners.registerListenerFailureFilter();

        try {
            SuperiorSkyblockAPI.setPluginInstance(this);
        } catch (UnsupportedOperationException error) {
            log("&cThe API instance was already initialized. " +
                    "This can be caused by a reload or another plugin initializing it.");
            shouldEnable = false;
        }

        if (!loadNMSAdapter()) {
            shouldEnable = false;
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownTask(this));

        IslandPrivileges.registerPrivileges();
        SortingTypes.registerSortingTypes();
        IslandFlags.registerFlags();

        try {
            SortingComparators.initializeTopIslandMembersSorting();
        } catch (IllegalArgumentException error) {
            shouldEnable = false;
            log("&cThe TopIslandMembersSorting was already initialized. " +
                    "This can be caused by a reload or another plugin initializing it.");
        }

        this.servicesHandler.registerPlaceholdersService(new PlaceholdersServiceImpl());
        this.servicesHandler.registerHologramsService(new HologramsServiceImpl(this));
        this.servicesHandler.registerEnderDragonService(new DragonBattleServiceImpl(this));
        this.servicesHandler.registerBossBarsService(new BossBarsServiceImpl(this));
        this.servicesHandler.registerMessagesService(new MessagesServiceImpl());
    }

    @Override
    public void onEnable() {
        try {
            if (SuperiorSkyblockAPI.getSuperiorSkyblock() == null) {
                shouldEnable = false;
                ManagerLoadException.handle(new ManagerLoadException("The API instance was not initialized properly. Contact Ome_R regarding this!",
                        ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN));
                return;
            }

            if (!shouldEnable) {
                Bukkit.shutdown();
                return;
            }

            BukkitExecutor.init(this);

            loadUpgradeCostLoaders();

            GlowEnchantment.registerGlowEnchantment();

            try {
                settingsHandler = new SettingsManagerImpl(this);
            } catch (ManagerLoadException ex) {
                if (!ManagerLoadException.handle(ex)) {
                    shouldEnable = false;
                    return;
                }
            }

            modulesHandler.loadData();

            EventsBus.PluginInitializeResult eventResult = eventsBus.callPluginInitializeEvent(this);
            this.playersHandler.setPlayersContainer(Optional.ofNullable(eventResult.getPlayersContainer()).orElse(new DefaultPlayersContainer()));
            this.gridHandler.setIslandsContainer(Optional.ofNullable(eventResult.getIslandsContainer()).orElse(new DefaultIslandsContainer(this)));

            modulesHandler.enableModules(ModuleLoadTime.BEFORE_WORLD_CREATION);

            try {
                providersHandler.getWorldsProvider().prepareWorlds();
            } catch (RuntimeException ex) {
                ManagerLoadException handlerError = new ManagerLoadException(ex.getMessage(), ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
                shouldEnable = false;
                handlerError.printStackTrace();
                PluginDebugger.debug(handlerError);
                Bukkit.shutdown();
                return;
            }

            try {
                reloadPlugin(true);
            } catch (ManagerLoadException error) {
                ManagerLoadException.handle(error);
                shouldEnable = false;
                return;
            }

            try {
                bukkitListeners.register();
            } catch (RuntimeException ex) {
                ManagerLoadException handlerError = new ManagerLoadException("Cannot load plugin due to a missing event: " + ex.getMessage() + " - contact @Ome_R!",
                        ManagerLoadException.ErrorLevel.CONTINUE);
                shouldEnable = false;
                handlerError.printStackTrace();
                PluginDebugger.debug(handlerError);
                Bukkit.shutdown();
                return;
            }

            modulesHandler.enableModules(ModuleLoadTime.AFTER_HANDLERS_LOADING);

            if (updater.isOutdated()) {
                log("");
                log("A new version is available (v" + updater.getLatestVersion() + ")!");
                log("Version's description: \"" + updater.getVersionDescription() + "\"");
                log("");
            }

            ChunksProvider.start();

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
                        playerIsland.setCurrentlyActive();

                    if (island != null)
                        island.setPlayerInside(superiorPlayer, true);
                }
            }, 1L);

            eventsBus.callPluginInitializedEvent(this);

        } catch (Throwable ex) {
            shouldEnable = false;
            ex.printStackTrace();
            PluginDebugger.debug(ex);
            Bukkit.shutdown();
        }
    }

    @Override
    public void onDisable() {
        if (!shouldEnable)
            return;

        ChunksProvider.stop();
        BukkitExecutor.syncDatabaseCalls();

        try {
            dataHandler.saveDatabase(false);

            gridHandler.disablePlugin();

            for (Island island : gridHandler.getIslandsToPurge())
                island.disbandIsland();

            playersHandler.savePlayers();
            gridHandler.saveIslands();
            stackedBlocksHandler.saveStackedBlocks();

            modulesHandler.getModules().forEach(modulesHandler::unregisterModule);

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
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        } finally {
            // SuperiorSkyblockPlugin.log("Unloading worlds...");
            // unloadIslandWorlds();

            SuperiorSkyblockPlugin.log("Shutting down calculation task...");
            CalcTask.cancelTask();

            SuperiorSkyblockPlugin.log("Shutting down executor");
            BukkitExecutor.close();

            SuperiorSkyblockPlugin.log("Closing database. This may hang the server. Do not shut it down, or data may get lost.");
            //pluginDebugger.cancel();
            dataHandler.closeConnection();
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return getGenerator();
    }

    public Updater getUpdater() {
        return updater;
    }

    private boolean loadNMSAdapter() {
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAlgorithms = loadNMSClass("NMSAlgorithmsImpl", version);

            if (!nmsAlgorithms.isMappingsSupported()) {
                new ManagerLoadException(
                        "The plugin doesn't support your version mappings.\n" +
                                "Please try a different version.",
                        ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN).printStackTrace();
                return false;
            }

            nmsChunks = loadNMSClass("NMSChunksImpl", version);
            nmsEntities = loadNMSClass("NMSEntitiesImpl", version);
            nmsHolograms = loadNMSClass("NMSHologramsImpl", version);
            nmsPlayers = loadNMSClass("NMSPlayersImpl", version);
            nmsTags = loadNMSClass("NMSTagsImpl", version);
            nmsWorld = loadNMSClass("NMSWorldImpl", version);
        } catch (Exception ex) {
            ex.printStackTrace();
            new ManagerLoadException(
                    "The plugin doesn't support your minecraft version.\n" +
                            "Please try a different version.",
                    ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN).printStackTrace();
            PluginDebugger.debug(ex);
            return false;
        }

        return true;
    }

    private <T> T loadNMSClass(String className, String version) throws Exception {
        Class<?> nmsClass = Class.forName(String.format("com.bgsoftware.superiorskyblock.nms.%s.%s", version, className));
        try {
            Constructor<?> constructor = nmsClass.getConstructor(SuperiorSkyblockPlugin.class);
            // noinspection unchecked
            return (T) constructor.newInstance(this);
        } catch (NoSuchMethodException error) {
            // noinspection unchecked
            return (T) nmsClass.newInstance();
        }
    }

    public ChunkGenerator getGenerator() {
        if (worldGenerator == null) {
            loadGeneratorFromFile();
            if (worldGenerator == null) {
                worldGenerator = nmsWorld.createGenerator(plugin);
            }
        }

        return worldGenerator;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadGeneratorFromFile() {
        File generatorFolder = new File(plugin.getDataFolder(), "world-generator");

        if (!generatorFolder.isDirectory()) {
            generatorFolder.delete();
        }

        if (!generatorFolder.exists()) {
            generatorFolder.mkdirs();
        } else {
            try {
                File[] generatorsFilesList = generatorFolder.listFiles();
                if (generatorsFilesList != null) {
                    for (File file : generatorsFilesList) {
                        //noinspection deprecation
                        Class<?> generatorClass = JarFiles.getClass(file.toURL(), ChunkGenerator.class);
                        if (generatorClass != null) {
                            for (Constructor<?> constructor : generatorClass.getConstructors()) {
                                if (constructor.getParameterCount() == 0) {
                                    worldGenerator = (ChunkGenerator) generatorClass.newInstance();
                                    return;
                                } else if (constructor.getParameterTypes()[0].equals(JavaPlugin.class) ||
                                        constructor.getParameterTypes()[0].equals(SuperiorSkyblock.class)) {
                                    worldGenerator = (ChunkGenerator) constructor.newInstance(this);
                                    return;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log("An error occurred while loading the generator:");
                ex.printStackTrace();
                PluginDebugger.debug(ex);
            }
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

    public void reloadPlugin(boolean loadGrid) throws ManagerLoadException {
        ItemSkulls.readTextures(this);

        if (!loadGrid) {
            settingsHandler = new SettingsManagerImpl(this);
        } else {
            commandsHandler.loadData();
            modulesHandler.enableModules(ModuleLoadTime.NORMAL);
        }

        if (!checkScriptEngine()) {
            throw new ManagerLoadException(
                    "It seems like the script engine of the plugin is corrupted.\n" +
                            "This may occur by one of the following reasons:\n" +
                            "1. You have a module/plugin that sets a custom script that doesn't work well.\n" +
                            "2. You're using Java 16 without installing an external module engine.\n" +
                            "If that's the case, check out the following link:\n" +
                            "https://github.com/BG-Software-LLC/SuperiorSkyblock2-NashornEngine",
                    ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        blockValuesHandler.loadData();
        upgradesHandler.loadData();
        rolesHandler.loadData();

        Message.reload();

        if (loadGrid) {
            playersHandler.loadData();
            gridHandler.loadData();
        } else {
            BukkitExecutor.sync(gridHandler::updateSpawn, 1L);
            gridHandler.syncUpgrades();
        }

        schematicsHandler.loadData();
        providersHandler.loadData();
        menusHandler.loadData();

        if (loadGrid) {
            dataHandler.loadData();
            stackedBlocksHandler.loadData();
            SortingType.values().forEach(gridHandler::sortIslands);
            modulesHandler.loadModulesData(this);
            modulesHandler.enableModules(ModuleLoadTime.AFTER_MODULE_DATA_LOAD);
        } else {
            modulesHandler.reloadModules(this);
        }

        BukkitExecutor.sync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                SuperiorPlayer superiorPlayer = playersHandler.getSuperiorPlayer(player);
                Island island = gridHandler.getIslandAt(player.getLocation());
                superiorPlayer.updateWorldBorder(island);
                if (island != null)
                    island.applyEffects(superiorPlayer);
            }
        });

        CalcTask.startTask();
    }

    private void unloadIslandWorlds() {
        for (World world : Bukkit.getWorlds()) {
            if (providersHandler.getWorldsProvider().isIslandsWorld(world))
                Bukkit.unloadWorld(world, true);
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
    public MenusManager getMenus() {
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
        this.scriptEngine = scriptEngine == null ? NashornEngine.getInstance() : scriptEngine;
    }

    @Override
    @Deprecated
    public WorldEventsManager getWorldEventsManager() {
        return getListener(ChunksListener.class).get().getWorldEventsManager();
    }

    @Override
    @Deprecated
    public void setWorldEventsManager(@Nullable WorldEventsManager worldEventsManager) {
        getListener(ChunksListener.class).get().setWorldEventsManager(worldEventsManager);
    }

    public EventsBus getEventsBus() {
        return eventsBus;
    }

    public ServicesHandler getServices() {
        return servicesHandler;
    }

    public void setSettings(SettingsManagerImpl settingsHandler) {
        this.settingsHandler = settingsHandler;
    }

    public NMSAlgorithms getNMSAlgorithms() {
        return nmsAlgorithms;
    }

    public NMSChunks getNMSChunks() {
        return nmsChunks;
    }

    public NMSDragonFight getNMSDragonFight() {
        if (nmsDragonFight == null) {
            String version = getServer().getClass().getPackage().getName().split("\\.")[3];
            try {
                nmsDragonFight = settingsHandler.getWorlds().getEnd().isDragonFight() ?
                        loadNMSClass("NMSDragonFightImpl", version) : new NMSDragonFightImpl();
            } catch (Exception ignored) {
            }
        }

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

    public <E extends Listener> Singleton<E> getListener(Class<E> listenerClass) {
        return bukkitListeners.getListener(listenerClass);
    }

    public String getFileName() {
        return getFile().getName();
    }

    private void loadUpgradeCostLoaders() {
        upgradesHandler.registerUpgradeCostLoader("money", new VaultUpgradeCostLoader());
        upgradesHandler.registerUpgradeCostLoader("placeholders", new PlaceholdersUpgradeCostLoader());
    }

}
