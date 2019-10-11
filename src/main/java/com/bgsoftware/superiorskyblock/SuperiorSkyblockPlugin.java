package com.bgsoftware.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandsHandler;
import com.bgsoftware.superiorskyblock.grid.WorldGenerator;
import com.bgsoftware.superiorskyblock.handlers.BlockValuesHandler;
import com.bgsoftware.superiorskyblock.handlers.DataHandler;
import com.bgsoftware.superiorskyblock.handlers.GridHandler;
import com.bgsoftware.superiorskyblock.handlers.MenusHandler;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.handlers.PlayersHandler;
import com.bgsoftware.superiorskyblock.handlers.ProvidersHandler;
import com.bgsoftware.superiorskyblock.handlers.SchematicsHandler;
import com.bgsoftware.superiorskyblock.handlers.SettingsHandler;
import com.bgsoftware.superiorskyblock.handlers.UpgradesHandler;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.listeners.CustomEventsListener;
import com.bgsoftware.superiorskyblock.listeners.MenusListener;
import com.bgsoftware.superiorskyblock.listeners.PlayersListener;
import com.bgsoftware.superiorskyblock.listeners.ProtectionListener;
import com.bgsoftware.superiorskyblock.listeners.SettingsListener;
import com.bgsoftware.superiorskyblock.listeners.UpgradesListener;
import com.bgsoftware.superiorskyblock.metrics.Metrics;
import com.bgsoftware.superiorskyblock.nms.NMSAdapter;
import com.bgsoftware.superiorskyblock.tasks.CalcTask;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public final class SuperiorSkyblockPlugin extends JavaPlugin implements SuperiorSkyblock {

    private static SuperiorSkyblockPlugin plugin;

    private GridHandler gridHandler;
    private BlockValuesHandler blockValuesHandler;
    private PlayersHandler playersHandler;
    private SchematicsHandler schematicsHandler;
    private SettingsHandler settingsHandler;
    private DataHandler dataHandler;
    private UpgradesHandler upgradesHandler;
    private ProvidersHandler providersHandler;
    private MissionsHandler missionsHandler;
    private MenusHandler menusHandler;

    private NMSAdapter nmsAdapter;

    @Override
    public void onEnable() {
        plugin = this;
        new Metrics(this);


        getServer().getPluginManager().registerEvents(new CustomEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayersListener(this), this);
        getServer().getPluginManager().registerEvents(new UpgradesListener(this), this);
        getServer().getPluginManager().registerEvents(new MenusListener(), this);
        getServer().getPluginManager().registerEvents(new SettingsListener(this), this);

        loadNMSAdapter();
        loadAPI();

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("island").setExecutor(commandsHandler);
        getCommand("island").setTabCompleter(commandsHandler);

        loadWorld();

        loadSortingTypes();
        reloadPlugin(true);

        if (Updater.isOutdated()) {
            log("");
            log("A new version is available (v" + Updater.getLatestVersion() + ")!");
            log("Version's description: \"" + Updater.getVersionDescription() + "\"");
            log("");
        }

        Executor.sync(() -> {
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);
                superiorPlayer.updateLastTimeStatus();
                if(superiorPlayer.hasIslandFlyEnabled() && superiorPlayer.isInsideIsland()){
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }
        }, 1L);
    }

    @Override
    public void onDisable() {
        dataHandler.saveDatabase(false);

        for(Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);
            superiorPlayer.updateLastTimeStatus();
            nmsAdapter.setWorldBorder(superiorPlayer, null);
            if(superiorPlayer.hasIslandFlyEnabled()){
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }

        CalcTask.cancelTask();
        Executor.close();
        dataHandler.closeConnection();
    }

    private void loadNMSAdapter(){
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAdapter = (NMSAdapter) Class.forName("com.bgsoftware.superiorskyblock.nms.NMSAdapter_" + version).newInstance();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void loadWorld(){
        String worldName = (settingsHandler = new SettingsHandler(this)).islandWorld;
        WorldCreator.name(worldName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generator(new WorldGenerator()).createWorld();

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
        return new WorldGenerator();
    }

    public void reloadPlugin(boolean loadGrid){
        blockValuesHandler = new BlockValuesHandler(this);
        settingsHandler = new SettingsHandler(this);
        upgradesHandler = new UpgradesHandler(this);
        missionsHandler = new MissionsHandler(this);

        if(loadGrid) {
            playersHandler = new PlayersHandler();
            gridHandler = new GridHandler(this);
        }

        schematicsHandler = new SchematicsHandler(this);
        providersHandler = new ProvidersHandler(this);
        menusHandler = new MenusHandler();

        Executor.sync(() -> {
            if (loadGrid)
                dataHandler = new DataHandler(this);

            for(Player player : Bukkit.getOnlinePlayers())
                nmsAdapter.setWorldBorder(SSuperiorPlayer.of(player), gridHandler.getIslandAt(player.getLocation()));

        });

        Locale.reload();
        CalcTask.startTask();
    }

    private void loadSortingTypes(){
        SortingType.register("WORTH", SortingComparators.WORTH_COMPARATOR);
        SortingType.register("LEVEL", SortingComparators.LEVEL_COMPARATOR);
        SortingType.register("RATING", SortingComparators.RATING_COMPARATOR);
        SortingType.register("PLAYERS", SortingComparators.PLAYERS_COMPARATOR);
    }

    @Override
    public MenusManager getMenus() {
        return menusHandler;
    }

    @Override
    public MissionsHandler getMissions() {
        return missionsHandler;
    }

    public ProvidersHandler getProviders() {
        return providersHandler;
    }

    public UpgradesHandler getUpgrades() {
        return upgradesHandler;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public SettingsHandler getSettings() {
        return settingsHandler;
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
    public GridHandler getGrid(){
        return gridHandler;
    }

    @Override
    public BlockValuesHandler getBlockValues() {
        return blockValuesHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public static void log(String message){
        message = ChatColor.translateAlternateColorCodes('&', message);
        if(message.contains(ChatColor.COLOR_CHAR + ""))
            Bukkit.getConsoleSender().sendMessage(ChatColor.getLastColors(message.substring(0, 2)) + "[" + plugin.getDescription().getName() + "] " + message);
        else
            plugin.getLogger().info(message);
    }

    public static SuperiorSkyblockPlugin getPlugin(){
        return plugin;
    }

}
