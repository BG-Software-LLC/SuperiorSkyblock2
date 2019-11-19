package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class IslandPanelMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static int membersSlot, settingsSlot, visitorsSlot;

    private IslandPanelMenu(){
        super("mainPage");
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
        Island island = superiorPlayer.getIsland();

        if(membersSlot == e.getRawSlot()){
            IslandMembersMenu.openInventory(superiorPlayer, this, island);
        }
        else if (settingsSlot == e.getRawSlot()) {
            if(superiorPlayer.hasPermission("superior.island.settings") && island.hasPermission(superiorPlayer, IslandPermission.SET_SETTINGS))
                IslandSettingsMenu.openInventory(superiorPlayer, this, island);
        }
        else if (visitorsSlot == e.getRawSlot()) {
            IslandVisitorsMenu.openInventory(superiorPlayer, this, island);
        }
    }

    @Override
    public Inventory getInventory() {
       return inventory;
    }

    public static void init(){
        IslandPanelMenu islandPanelMenu = new IslandPanelMenu();

        islandPanelMenu.resetData();

        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(islandPanelMenu, cfg.getConfigurationSection("main-panel"), 5, "&lIsland Panel");

        ItemStack membersButton = FileUtils.getItemStack(cfg.getConfigurationSection("main-panel.members"));
        ItemStack settingsButton = FileUtils.getItemStack(cfg.getConfigurationSection("main-panel.settings"));
        ItemStack visitorsButton = FileUtils.getItemStack(cfg.getConfigurationSection("main-panel.visitors"));

        membersSlot = cfg.getInt("main-panel.members.slot");
        settingsSlot = cfg.getInt("main-panel.settings.slot");
        visitorsSlot = cfg.getInt("main-panel.visitors.slot");

        islandPanelMenu.addSound(membersSlot, FileUtils.getSound(cfg.getConfigurationSection("main-panel.members.sound")));
        islandPanelMenu.addSound(settingsSlot, FileUtils.getSound(cfg.getConfigurationSection("main-panel.settings.sound")));
        islandPanelMenu.addSound(visitorsSlot, FileUtils.getSound(cfg.getConfigurationSection("main-panel.visitors.sound")));
        islandPanelMenu.addCommands(membersSlot, cfg.getStringList("main-panel.members.commands"));
        islandPanelMenu.addCommands(settingsSlot, cfg.getStringList("main-panel.settings.commands"));
        islandPanelMenu.addCommands(visitorsSlot, cfg.getStringList("main-panel.visitors.commands"));

        inventory.setItem(membersSlot, membersButton);
        inventory.setItem(settingsSlot, settingsButton);
        inventory.setItem(visitorsSlot, visitorsButton);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new IslandPanelMenu().open(superiorPlayer, previousMenu);
    }

}
