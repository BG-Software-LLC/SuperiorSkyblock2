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
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.database.Query;
import com.bgsoftware.superiorskyblock.utils.database.StatementHolder;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import com.bgsoftware.superiorskyblock.utils.reflections.ReflectionUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.tasks.CalcTask;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.tasks.CropsTask;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SuperiorSkyblockPlugin extends JavaPlugin implements SuperiorSkyblock {

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

        loadAPI();

        if(!loadNMSAdapter() || !ReflectionUtils.init()) {
            shouldEnable = false;
        }
    }

    @Override
    public void onEnable() {
        try {
            if (!shouldEnable) {
                Bukkit.shutdown();
                return;
            }

            getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
            getServer().getPluginManager().registerEvents(new ChunksListener(this), this);
            getServer().getPluginManager().registerEvents(new CustomEventsListener(this), this);
            getServer().getPluginManager().registerEvents(new GeneratorsListener(this), this);
            getServer().getPluginManager().registerEvents(new MenusListener(), this);
            getServer().getPluginManager().registerEvents(new PlayersListener(this), this);
            getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
            getServer().getPluginManager().registerEvents(new SettingsListener(this), this);
            getServer().getPluginManager().registerEvents(new UpgradesListener(this), this);

            Executor.init(this);

            loadSortingTypes();
            loadIslandFlags();
            loadIslandPrivileges();

            EnchantsUtils.registerGlowEnchantment();

            loadWorld();

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
        CropsTask.cancelTask();
        try {
            dataHandler.saveDatabase(false);
            missionsHandler.saveMissionsData();

            for(Island island : gridHandler.getIslandsToPurge())
                island.disbandIsland();

            Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            if(!players.isEmpty()){
                long lastTimeStatus = System.currentTimeMillis() / 1000;

                StatementHolder playerStatusHolder = Query.PLAYER_SET_LAST_STATUS.getStatementHolder();
                playerStatusHolder.prepareBatch();
                players.stream().map(SSuperiorPlayer::of).forEach(superiorPlayer ->
                        playerStatusHolder.setString(lastTimeStatus + "").setString(superiorPlayer.getUniqueId() + "").addBatch());
                playerStatusHolder.execute(false);

                List<Island> islandList = players.stream().map(player -> SSuperiorPlayer.of(player).getIsland())
                        .filter(Objects::nonNull).collect(Collectors.toList());

                if(!islandList.isEmpty()) {
                    {
                        StatementHolder islandStatusHolder = Query.ISLAND_SET_LAST_TIME_UPDATE.getStatementHolder();
                        islandStatusHolder.prepareBatch();
                        islandList.forEach(island -> islandStatusHolder.setLong(lastTimeStatus).setString(island.getOwner().getUniqueId() + "").addBatch());
                        islandStatusHolder.execute(false);
                    }
                    {
                        StatementHolder islandCountsHolder = Query.ISLAND_SET_BLOCK_COUNTS.getStatementHolder();
                        islandCountsHolder.prepareBatch();
                        islandList.forEach(island -> islandCountsHolder
                                .setString(IslandSerializer.serializeBlockCounts(((SIsland) island).getRawBlockCounts()))
                                .setString(island.getOwner().getUniqueId() + "")
                                .addBatch()
                        );
                        islandCountsHolder.execute(false);
                    }
                }

                players.forEach(player -> {
                    SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);
                    player.closeInventory();
                    nmsAdapter.setWorldBorder(superiorPlayer, null);
                    if (superiorPlayer.hasIslandFlyEnabled()) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                });
            }
        }catch(Exception ignored){
            //Ignore
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
                    for (File file : generatorFolder.listFiles()) {
                        Optional<Class<?>> generatorClass = FileUtils.getClasses(file.toURL(), ChunkGenerator.class).stream().findFirst();
                        if (generatorClass.isPresent()) {
                            worldGenerator = (ChunkGenerator) generatorClass.get().newInstance();
                            break;
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
        String worldName = (settingsHandler = new SettingsHandler(this)).islandWorldName;
        loadWorld(worldName, World.Environment.NORMAL);
        if(settingsHandler.netherWorldEnabled)
            loadWorld(worldName + "_nether", World.Environment.NETHER);
        if(settingsHandler.endWorldEnabled)
            loadWorld(worldName + "_the_end", World.Environment.THE_END);
    }

    private void loadWorld(String worldName, World.Environment environment){
        WorldCreator.name(worldName).type(WorldType.FLAT).environment(environment).generator(getGenerator()).createWorld();

        if(getServer().getPluginManager().isPluginEnabled("Multiverse-Core")){
            getServer().dispatchCommand(getServer().getConsoleSender(), "mv import " + worldName + " normal -g " + getName());
            getServer().dispatchCommand(getServer().getConsoleSender(), "mv modify set generator " + getName() + " " + worldName);
        }
    }

    private void loadAPI(){
        try{
            Field plugin = SuperiorSkyblockAPI.class.getDeclaredField("plugin");
            plugin.setAccessible(true);
            plugin.set(null, this);
            plugin.setAccessible(false);
        }catch(Exception ignored){}
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return getGenerator();
    }

    public void reloadPlugin(boolean loadGrid){
        CalcTask.startTask();

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
        }

        schematicsHandler = new SchematicsHandler(this);
        providersHandler = new ProvidersHandler(this);
        menusHandler = new MenusHandler(this);
        keysHandler = new KeysHandler(this);

        if (loadGrid) {
            try {
                dataHandler = new DataHandler(this);
            }catch(HandlerLoadException ex){
                if(!HandlerLoadException.handle(ex))
                    return;
            }
        }

        Executor.sync(() -> {
            for(Player player : Bukkit.getOnlinePlayers())
                nmsAdapter.setWorldBorder(SSuperiorPlayer.of(player), gridHandler.getIslandAt(player.getLocation()));
            CropsTask.startTask();
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
        IslandPrivilege.register("FLY");
        IslandPrivilege.register("FISH");
        IslandPrivilege.register("INTERACT");
        IslandPrivilege.register("INVITE_MEMBER");
        IslandPrivilege.register("ITEM_FRAME");
        IslandPrivilege.register("KICK_MEMBER");
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
