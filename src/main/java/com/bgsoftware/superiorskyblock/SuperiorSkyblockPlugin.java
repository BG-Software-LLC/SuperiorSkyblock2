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
import com.bgsoftware.superiorskyblock.commands.CommandsHandler;
import com.bgsoftware.superiorskyblock.commands.admin.AdminCommandsMap;
import com.bgsoftware.superiorskyblock.commands.player.PlayerCommandsMap;
import com.bgsoftware.superiorskyblock.config.SettingsHandler;
import com.bgsoftware.superiorskyblock.database.DataHandler;
import com.bgsoftware.superiorskyblock.engine.NashornEngine;
import com.bgsoftware.superiorskyblock.engine.NashornEngineDownloader;
import com.bgsoftware.superiorskyblock.factory.FactoriesHandler;
import com.bgsoftware.superiorskyblock.handler.HandlerLoadException;
import com.bgsoftware.superiorskyblock.hooks.ProvidersHandler;
import com.bgsoftware.superiorskyblock.island.container.DefaultIslandsContainer;
import com.bgsoftware.superiorskyblock.island.flags.IslandFlags;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.key.KeysHandler;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.listeners.ChunksListener;
import com.bgsoftware.superiorskyblock.listeners.CustomEventsListener;
import com.bgsoftware.superiorskyblock.listeners.DragonListener;
import com.bgsoftware.superiorskyblock.listeners.MenusListener;
import com.bgsoftware.superiorskyblock.listeners.PlayersListener;
import com.bgsoftware.superiorskyblock.listeners.ProtectionListener;
import com.bgsoftware.superiorskyblock.listeners.SettingsListener;
import com.bgsoftware.superiorskyblock.menu.MenusHandler;
import com.bgsoftware.superiorskyblock.metrics.Metrics;
import com.bgsoftware.superiorskyblock.mission.MissionsHandler;
import com.bgsoftware.superiorskyblock.mission.container.DefaultMissionsContainer;
import com.bgsoftware.superiorskyblock.module.ModulesHandler;
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
import com.bgsoftware.superiorskyblock.player.PlayersHandler;
import com.bgsoftware.superiorskyblock.player.container.DefaultPlayersContainer;
import com.bgsoftware.superiorskyblock.role.RolesHandler;
import com.bgsoftware.superiorskyblock.role.container.DefaultRolesContainer;
import com.bgsoftware.superiorskyblock.schematic.SchematicsHandler;
import com.bgsoftware.superiorskyblock.schematic.container.DefaultSchematicsContainer;
import com.bgsoftware.superiorskyblock.tasks.CalcTask;
import com.bgsoftware.superiorskyblock.tasks.ShutdownTask;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.upgrade.UpgradesHandler;
import com.bgsoftware.superiorskyblock.upgrade.container.DefaultUpgradesContainer;
import com.bgsoftware.superiorskyblock.upgrade.loaders.PlaceholdersUpgradeCostLoader;
import com.bgsoftware.superiorskyblock.upgrade.loaders.VaultUpgradeCostLoader;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.values.BlockValuesHandler;
import com.bgsoftware.superiorskyblock.values.container.BlockLevelsContainer;
import com.bgsoftware.superiorskyblock.values.container.BlockWorthValuesContainer;
import com.bgsoftware.superiorskyblock.values.container.GeneralBlockValuesContainer;
import com.bgsoftware.superiorskyblock.world.GridHandler;
import com.bgsoftware.superiorskyblock.world.blocks.stacked.StackedBlocksHandler;
import com.bgsoftware.superiorskyblock.world.blocks.stacked.container.DefaultStackedBlocksContainer;
import com.bgsoftware.superiorskyblock.world.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.world.event.WorldEventsManagerImpl;
import com.bgsoftware.superiorskyblock.world.preview.DefaultIslandPreviews;
import com.bgsoftware.superiorskyblock.world.purge.DefaultIslandsPurger;
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
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SuperiorSkyblockPlugin extends JavaPlugin implements SuperiorSkyblock {

    private static final Pattern LISTENER_REGISTER_FAILURE =
            Pattern.compile("Plugin SuperiorSkyblock2 v(.*) has failed to register events for (.*) because (.*) does not exist\\.");

    private static SuperiorSkyblockPlugin plugin;
    private final Updater updater = new Updater(this, "superiorskyblock2");

    private final DataHandler dataHandler = new DataHandler(this);
    private final FactoriesHandler factoriesHandler = new FactoriesHandler();
    private final GridHandler gridHandler = new GridHandler(this,
            new DefaultIslandsPurger(), new DefaultIslandPreviews(), new DefaultIslandsContainer(this));
    private final StackedBlocksHandler stackedBlocksHandler = new StackedBlocksHandler(this,
            new DefaultStackedBlocksContainer());
    private final BlockValuesHandler blockValuesHandler = new BlockValuesHandler(this,
            new BlockWorthValuesContainer(), new BlockLevelsContainer(),
            new GeneralBlockValuesContainer(), new GeneralBlockValuesContainer());
    private final SchematicsHandler schematicsHandler = new SchematicsHandler(this,
            new DefaultSchematicsContainer());
    private final PlayersHandler playersHandler = new PlayersHandler(this,
            new DefaultPlayersContainer());
    private final RolesHandler rolesHandler = new RolesHandler(this,
            new DefaultRolesContainer());
    private final MissionsHandler missionsHandler = new MissionsHandler(this,
            new DefaultMissionsContainer());
    private final MenusHandler menusHandler = new MenusHandler(this);
    private final KeysHandler keysHandler = new KeysHandler(this);
    private final ProvidersHandler providersHandler = new ProvidersHandler(this);
    private final UpgradesHandler upgradesHandler = new UpgradesHandler(this,
            new DefaultUpgradesContainer());
    private final CommandsHandler commandsHandler = new CommandsHandler(this,
            new PlayerCommandsMap(this), new AdminCommandsMap(this));
    private final ModulesHandler modulesHandler = new ModulesHandler(this,
            new DefaultModulesContainer());
    // The only handler that is initialized is this one, therefore it's not final.
    // This is to prevent it's fields to be non-finals.
    private SettingsHandler settingsHandler = null;
    private IScriptEngine scriptEngine = NashornEngine.getInstance();
    private WorldEventsManager worldEventsManager = new WorldEventsManagerImpl(this);

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
    private String listenerRegisterFailure = "";

    public static void log(String message) {
        //plugin.pluginDebugger.debug(ChatColor.stripColor(message));
        message = StringUtils.translateColors(message);
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

        // Setting the default locale to English will fix issues related to using upper case in Turkish.
        // https://stackoverflow.com/questions/11063102/using-locales-with-javas-tolowercase-and-touppercase
        Locale.setDefault(Locale.ENGLISH);

        new Metrics(this);

        initCustomFilter();

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
    }

    @Override
    public void onEnable() {
        try {
            if (!shouldEnable) {
                Bukkit.shutdown();
                return;
            }

            Executor.init(this);

            loadUpgradeCostLoaders();

            EnchantsUtils.registerGlowEnchantment();

            try {
                settingsHandler = new SettingsHandler(this);
            } catch (HandlerLoadException ex) {
                if (!HandlerLoadException.handle(ex)) {
                    shouldEnable = false;
                    return;
                }
            }

            modulesHandler.loadData();

            EventsCaller.callPluginInitializeEvent(this);

            modulesHandler.enableModules(ModuleLoadTime.BEFORE_WORLD_CREATION);

            try {
                providersHandler.getWorldsProvider().prepareWorlds();
            } catch (RuntimeException ex) {
                HandlerLoadException handlerError = new HandlerLoadException(ex.getMessage(), HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
                shouldEnable = false;
                handlerError.printStackTrace();
                PluginDebugger.debug(handlerError);
                Bukkit.shutdown();
                return;
            }

            try {
                reloadPlugin(true);
            } catch (HandlerLoadException error) {
                HandlerLoadException.handle(error);
                shouldEnable = false;
                return;
            }

            try {
                safeEventsRegister(new BlocksListener(this));
                safeEventsRegister(new ChunksListener(this));
                safeEventsRegister(new CustomEventsListener(this));
                if (settingsHandler.getWorlds().getEnd().isDragonFight())
                    safeEventsRegister(new DragonListener(this));
                safeEventsRegister(new MenusListener(this));
                safeEventsRegister(new PlayersListener(this));
                safeEventsRegister(new ProtectionListener(this));
                safeEventsRegister(new SettingsListener(this));
            } catch (RuntimeException ex) {
                HandlerLoadException handlerError = new HandlerLoadException("Cannot load plugin due to a missing event: " + ex.getMessage() + " - contact @Ome_R!",
                        HandlerLoadException.ErrorLevel.CONTINUE);
                shouldEnable = false;
                handlerError.printStackTrace();
                PluginDebugger.debug(handlerError);
                Bukkit.shutdown();
                return;
            }

            if (updater.isOutdated()) {
                log("");
                log("A new version is available (v" + updater.getLatestVersion() + ")!");
                log("Version's description: \"" + updater.getVersionDescription() + "\"");
                log("");
            }

            ChunksProvider.start();

            Executor.sync(() -> {
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
        Executor.syncDatabaseCalls();
        unloadIslandWorlds();

        try {
            dataHandler.saveDatabase(false);

            gridHandler.disablePlugin();

            for (Island island : gridHandler.getIslandsToPurge())
                island.disbandIsland();

            playersHandler.savePlayers();
            gridHandler.saveIslands();
            stackedBlocksHandler.saveStackedBlocks();

            modulesHandler.getModules().forEach(modulesHandler::unregisterModule);

            Bukkit.getOnlinePlayers().forEach(player -> {
                SuperiorPlayer superiorPlayer = playersHandler.getSuperiorPlayer(player);
                player.closeInventory();
                superiorPlayer.updateWorldBorder(null);
                if (superiorPlayer.hasIslandFlyEnabled()) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        } finally {
            CalcTask.cancelTask();
            Executor.close();
            SuperiorSkyblockPlugin.log("Closing database. This may hang the server. Do not shut it down, or data may get lost.");

            //pluginDebugger.cancel();
            dataHandler.closeConnection();
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return getGenerator();
    }

    private void initCustomFilter() {
        getLogger().setFilter(record -> {
            Matcher matcher = LISTENER_REGISTER_FAILURE.matcher(record.getMessage());
            if (matcher.find())
                listenerRegisterFailure = matcher.group(3);

            return true;
        });
    }

    private void safeEventsRegister(Listener listener) {
        listenerRegisterFailure = "";
        getServer().getPluginManager().registerEvents(listener, this);
        if (!listenerRegisterFailure.isEmpty())
            throw new RuntimeException(listenerRegisterFailure);
    }

    public Updater getUpdater() {
        return updater;
    }

    private boolean loadNMSAdapter() {
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAlgorithms = loadNMSClass("NMSAlgorithmsImpl", version);
            nmsChunks = loadNMSClass("NMSChunksImpl", version);
            nmsEntities = loadNMSClass("NMSEntitiesImpl", version);
            nmsHolograms = loadNMSClass("NMSHologramsImpl", version);
            nmsPlayers = loadNMSClass("NMSPlayersImpl", version);
            nmsTags = loadNMSClass("NMSTagsImpl", version);
            nmsWorld = loadNMSClass("NMSWorldImpl", version);
            return true;
        } catch (Exception ex) {
            new HandlerLoadException(
                    "The plugin doesn't support your minecraft version.\n" +
                            "Please try a different version.",
                    HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN).printStackTrace();
            PluginDebugger.debug(ex);
            return false;
        }
    }

    private <T> T loadNMSClass(String className, String version) throws Exception {
        // noinspection unchecked
        return (T) Class.forName(String.format("com.bgsoftware.superiorskyblock.nms.%s.%s", version, className)).newInstance();
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
                        Optional<Class<?>> generatorClassOptional = FileUtils.getClasses(file.toURL(), ChunkGenerator.class).stream().findFirst();
                        if (generatorClassOptional.isPresent()) {
                            Class<?> generatorClass = generatorClassOptional.get();
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

    public void reloadPlugin(boolean loadGrid) throws HandlerLoadException {
        HeadUtils.readTextures(this);

        if (!loadGrid) {
            settingsHandler = new SettingsHandler(this);
        } else {
            commandsHandler.loadData();
            modulesHandler.enableModules(ModuleLoadTime.NORMAL);
        }

        if (!checkScriptEngine()) {
            throw new HandlerLoadException(
                    "It seems like the script engine of the plugin is corrupted.\n" +
                            "This may occur by one of the following reasons:\n" +
                            "1. You have a module/plugin that sets a custom script that doesn't work well.\n" +
                            "2. You're using Java 16 without installing an external module engine.\n" +
                            "If that's the case, check out the following link:\n" +
                            "https://github.com/BG-Software-LLC/SuperiorSkyblock2-NashornEngine",
                    HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        blockValuesHandler.loadData();
        upgradesHandler.loadData();
        rolesHandler.loadData();

        Message.reload();

        if (loadGrid) {
            playersHandler.loadData();
            gridHandler.loadData();
        } else {
            Executor.sync(gridHandler::updateSpawn, 1L);
            gridHandler.syncUpgrades();
        }

        schematicsHandler.loadData();
        providersHandler.loadData();
        menusHandler.loadData();

        if (loadGrid) {
            dataHandler.loadData();
            stackedBlocksHandler.loadData();
            modulesHandler.enableModules(ModuleLoadTime.AFTER_HANDLERS_LOADING);
            SortingType.values().forEach(gridHandler::sortIslands);
        } else {
            modulesHandler.getModules().forEach(pluginModule -> pluginModule.onReload(this));
        }

        Executor.sync(() -> {
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
    public GridHandler getGrid() {
        return gridHandler;
    }

    @Override
    public StackedBlocksHandler getStackedBlocks() {
        return stackedBlocksHandler;
    }

    @Override
    public BlockValuesHandler getBlockValues() {
        return blockValuesHandler;
    }

    @Override
    public SchematicsHandler getSchematics() {
        return schematicsHandler;
    }

    @Override
    public PlayersHandler getPlayers() {
        return playersHandler;
    }

    @Override
    public RolesHandler getRoles() {
        return rolesHandler;
    }

    @Override
    public MissionsHandler getMissions() {
        return missionsHandler;
    }

    @Override
    public MenusManager getMenus() {
        return menusHandler;
    }

    @Override
    public KeysHandler getKeys() {
        return keysHandler;
    }

    @Override
    public ProvidersHandler getProviders() {
        return providersHandler;
    }

    @Override
    public UpgradesHandler getUpgrades() {
        return upgradesHandler;
    }

    @Override
    public CommandsHandler getCommands() {
        return commandsHandler;
    }

    @Override
    public SettingsHandler getSettings() {
        return settingsHandler;
    }

    @Override
    public FactoriesHandler getFactory() {
        return factoriesHandler;
    }

    @Override
    public ModulesHandler getModules() {
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
    public WorldEventsManager getWorldEventsManager() {
        return worldEventsManager;
    }

    @Override
    public void setWorldEventsManager(@Nullable WorldEventsManager worldEventsManager) {
        this.worldEventsManager = worldEventsManager == null ? new WorldEventsManagerImpl(this) : worldEventsManager;
    }

    public void setSettings(SettingsHandler settingsHandler) {
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

    public String getFileName() {
        return getFile().getName();
    }

    private void loadUpgradeCostLoaders() {
        upgradesHandler.registerUpgradeCostLoader("money", new VaultUpgradeCostLoader());
        upgradesHandler.registerUpgradeCostLoader("placeholders", new PlaceholdersUpgradeCostLoader());
    }

}
