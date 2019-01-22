package com.ome_r.superiorskyblock;

import com.ome_r.superiorskyblock.commands.CommandsHandler;
import com.ome_r.superiorskyblock.handlers.DataHandler;
import com.ome_r.superiorskyblock.handlers.GridHandler;
import com.ome_r.superiorskyblock.grid.WorldGenerator;
import com.ome_r.superiorskyblock.handlers.PanelHandler;
import com.ome_r.superiorskyblock.handlers.PlayersHandler;
import com.ome_r.superiorskyblock.handlers.SchematicsHandler;
import com.ome_r.superiorskyblock.handlers.SettingsHandler;
import com.ome_r.superiorskyblock.handlers.UpgradesHandler;
import com.ome_r.superiorskyblock.listeners.BlocksListener;
import com.ome_r.superiorskyblock.listeners.PanelListener;
import com.ome_r.superiorskyblock.listeners.UpgradesListener;
import com.ome_r.superiorskyblock.listeners.CustomEventsListener;
import com.ome_r.superiorskyblock.listeners.PlayersListener;
import com.ome_r.superiorskyblock.nms.NMSAdapter;
import com.ome_r.superiorskyblock.tasks.CalcTask;
import com.ome_r.superiorskyblock.tasks.SaveTask;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SuperiorSkyblock extends JavaPlugin implements Listener {

    private static SuperiorSkyblock plugin;

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

        getServer().getPluginManager().registerEvents(new CustomEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayersListener(this), this);
        getServer().getPluginManager().registerEvents(new PanelListener(this), this);
        getServer().getPluginManager().registerEvents(new UpgradesListener(this), this);

        loadNMSAdapter();

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("island").setExecutor(commandsHandler);
        getCommand("island").setTabCompleter(commandsHandler);

        boolean isWhitelisted = getServer().hasWhitelist();
        getServer().setWhitelist(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            loadWorld();

            reloadPlugin(true);
            getServer().setWhitelist(isWhitelisted);

            if(Updater.isOutdated()) {
                log("");
                log("A new version is available (v" + Updater.getLatestVersion() + ")!");
                log("Version's description: \"" + Updater.getVersionDescription() + "\"");
                log("");
            }
        });

    }

    @Override
    public void onDisable() {
        dataHandler.saveDatabase(false);
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            nmsAdapter.setWorldBorder(WrappedPlayer.of(player), null);
        }
        SaveTask.cancelTask();
        CalcTask.cancelTask();
    }

    private void loadNMSAdapter(){
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAdapter = (NMSAdapter) Class.forName("com.ome_r.superiorskyblock.nms.NMSAdapter_" + version).newInstance();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void loadWorld(){
        String worldName = (settingsHandler = new SettingsHandler(this)).islandWorld;
        WorldCreator.name(worldName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generator(new WorldGenerator()).createWorld();
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
            dataHandler = new DataHandler(this);
        }
        schematicsHandler = new SchematicsHandler(this);

        for(Player player : Bukkit.getOnlinePlayers())
            nmsAdapter.setWorldBorder(WrappedPlayer.of(player), gridHandler.getIslandAt(player.getLocation()));

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

    public static SuperiorSkyblock getPlugin(){
        return plugin;
    }

}
