package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuControlPanel extends SuperiorMenu {

    private static int membersSlot, settingsSlot, visitorsSlot;

    private MenuControlPanel(SuperiorPlayer superiorPlayer){
        super("menuControlPanel", superiorPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        Island island = superiorPlayer.getIsland();

        if(membersSlot == e.getRawSlot()){
            MenuMembers.openInventory(superiorPlayer, this, island);
        }
        else if (settingsSlot == e.getRawSlot()) {
            if(superiorPlayer.hasPermission("superior.island.settings") && island.hasPermission(superiorPlayer, IslandPermission.SET_SETTINGS))
                MenuSettings.openInventory(superiorPlayer, this, island);
        }
        else if (visitorsSlot == e.getRawSlot()) {
            MenuVisitors.openInventory(superiorPlayer, this, island);
        }
    }

    public static void init(){
        MenuControlPanel menuControlPanel = new MenuControlPanel(null);

        File file = new File(plugin.getDataFolder(), "menus/control-panel.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/control-panel.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuControlPanel, cfg);

        membersSlot = charSlots.getOrDefault(cfg.getString("members", "%").charAt(0), Collections.singletonList(-1)).get(0);
        settingsSlot = charSlots.getOrDefault(cfg.getString("settings", "*").charAt(0), Collections.singletonList(-1)).get(0);
        visitorsSlot = charSlots.getOrDefault(cfg.getString("visitors", "^").charAt(0), Collections.singletonList(-1)).get(0);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuControlPanel(superiorPlayer).open(previousMenu);
    }

}
