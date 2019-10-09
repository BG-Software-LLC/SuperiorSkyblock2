package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class IslandMainMissionsMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static int playerSlot, islandSlot;

    private IslandMainMissionsMenu(){
        super("mainMissionsPage");
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        if(e.getRawSlot() == playerSlot || e.getRawSlot() == islandSlot){
            IslandMissionsMenu.openInventory(superiorPlayer, this, e.getRawSlot() == islandSlot);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void init(){
        IslandMainMissionsMenu islandMainMissionsMenu = new IslandMainMissionsMenu();

        File file = new File(plugin.getDataFolder(), "guis/missions-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/missions-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(islandMainMissionsMenu, cfg.getConfigurationSection("main-panel"), 5, "&lMissions");

        ItemStack playerItem = FileUtils.getItemStack(cfg.getConfigurationSection("main-panel.player-missions"));
        playerSlot = cfg.getInt("main-panel.player-missions.slot");
        inventory.setItem(playerSlot, playerItem);

        ItemStack islandItem = FileUtils.getItemStack(cfg.getConfigurationSection("main-panel.island-missions"));
        islandSlot = cfg.getInt("main-panel.island-missions.slot");
        inventory.setItem(islandSlot, islandItem);

        islandMainMissionsMenu.addSound(playerSlot, FileUtils.getSound(cfg.getConfigurationSection("main-panel.player-missions.sound")));
        islandMainMissionsMenu.addSound(islandSlot, FileUtils.getSound(cfg.getConfigurationSection("main-panel.island-missions.sound")));
        islandMainMissionsMenu.addCommands(playerSlot, cfg.getStringList("main-panel.player-missions.commands"));
        islandMainMissionsMenu.addCommands(islandSlot, cfg.getStringList("main-panel.island-missions.commands"));
    }

    public static SuperiorMenu getMenu(){
        return new IslandMainMissionsMenu();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new IslandMainMissionsMenu().open(superiorPlayer, previousMenu);
    }

}
