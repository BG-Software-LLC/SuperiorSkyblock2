package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class IslandPanelMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static int membersSlot, settingsSlot, visitorsSlot;
    private static Sound membersSound, settingsSound, visitorsSound;

    private IslandPanelMenu(){
        super("mainPage");
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        if(membersSlot == e.getRawSlot()){
            IslandMembersMenu.openInventory(superiorPlayer, this, superiorPlayer.getIsland());
        }
        else if (visitorsSlot == e.getRawSlot()) {
            IslandVisitorsMenu.openInventory(superiorPlayer, this, superiorPlayer.getIsland());
        }
    }

    @Override
    public Inventory getInventory() {
       return inventory;
    }

    public static void init(){
        IslandPanelMenu islandPanelMenu = new IslandPanelMenu();

        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandPanelMenu, cfg.getConfigurationSection("main-panel"), 5, "&lIsland Panel");

        ItemStack membersButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.members"));
        ItemStack settingsButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.settings"));
        ItemStack visitorsButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.visitors"));

        membersSound = getSound(cfg.getString("main-panel.members.sound", ""));
        settingsSound = getSound(cfg.getString("main-panel.settings.sound", ""));
        visitorsSound = getSound(cfg.getString("main-panel.visitors.sound", ""));

        membersSlot = cfg.getInt("main-panel.members.slot");
        settingsSlot = cfg.getInt("main-panel.settings.slot");
        visitorsSlot = cfg.getInt("main-panel.visitors.slot");

        inventory.setItem(membersSlot, membersButton);
        inventory.setItem(settingsSlot, settingsButton);
        inventory.setItem(visitorsSlot, visitorsButton);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new IslandPanelMenu().open(superiorPlayer, previousMenu);
    }

}
