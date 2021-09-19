package com.bgsoftware.superiorskyblock;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.updater.Updater;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLoadTime;
import com.bgsoftware.superiorskyblock.api.scripts.IScriptEngine;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandsHandler;
import com.bgsoftware.superiorskyblock.commands.admin.AdminCommandsMap;
import com.bgsoftware.superiorskyblock.commands.player.PlayerCommandsMap;
import com.bgsoftware.superiorskyblock.database.DataHandler;
import com.bgsoftware.superiorskyblock.factory.FactoriesHandler;
import com.bgsoftware.superiorskyblock.island.container.DefaultIslandsContainer;
import com.bgsoftware.superiorskyblock.schematic.container.DefaultSchematicsContainer;
import com.bgsoftware.superiorskyblock.upgrade.container.DefaultUpgradesContainer;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.bgsoftware.superiorskyblock.world.GridHandler;
import com.bgsoftware.superiorskyblock.key.KeysHandler;
import com.bgsoftware.superiorskyblock.menu.MenusHandler;
import com.bgsoftware.superiorskyblock.mission.MissionsHandler;
import com.bgsoftware.superiorskyblock.module.ModulesHandler;
import com.bgsoftware.superiorskyblock.player.PlayersHandler;
import com.bgsoftware.superiorskyblock.hooks.ProvidersHandler;
import com.bgsoftware.superiorskyblock.schematic.SchematicsHandler;
import com.bgsoftware.superiorskyblock.config.SettingsHandler;
import com.bgsoftware.superiorskyblock.upgrade.UpgradesHandler;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.listeners.ChunksListener;
import com.bgsoftware.superiorskyblock.listeners.CustomEventsListener;
import com.bgsoftware.superiorskyblock.listeners.DragonListener;
import com.bgsoftware.superiorskyblock.listeners.MenusListener;
import com.bgsoftware.superiorskyblock.listeners.PlayersListener;
import com.bgsoftware.superiorskyblock.listeners.ProtectionListener;
import com.bgsoftware.superiorskyblock.listeners.SettingsListener;
import com.bgsoftware.superiorskyblock.metrics.Metrics;
import com.bgsoftware.superiorskyblock.mission.container.DefaultMissionsContainer;
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
import com.bgsoftware.superiorskyblock.player.container.DefaultPlayersContainer;
import com.bgsoftware.superiorskyblock.role.RolesHandler;
import com.bgsoftware.superiorskyblock.role.container.DefaultRolesContainer;
import com.bgsoftware.superiorskyblock.scripts.NashornEngine;
import com.bgsoftware.superiorskyblock.tasks.CalcTask;
import com.bgsoftware.superiorskyblock.upgrade.loaders.PlaceholdersUpgradeCostLoader;
import com.bgsoftware.superiorskyblock.upgrade.loaders.VaultUpgradeCostLoader;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.handler.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.values.BlockValuesHandler;
import com.bgsoftware.superiorskyblock.values.container.BlockLevelsContainer;
import com.bgsoftware.superiorskyblock.values.container.BlockWorthValuesContainer;
import com.bgsoftware.superiorskyblock.values.container.GeneralBlockValuesContainer;
import com.bgsoftware.superiorskyblock.world.blocks.StackedBlocksHandler;
import com.bgsoftware.superiorskyblock.world.blocks.container.DefaultStackedBlocksContainer;
import com.bgsoftware.superiorskyblock.world.preview.DefaultIslandPreviews;
import com.bgsoftware.superiorskyblock.world.purge.DefaultIslandsPurger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SuperiorSkyblockPlugin extends JavaPlugin implements SuperiorSkyblock {

    private static final ReflectField<SuperiorSkyblock> PLUGIN = new ReflectField<>(SuperiorSkyblockAPI.class, SuperiorSkyblock.class, "plugin");
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
    private boolean debugMode = false;
    private Pattern debugFilter = null;

    @Override
    public void onLoad() {
        plugin = this;
        new Metrics(this);

        initCustomFilter();

        PLUGIN.set(null, this);

        if(!loadNMSAdapter()) {
            shouldEnable = false;
        }
    }

    private static final Pattern LISTENER_REGISTER_FAILURE =
            Pattern.compile("Plugin SuperiorSkyblock2 v(.*) has failed to register events for (.*) because (.*) does not exist\\.");

    private String listenerRegisterFailure = "";

    private void initCustomFilter(){
        getLogger().setFilter(record -> {
            Matcher matcher = LISTENER_REGISTER_FAILURE.matcher(record.getMessage());
            if(matcher.find())
                listenerRegisterFailure = matcher.group(3);

            return true;
        });
    }

    private void safeEventsRegister(Listener listener){
        listenerRegisterFailure = "";
        getServer().getPluginManager().registerEvents(listener, this);
        if(!listenerRegisterFailure.isEmpty())
            throw new RuntimeException(listenerRegisterFailure);
    }

    @Override
    public void onEnable() {
        try {
            if (!shouldEnable) {
                Bukkit.shutdown();
                return;
            }

            Executor.init(this);

            loadSortingTypes();
            loadIslandFlags();
            loadIslandPrivileges();
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

            try{
                providersHandler.prepareWorlds();
            }catch (RuntimeException ex){
                shouldEnable = false;
                new HandlerLoadException(ex.getMessage(), HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN).printStackTrace();
                Bukkit.shutdown();
                return;
            }

            if(!reloadPlugin(true)) {
                shouldEnable = false;
                return;
            }

            try {
                safeEventsRegister(new BlocksListener(this));
                safeEventsRegister(new ChunksListener(this));
                safeEventsRegister(new CustomEventsListener(this));
                if(settingsHandler.getWorlds().getEnd().isDragonFight())
                    safeEventsRegister(new DragonListener(this));
                safeEventsRegister(new MenusListener(this));
                safeEventsRegister(new PlayersListener(this));
                safeEventsRegister(new ProtectionListener(this));
                safeEventsRegister(new SettingsListener(this));
            }catch (RuntimeException ex){
                shouldEnable = false;
                new HandlerLoadException("Cannot load plugin due to a missing event: " + ex.getMessage() + " - contact @Ome_R!",
                        HandlerLoadException.ErrorLevel.CONTINUE).printStackTrace();
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
        }catch (Throwable ex){
            shouldEnable = false;
            ex.printStackTrace();
            Bukkit.shutdown();
        }
    }

    @Override
    public void onDisable() {
        if(!shouldEnable)
            return;

        ChunksProvider.stop();
        try {
            dataHandler.saveDatabase(false);

            gridHandler.disablePlugin();

            for(Island island : gridHandler.getIslandsToPurge())
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
        }catch(Exception ex){
            ex.printStackTrace();
        }finally {
            CalcTask.cancelTask();
            Executor.close();
            SuperiorSkyblockPlugin.log("Closing database. This may hang the server. Do not shut it down, or data may get lost.");

            dataHandler.closeConnection();
        }
    }

    public Updater getUpdater() {
        return updater;
    }

    private boolean loadNMSAdapter(){
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAlgorithms = loadNMSClass("NMSAlgorithmsImpl", version);
            nmsChunks = loadNMSClass("NMSChunksImpl", version);
            nmsDragonFight = new SettingsHandler(this).getWorlds().getEnd().isDragonFight() ?
                    loadNMSClass("NMSDragonFightImpl", version) : new NMSDragonFightImpl();
            nmsEntities = loadNMSClass("NMSEntitiesImpl", version);
            nmsHolograms = loadNMSClass("NMSHologramsImpl", version);
            nmsPlayers = loadNMSClass("NMSPlayersImpl", version);
            nmsTags = loadNMSClass("NMSTagsImpl", version);
            nmsWorld = loadNMSClass("NMSWorldImpl", version);
            return true;
        } catch (Exception ex) {
            log("SuperiorSkyblock doesn't support " + version + " - shutting down...");
            return false;
        }
    }

    private <T> T loadNMSClass(String className, String version) throws Exception {
        // noinspection unchecked
        return (T) Class.forName(String.format("com.bgsoftware.superiorskyblock.nms.%s.%s", version, className)).newInstance();
    }

    public ChunkGenerator getGenerator(){
        if(worldGenerator == null) {
            loadGeneratorFromFile();
            if (worldGenerator == null)
                worldGenerator = new IslandsGenerator(settingsHandler.getWorlds().getDefaultWorld());
        }

        return worldGenerator;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadGeneratorFromFile(){
        File generatorFolder = new File(plugin.getDataFolder(), "world-generator");

        if(!generatorFolder.isDirectory()){
            generatorFolder.delete();
        }

        if (!generatorFolder.exists()) {
            generatorFolder.mkdirs();
        }

        else {
            try {
                File[] generatorsFilesList =  generatorFolder.listFiles();
                if(generatorsFilesList != null) {
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
            }
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return getGenerator();
    }

    private boolean checkScriptEngine(){
        try{
            scriptEngine.eval("1+1");
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    public boolean reloadPlugin(boolean loadGrid){
        HeadUtils.readTextures(this);

        if(!loadGrid) {
            try {
                settingsHandler = new SettingsHandler(this);
            } catch (HandlerLoadException ex) {
                if (!HandlerLoadException.handle(ex))
                    return false;
            }
        }

        else{
            commandsHandler.loadData();
            modulesHandler.enableModules(ModuleLoadTime.NORMAL);
        }

        if(!checkScriptEngine()){
            HandlerLoadException.handle(new HandlerLoadException(
                    "It seems like the script engine of the plugin is corrupted.\n" +
                    "This may occur by one of the following reasons:\n" +
                    "1. You have a module/plugin that sets a custom script that doesn't work well.\n" +
                    "2. You're using Java 16 without installing an external module engine.\n" +
                    "If that's the case, check out the following link:\n" +
                    "https://github.com/BG-Software-LLC/SuperiorSkyblock2-NashornEngine",
                    HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN));
            return false;
        }

        blockValuesHandler.loadData();
        upgradesHandler.loadData();
        rolesHandler.loadData();

        Locale.reload();

        if(loadGrid) {
            playersHandler.loadData();
            gridHandler.loadData();
        }
        else{
            Executor.sync(gridHandler::updateSpawn, 1L);
            gridHandler.syncUpgrades();
        }

        schematicsHandler.loadData();
        providersHandler.loadData();
        menusHandler.loadData();

        if (loadGrid) {
            try {
                dataHandler.loadDataWithException();
                stackedBlocksHandler.loadData();
            }catch(HandlerLoadException ex){
                if(!HandlerLoadException.handle(ex))
                    return false;
            }
        }

        else{
            modulesHandler.enableModules(ModuleLoadTime.AFTER_HANDLERS_LOADING);

            modulesHandler.getModules().forEach(pluginModule -> pluginModule.onReload(this));
        }

        Executor.sync(() -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                SuperiorPlayer superiorPlayer = playersHandler.getSuperiorPlayer(player);
                Island island = gridHandler.getIslandAt(player.getLocation());
                superiorPlayer.updateWorldBorder(island);
                if(island != null)
                    island.applyEffects(superiorPlayer);
            }
        });

        CalcTask.startTask();

        return true;
    }

    @Override
    public GridHandler getGrid(){
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

    public void setSettings(SettingsHandler settingsHandler){
        this.settingsHandler = settingsHandler;
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

    public NMSTags getNMSTags(){
        return nmsTags;
    }

    public NMSWorld getNMSWorld() {
        return nmsWorld;
    }

    public String getFileName(){
        return getFile().getName();
    }

    public boolean isDebugMode(){
        return debugMode;
    }

    public void toggleDebugMode(){
        debugMode = !debugMode;
    }

    public void setDebugFilter(String debugFilter){
        if(debugFilter.isEmpty())
            this.debugFilter = null;
        else
            this.debugFilter = Pattern.compile(debugFilter.toUpperCase());
    }

    private void loadSortingTypes(){
        try { SortingType.register("WORTH", SortingComparators.WORTH_COMPARATOR, false); }catch(NullPointerException ignored) {}
        try { SortingType.register("LEVEL", SortingComparators.LEVEL_COMPARATOR, false); }catch(NullPointerException ignored) {}
        try { SortingType.register("RATING", SortingComparators.RATING_COMPARATOR, false); }catch(NullPointerException ignored) {}
        try { SortingType.register("PLAYERS", SortingComparators.PLAYERS_COMPARATOR, false); }catch(NullPointerException ignored) {}
    }

    private void loadIslandFlags(){
        IslandFlag.register("ALWAYS_DAY");
        IslandFlag.register("ALWAYS_MIDDLE_DAY");
        IslandFlag.register("ALWAYS_NIGHT");
        IslandFlag.register("ALWAYS_MIDDLE_NIGHT");
        IslandFlag.register("ALWAYS_RAIN");
        IslandFlag.register("ALWAYS_SHINY");
        IslandFlag.register("CREEPER_EXPLOSION");
        IslandFlag.register("CROPS_GROWTH");
        IslandFlag.register("EGG_LAY");
        IslandFlag.register("ENDERMAN_GRIEF");
        IslandFlag.register("FIRE_SPREAD");
        IslandFlag.register("GHAST_FIREBALL");
        IslandFlag.register("LAVA_FLOW");
        IslandFlag.register("NATURAL_ANIMALS_SPAWN");
        IslandFlag.register("NATURAL_MONSTER_SPAWN");
        IslandFlag.register("PVP");
        IslandFlag.register("SPAWNER_ANIMALS_SPAWN");
        IslandFlag.register("SPAWNER_MONSTER_SPAWN");
        IslandFlag.register("TNT_EXPLOSION");
        IslandFlag.register("TREE_GROWTH");
        IslandFlag.register("WATER_FLOW");
        IslandFlag.register("WITHER_EXPLOSION");
    }

    private void loadIslandPrivileges() {
        IslandPrivilege.register("ALL");
        IslandPrivilege.register("ANIMAL_BREED");
        IslandPrivilege.register("ANIMAL_DAMAGE");
        IslandPrivilege.register("ANIMAL_SHEAR");
        IslandPrivilege.register("ANIMAL_SPAWN");
        IslandPrivilege.register("BAN_MEMBER");
        IslandPrivilege.register("BREAK");
        IslandPrivilege.register("BUILD");
        IslandPrivilege.register("CHANGE_NAME");
        IslandPrivilege.register("CHEST_ACCESS");
        IslandPrivilege.register("CLOSE_BYPASS");
        IslandPrivilege.register("CLOSE_ISLAND");
        IslandPrivilege.register("COOP_MEMBER");
        IslandPrivilege.register("DELETE_WARP");
        IslandPrivilege.register("DEMOTE_MEMBERS");
        IslandPrivilege.register("DEPOSIT_MONEY");
        IslandPrivilege.register("DISBAND_ISLAND");
        IslandPrivilege.register("DISCORD_SHOW");
        IslandPrivilege.register("DROP_ITEMS");
        IslandPrivilege.register("ENDER_PEARL");
        IslandPrivilege.register("EXPEL_BYPASS");
        IslandPrivilege.register("EXPEL_PLAYERS");
        IslandPrivilege.register("FARM_TRAMPING");
        IslandPrivilege.register("FERTILIZE");
        IslandPrivilege.register("FISH");
        IslandPrivilege.register("FLY");
        IslandPrivilege.register("HORSE_INTERACT");
        IslandPrivilege.register("INTERACT");
        IslandPrivilege.register("INVITE_MEMBER");
        IslandPrivilege.register("ISLAND_CHEST");
        IslandPrivilege.register("ITEM_FRAME");
        IslandPrivilege.register("KICK_MEMBER");
        IslandPrivilege.register("LEASH");
        IslandPrivilege.register("MINECART_DAMAGE");
        IslandPrivilege.register("MINECART_ENTER");
        IslandPrivilege.register("MINECART_OPEN");
        IslandPrivilege.register("MINECART_PLACE");
        IslandPrivilege.register("MONSTER_DAMAGE");
        IslandPrivilege.register("MONSTER_SPAWN");
        IslandPrivilege.register("NAME_ENTITY");
        IslandPrivilege.register("OPEN_ISLAND");
        IslandPrivilege.register("PAINTING");
        IslandPrivilege.register("PAYPAL_SHOW");
        IslandPrivilege.register("PICKUP_DROPS");
        if(!ServerVersion.isLegacy())
            IslandPrivilege.register("PICKUP_FISH");
        IslandPrivilege.register("PROMOTE_MEMBERS");
        IslandPrivilege.register("RANKUP");
        IslandPrivilege.register("RATINGS_SHOW");
        IslandPrivilege.register("SET_BIOME");
        IslandPrivilege.register("SET_DISCORD");
        IslandPrivilege.register("SET_HOME");
        IslandPrivilege.register("SET_PAYPAL");
        IslandPrivilege.register("SET_PERMISSION");
        IslandPrivilege.register("SET_ROLE");
        IslandPrivilege.register("SET_SETTINGS");
        IslandPrivilege.register("SET_WARP");
        IslandPrivilege.register("SIGN_INTERACT");
        IslandPrivilege.register("SPAWNER_BREAK");
        if(!ServerVersion.isLegacy())
            IslandPrivilege.register("TURTLE_EGG_TRAMPING");
        IslandPrivilege.register("UNCOOP_MEMBER");
        IslandPrivilege.register("USE");
        IslandPrivilege.register("VALUABLE_BREAK");
        IslandPrivilege.register("VILLAGER_TRADING");
        IslandPrivilege.register("WITHDRAW_MONEY");
    }

    private void loadUpgradeCostLoaders(){
        upgradesHandler.registerUpgradeCostLoader("money", new VaultUpgradeCostLoader());
        upgradesHandler.registerUpgradeCostLoader("placeholders", new PlaceholdersUpgradeCostLoader());
    }

    public static void log(String message){
        message = StringUtils.translateColors(message);
        if(message.contains(ChatColor.COLOR_CHAR + ""))
            Bukkit.getConsoleSender().sendMessage(ChatColor.getLastColors(message.substring(0, 2)) + "[" + plugin.getDescription().getName() + "] " + message);
        else
            plugin.getLogger().info(message);
    }

    public static void debug(String message){
        if(plugin.debugMode && (plugin.debugFilter == null || plugin.debugFilter.matcher(message.toUpperCase()).find()))
            plugin.getLogger().info("[DEBUG] " + message);
    }

    public static SuperiorSkyblockPlugin getPlugin(){
        return plugin;
    }

}
