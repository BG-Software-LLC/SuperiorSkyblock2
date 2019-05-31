package com.bgsoftware.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.commands.CommandsHandler;
import com.bgsoftware.superiorskyblock.grid.WorldGenerator;
import com.bgsoftware.superiorskyblock.handlers.DataHandler;
import com.bgsoftware.superiorskyblock.handlers.GridHandler;
import com.bgsoftware.superiorskyblock.handlers.PanelHandler;
import com.bgsoftware.superiorskyblock.handlers.PlayersHandler;
import com.bgsoftware.superiorskyblock.handlers.SchematicsHandler;
import com.bgsoftware.superiorskyblock.handlers.SettingsHandler;
import com.bgsoftware.superiorskyblock.handlers.UpgradesHandler;
import com.bgsoftware.superiorskyblock.hooks.FAWEHook;
import com.bgsoftware.superiorskyblock.hooks.LeaderHeadsHook;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.hooks.WildStackerHook;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.listeners.CustomEventsListener;
import com.bgsoftware.superiorskyblock.listeners.PanelListener;
import com.bgsoftware.superiorskyblock.listeners.PlayersListener;
import com.bgsoftware.superiorskyblock.listeners.ProtectionListener;
import com.bgsoftware.superiorskyblock.listeners.UpgradesListener;
import com.bgsoftware.superiorskyblock.metrics.Metrics;
import com.bgsoftware.superiorskyblock.nms.NMSAdapter;
import com.bgsoftware.superiorskyblock.tasks.CalcTask;
import com.bgsoftware.superiorskyblock.tasks.SaveTask;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import org.bukkit.Bukkit;
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
    private PlayersHandler playersHandler;
    private SchematicsHandler schematicsHandler;
    private SettingsHandler settingsHandler;
    private DataHandler dataHandler;
    private PanelHandler panelHandler;
    private UpgradesHandler upgradesHandler;

    private NMSAdapter nmsAdapter;

    @Override
    public void onEnable() {
        plugin = this;
        new Metrics(this);

        getServer().getPluginManager().registerEvents(new CustomEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayersListener(this), this);
        getServer().getPluginManager().registerEvents(new PanelListener(this), this);
        getServer().getPluginManager().registerEvents(new UpgradesListener(this), this);

        loadNMSAdapter();
        loadAPI();

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("island").setExecutor(commandsHandler);
        getCommand("island").setTabCompleter(commandsHandler);

        boolean isWhitelisted = getServer().hasWhitelist();
        getServer().setWhitelist(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit"))
                    FAWEHook.register();
                if(Bukkit.getPluginManager().isPluginEnabled("LeaderHeads"))
                    LeaderHeadsHook.register();
                WildStackerHook.register(this);

                reloadPlugin(true);

                PlaceholderHook.register(this);

                loadWorld();

                getServer().setWhitelist(isWhitelisted);

                if (Updater.isOutdated()) {
                    log("");
                    log("A new version is available (v" + Updater.getLatestVersion() + ")!");
                    log("Version's description: \"" + Updater.getVersionDescription() + "\"");
                    log("");
                }
            }catch(Exception ex){
                ex.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(this);
            }
        });

    }

    @Override
    public void onDisable() {
        dataHandler.saveDatabase(false);
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            nmsAdapter.setWorldBorder(SSuperiorPlayer.of(player), null);
        }
        dataHandler.closeConnection();
        SaveTask.cancelTask();
        CalcTask.cancelTask();
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
        settingsHandler = new SettingsHandler(this);
        panelHandler = new PanelHandler(this);
        upgradesHandler = new UpgradesHandler(this);

        if(loadGrid) {
            gridHandler = new GridHandler(this);
            playersHandler = new PlayersHandler();
        }else {
            gridHandler.reloadBlockValues();
        }
        schematicsHandler = new SchematicsHandler(this);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (loadGrid)
                dataHandler = new DataHandler(this);

            for(Player player : Bukkit.getOnlinePlayers())
                nmsAdapter.setWorldBorder(SSuperiorPlayer.of(player), gridHandler.getIslandAt(player.getLocation()));

        });

        Locale.reload();
        SaveTask.startTask();
        CalcTask.startTask();
    }

    public UpgradesHandler getUpgrades() {
        return upgradesHandler;
    }

    public PanelHandler getPanel() {
        return panelHandler;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public SettingsHandler getSettings() {
        return settingsHandler;
    }

    public SchematicsHandler getSchematics() {
        return schematicsHandler;
    }

    public PlayersHandler getPlayers() {
        return playersHandler;
    }

    public GridHandler getGrid(){
        return gridHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public static void log(String message){
        plugin.getLogger().info(message);
    }

    public static SuperiorSkyblockPlugin getPlugin(){
        return plugin;
    }

}
