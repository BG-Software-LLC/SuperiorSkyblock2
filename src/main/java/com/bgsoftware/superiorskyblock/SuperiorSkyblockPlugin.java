package com.bgsoftware.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.generator.WorldGenerator;
import com.bgsoftware.superiorskyblock.handlers.CommandsHandler;
import com.bgsoftware.superiorskyblock.handlers.BlockValuesHandler;
import com.bgsoftware.superiorskyblock.handlers.DataHandler;
import com.bgsoftware.superiorskyblock.handlers.GridHandler;
import com.bgsoftware.superiorskyblock.handlers.KeysHandler;
import com.bgsoftware.superiorskyblock.handlers.MenusHandler;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.handlers.PlayersHandler;
import com.bgsoftware.superiorskyblock.handlers.ProvidersHandler;
import com.bgsoftware.superiorskyblock.handlers.SchematicsHandler;
import com.bgsoftware.superiorskyblock.handlers.SettingsHandler;
import com.bgsoftware.superiorskyblock.handlers.UpgradesHandler;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.listeners.ChunksListener;
import com.bgsoftware.superiorskyblock.listeners.CustomEventsListener;
import com.bgsoftware.superiorskyblock.listeners.GeneratorsListener;
import com.bgsoftware.superiorskyblock.listeners.MenusListener;
import com.bgsoftware.superiorskyblock.listeners.PlayersListener;
import com.bgsoftware.superiorskyblock.listeners.ProtectionListener;
import com.bgsoftware.superiorskyblock.listeners.SettingsListener;
import com.bgsoftware.superiorskyblock.listeners.UpgradesListener;
import com.bgsoftware.superiorskyblock.metrics.Metrics;
import com.bgsoftware.superiorskyblock.nms.NMSAdapter;
import com.bgsoftware.superiorskyblock.nms.NMSBlocks;
import com.bgsoftware.superiorskyblock.nms.NMSTags;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.utils.reflections.ReflectField;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.tasks.CalcTask;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SuperiorSkyblockPlugin extends JavaPlugin implements SuperiorSkyblock {

    private static final ReflectField<SuperiorSkyblock> PLUGIN = new ReflectField<>(SuperiorSkyblockAPI.class, SuperiorSkyblock.class, "plugin");
    private static SuperiorSkyblockPlugin plugin;

    private GridHandler gridHandler = null;
    private BlockValuesHandler blockValuesHandler = null;
    private SchematicsHandler schematicsHandler = null;
    private PlayersHandler playersHandler = null;
    private MissionsHandler missionsHandler = null;
    private MenusHandler menusHandler = null;
    private KeysHandler keysHandler = null;
    private ProvidersHandler providersHandler = null;
    private UpgradesHandler upgradesHandler = null;
    private CommandsHandler commandsHandler = null;

    private SettingsHandler settingsHandler = null;
    private DataHandler dataHandler = null;

    private NMSAdapter nmsAdapter;
    private NMSTags nmsTags;
    private NMSBlocks nmsBlocks;

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

            try {
                safeEventsRegister(new BlocksListener(this));
                safeEventsRegister(new ChunksListener(this));
                safeEventsRegister(new CustomEventsListener(this));
                safeEventsRegister(new GeneratorsListener(this));
                safeEventsRegister(new MenusListener());
                safeEventsRegister(new PlayersListener(this));
                safeEventsRegister(new ProtectionListener(this));
                safeEventsRegister(new SettingsListener(this));
                safeEventsRegister(new UpgradesListener(this));
            }catch (RuntimeException ex){
                new HandlerLoadException("Cannot load plugin due to a missing event: " + ex.getMessage() + " - contact @Ome_R!",
                        HandlerLoadException.ErrorLevel.CONTINUE).printStackTrace();
                Bukkit.shutdown();
                return;
            }

            Executor.init(this);

            loadSortingTypes();
            loadIslandFlags();
            loadIslandPrivileges();

            EnchantsUtils.registerGlowEnchantment();

            loadWorld();

            EventsCaller.callPluginInitializeEvent(this);
            reloadPlugin(true);

            if (Updater.isOutdated()) {
                log("");
                log("A new version is available (v" + Updater.getLatestVersion() + ")!");
                log("Version's description: \"" + Updater.getVersionDescription() + "\"");
                log("");
            }

            ChunksProvider.init();

            Executor.sync(() -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);
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

                    if (playerIsland != null) {
                        ((SIsland) playerIsland).setLastTimeUpdate(-1);
                    }

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
            missionsHandler.saveMissionsData();

            for(Island island : gridHandler.getIslandsToPurge())
                island.disbandIsland();

            playersHandler.savePlayers();
            gridHandler.saveIslands();
            gridHandler.saveStackedBlocks();

            Bukkit.getOnlinePlayers().forEach(player -> {
                SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);
                player.closeInventory();
                nmsAdapter.setWorldBorder(superiorPlayer, null);
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
            System.out.println("Closing database...");
            dataHandler.closeConnection();
            Registry.clearCache();
        }
    }

    private boolean loadNMSAdapter(){
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAdapter = (NMSAdapter) Class.forName("com.bgsoftware.superiorskyblock.nms.NMSAdapter_" + version).newInstance();
            nmsTags = (NMSTags) Class.forName("com.bgsoftware.superiorskyblock.nms.NMSTags_" + version).newInstance();
            nmsBlocks = (NMSBlocks) Class.forName("com.bgsoftware.superiorskyblock.nms.NMSBlocks_" + version).newInstance();
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("all")
    private ChunkGenerator getGenerator(){
        if(worldGenerator == null) {
            File generatorFolder = new File(plugin.getDataFolder(), "world-generator");

            if (!generatorFolder.exists()) {
                generatorFolder.mkdirs();
            } else {
                try {
                    outerLoop:
                    for (File file : generatorFolder.listFiles()) {
                        Optional<Class<?>> generatorClassOptional = FileUtils.getClasses(file.toURL(), ChunkGenerator.class).stream().findFirst();
                        if (generatorClassOptional.isPresent()) {
                            Class<?> generatorClass = generatorClassOptional.get();
                            for(Constructor<?> constructor : generatorClass.getConstructors()){
                                if(constructor.getParameterCount() == 0){
                                    worldGenerator = (ChunkGenerator) generatorClass.newInstance();
                                    break outerLoop;
                                }
                                else if(constructor.getParameterTypes()[0].equals(JavaPlugin.class) ||
                                        constructor.getParameterTypes()[0].equals(SuperiorSkyblock.class)){
                                    worldGenerator = (ChunkGenerator) constructor.newInstance(this);
                                    break outerLoop;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log("An error occurred while loading the generator:");
                    ex.printStackTrace();
                }
            }

            if (worldGenerator == null)
                worldGenerator = new WorldGenerator();
        }

        return worldGenerator;
    }

    private void loadWorld(){
        settingsHandler = new SettingsHandler(this);
        Difficulty difficulty = Difficulty.valueOf(settingsHandler.worldsDifficulty);
        loadWorld(settingsHandler.islandWorldName, difficulty, World.Environment.NORMAL);
        if(settingsHandler.netherWorldEnabled)
            loadWorld(settingsHandler.netherWorldName, difficulty, World.Environment.NETHER);
        if(settingsHandler.endWorldEnabled)
            loadWorld(settingsHandler.endWorldName, difficulty, World.Environment.THE_END);
    }

    private void loadWorld(String worldName, Difficulty difficulty, World.Environment environment){
        World world = WorldCreator.name(worldName).type(WorldType.FLAT).environment(environment).generator(getGenerator()).createWorld();
        world.setDifficulty(difficulty);

        if(getServer().getPluginManager().isPluginEnabled("Multiverse-Core")){
            getServer().dispatchCommand(getServer().getConsoleSender(), "mv import " + worldName + " normal -g " + getName());
            getServer().dispatchCommand(getServer().getConsoleSender(), "mv modify set generator " + getName() + " " + worldName);
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return getGenerator();
    }

    public void reloadPlugin(boolean loadGrid){
        CalcTask.startTask();
        HeadUtils.readTextures(this);

        blockValuesHandler = new BlockValuesHandler(this);
        settingsHandler = new SettingsHandler(this);
        upgradesHandler = new UpgradesHandler(this);
        missionsHandler = new MissionsHandler(this);

        Locale.reload();

        commandsHandler = new CommandsHandler(this, settingsHandler.islandCommand);
        nmsAdapter.registerCommand(commandsHandler);

        if(loadGrid) {
            playersHandler = new PlayersHandler();
            gridHandler = new GridHandler(this);
        }
        else{
            Executor.sync(gridHandler::updateSpawn);
            gridHandler.syncUpgrades();
        }

        schematicsHandler = new SchematicsHandler(this);
        providersHandler = new ProvidersHandler(this);
        menusHandler = new MenusHandler(this);
        keysHandler = new KeysHandler();

        if (loadGrid) {
            try {
                dataHandler = new DataHandler(this);
            }catch(HandlerLoadException ex){
                if(!HandlerLoadException.handle(ex))
                    return;
            }
        }

        Executor.sync(() -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);
                Island island = gridHandler.getIslandAt(player.getLocation());
                nmsAdapter.setWorldBorder(superiorPlayer, island);
                if(island != null)
                    island.applyEffects(superiorPlayer);
            }
            //CropsTask.startTask();
        });
    }

    @Override
    public GridHandler getGrid(){
        return gridHandler;
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

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public SettingsHandler getSettings() {
        return settingsHandler;
    }

    public void setSettings(SettingsHandler settingsHandler){
        this.settingsHandler = settingsHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public NMSTags getNMSTags(){
        return nmsTags;
    }

    public NMSBlocks getNMSBlocks() {
        return nmsBlocks;
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
        IslandPrivilege.register("UNCOOP_MEMBER");
        IslandPrivilege.register("USE");
        IslandPrivilege.register("VALUABLE_BREAK");
        IslandPrivilege.register("WITHDRAW_MONEY");
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
            System.out.println("[SuperiorSkyblock2-DEBUG] " + message);
    }

    public static SuperiorSkyblockPlugin getPlugin(){
        return plugin;
    }

}
