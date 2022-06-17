package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.BorderColorButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.BorderColorToggleButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class MenuBorderColor extends SuperiorMenu<MenuBorderColor> {

    private static RegularMenuPattern<MenuBorderColor> menuPattern;

    private MenuBorderColor(SuperiorPlayer superiorPlayer) {
        super(menuPattern, superiorPlayer);
    }


    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu);
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuBorderColor> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "border-color.yml", MenuBorderColor::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        if (cfg.isConfigurationSection("items")) {
            for (String itemsSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + itemsSectionName);

                if (!itemsSection.contains("enable-border") || !itemsSection.contains("disable-border"))
                    continue;

                patternBuilder.setButtons(menuPatternSlots.getSlots(itemsSectionName),
                        new BorderColorToggleButton.Builder()
                                .setEnabledItem(MenuParser.getItemStack("border-color.yml",
                                        itemsSection.getConfigurationSection("disable-border")))
                                .setDisabledItem(MenuParser.getItemStack("border-color.yml",
                                        itemsSection.getConfigurationSection("enable-border"))));
            }
        }

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "green-color", menuPatternSlots), new BorderColorButton.Builder()
                        .setBorderColor(BorderColor.GREEN))
                .mapButtons(getSlots(cfg, "red-color", menuPatternSlots), new BorderColorButton.Builder()
                        .setBorderColor(BorderColor.RED))
                .mapButtons(getSlots(cfg, "blue-color", menuPatternSlots), new BorderColorButton.Builder()
                        .setBorderColor(BorderColor.BLUE))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new MenuBorderColor(superiorPlayer).open(previousMenu);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/border-gui.yml");

        if (!oldFile.exists())
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

        if (cfg.contains("border-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("border-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char greenChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char blueChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char redChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.green_color"), patternChars, greenChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.blue_color"), patternChars, blueChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.red_color"), patternChars, redChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("green-color", greenChar + "");
        newMenu.set("red-color", redChar + "");
        newMenu.set("blue-color", blueChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(1, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
