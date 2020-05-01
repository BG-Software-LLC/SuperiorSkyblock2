package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class MenuBorderColor extends SuperiorMenu {

    private static List<Integer> greenColorSlot, blueColorSlot, redColorSlot;

    private MenuBorderColor(SuperiorPlayer superiorPlayer){
        super("menuBorderColor", superiorPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        BorderColor borderColor;

        if(greenColorSlot.contains(e.getRawSlot()))
            borderColor = BorderColor.GREEN;
        else if(blueColorSlot.contains(e.getRawSlot()))
            borderColor = BorderColor.BLUE;
        else if(redColorSlot.contains(e.getRawSlot()))
            borderColor = BorderColor.RED;
        else return;

        superiorPlayer.setBorderColor(borderColor);
        plugin.getNMSAdapter().setWorldBorder(superiorPlayer, plugin.getGrid().getIslandAt(superiorPlayer.getLocation()));

        Locale.BORDER_PLAYER_COLOR_UPDATED.send(superiorPlayer, StringUtils.format(borderColor.name()));

        Executor.sync(() -> {
            previousMove = false;
            superiorPlayer.asPlayer().closeInventory();
        }, 1L);
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    public static void init(){
        MenuBorderColor menuBorderColor = new MenuBorderColor(null);

        File file = new File(plugin.getDataFolder(), "menus/border-color.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/border-color.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuBorderColor, "border-color.yml", cfg);

        greenColorSlot = getSlots(cfg, "green-color", charSlots);
        redColorSlot = getSlots(cfg, "red-color", charSlots);
        blueColorSlot = getSlots(cfg, "blue-color", charSlots);

        charSlots.delete();

        menuBorderColor.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuBorderColor(superiorPlayer).open(previousMenu);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/border-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("border-gui.title"));
        newMenu.set("type", "HOPPER");

        char[] patternChars = new char[5];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("border-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("border-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char greenChar = itemChars[charCounter++], blueChar = itemChars[charCounter++], redChar = itemChars[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.green_color"), patternChars, greenChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.blue_color"), patternChars, blueChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.red_color"), patternChars, redChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("green-color", greenChar + "");
        newMenu.set("red-color", redChar + "");
        newMenu.set("blue-color", blueChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(1, patternChars, itemChars[charCounter]));

        return true;
    }

}
