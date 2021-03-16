package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class MenuControlPanel extends SuperiorMenu {

    private static List<Integer> membersSlot, settingsSlot, visitorsSlot;

    private MenuControlPanel(SuperiorPlayer superiorPlayer){
        super("menuControlPanel", superiorPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        Island island = superiorPlayer.getIsland();

        if(island == null)
            return;

        if(membersSlot.contains(e.getRawSlot())){
            MenuMembers.openInventory(superiorPlayer, this, island);
        }
        else if (settingsSlot.contains(e.getRawSlot())) {
            if(superiorPlayer.hasPermission("superior.island.settings")) {
                if(!superiorPlayer.hasPermission(IslandPrivileges.SET_SETTINGS)){
                    Locale.NO_SET_SETTINGS_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.SET_SETTINGS));
                    return;
                }

                MenuSettings.openInventory(superiorPlayer, this, island);
            }
        }
        else if (visitorsSlot.contains(e.getRawSlot())) {
            MenuVisitors.openInventory(superiorPlayer, this, island);
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    public static void init(){
        MenuControlPanel menuControlPanel = new MenuControlPanel(null);

        File file = new File(plugin.getDataFolder(), "menus/control-panel.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/control-panel.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuControlPanel, "control-panel.yml", cfg);

        membersSlot = getSlots(cfg, "members", charSlots);
        settingsSlot = getSlots(cfg, "settings", charSlots);
        visitorsSlot = getSlots(cfg, "visitors", charSlots);

        charSlots.delete();

        menuControlPanel.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuControlPanel(superiorPlayer).open(previousMenu);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("main-panel.title"));

        int size = cfg.getInt("main-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("main-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("main-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char membersChar = itemChars[charCounter++], settingsChar = itemChars[charCounter++], visitorsChar = itemChars[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.members"), patternChars, membersChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.settings"), patternChars, settingsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.visitors"), patternChars, visitorsChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("members", membersChar + "");
        newMenu.set("settings", settingsChar + "");
        newMenu.set("visitors", visitorsChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
