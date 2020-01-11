package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuMissions extends SuperiorMenu {

    private static int playerSlot, islandSlot;

    private MenuMissions(SuperiorPlayer superiorPlayer){
        super("menuMissions", superiorPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(e.getRawSlot() == playerSlot){
            previousMove = false;
            MenuPlayerMissions.openInventory(superiorPlayer, this);
        }
        else if(e.getRawSlot() == islandSlot){
            previousMove = false;
            MenuIslandMissions.openInventory(superiorPlayer, this);
        }
    }

    public static void init(){
        MenuMissions menuMissions = new MenuMissions(null);

        File file = new File(plugin.getDataFolder(), "menus/missions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/missions.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMissions, "missions.yml", cfg);

        playerSlot = charSlots.getOrDefault(cfg.getString("player-missions", "@").charAt(0), Collections.singletonList(-1)).get(0);
        islandSlot = charSlots.getOrDefault(cfg.getString("island-missions", "^").charAt(0), Collections.singletonList(-1)).get(0);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuMissions(superiorPlayer).open(previousMenu);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/missions-gui.yml");

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

        char playerChar = itemChars[charCounter++], islandChar = itemChars[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.player-missions"), patternChars, playerChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.island-missions"), patternChars, islandChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("player-missions", playerChar + "");
        newMenu.set("island-missions", islandChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
