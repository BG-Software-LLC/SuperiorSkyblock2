package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class MenuMissions extends SuperiorMenu {

    private static List<Integer> playerSlot, islandSlot;

    private MenuMissions(SuperiorPlayer superiorPlayer){
        super("menuMissions", superiorPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(playerSlot.contains(e.getRawSlot())){
            previousMove = false;
            MenuPlayerMissions.openInventory(superiorPlayer, this);
        }
        else if(islandSlot.contains(e.getRawSlot())){
            previousMove = false;
            MenuIslandMissions.openInventory(superiorPlayer, this);
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    public static void init(){
        MenuMissions menuMissions = new MenuMissions(null);

        File file = new File(plugin.getDataFolder(), "menus/missions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/missions.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMissions, "missions.yml", cfg);

        playerSlot = getSlots(cfg, "player-missions", charSlots);
        islandSlot = getSlots(cfg, "island-missions", charSlots);

        charSlots.delete();

        menuMissions.markCompleted();
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
