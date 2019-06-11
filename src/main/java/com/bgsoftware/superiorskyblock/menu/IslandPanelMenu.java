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

    private static ItemStack membersButton, settingsButton, visitorsButton;
    private static Sound membersSound, settingsSound, visitorsSound;

    private IslandPanelMenu(){
        super("mainPage");
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        if(membersButton.equals(e.getCurrentItem())){
            SuperiorMenu previousMenu = previousMenus.get(superiorPlayer.getUniqueId());
            previousMenus.remove(superiorPlayer.getUniqueId());
            IslandMembersMenu.createInventory(superiorPlayer.getIsland()).openInventory(superiorPlayer, previousMenu);
        }
        else if (visitorsButton.equals(e.getCurrentItem())) {
            //TODO: Visitors panel
            plugin.getPanel().openVisitorsPanel(superiorPlayer, 1);
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

        membersButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.members"));
        settingsButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.settings"));
        visitorsButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.visitors"));

        membersSound = getSound(cfg.getString("main-panel.members.sound", ""));
        settingsSound = getSound(cfg.getString("main-panel.settings.sound", ""));
        visitorsSound = getSound(cfg.getString("main-panel.visitors.sound", ""));

        int membersSlot = cfg.getInt("main-panel.members.slot");
        int settingsSlot = cfg.getInt("main-panel.settings.slot");
        int visitorsSlot = cfg.getInt("main-panel.visitors.slot");

        inventory.setItem(membersSlot, membersButton);
        inventory.setItem(settingsSlot, settingsButton);
        inventory.setItem(visitorsSlot, visitorsButton);
    }

    public static IslandPanelMenu createInventory(){
        return new IslandPanelMenu();
    }

}
