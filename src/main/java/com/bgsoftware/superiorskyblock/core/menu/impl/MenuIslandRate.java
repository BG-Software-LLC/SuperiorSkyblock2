package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.RateIslandButton;
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

public class MenuIslandRate extends SuperiorMenu<MenuIslandRate> {

    private static RegularMenuPattern<MenuIslandRate> menuPattern;

    private final Island island;

    private MenuIslandRate(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
    }

    public Island getTargetIsland() {
        return island;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuIslandRate> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "island-rate.yml",
                MenuIslandRate::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "zero-stars", menuPatternSlots), new RateIslandButton.Builder()
                        .setRating(Rating.ZERO_STARS))
                .mapButtons(getSlots(cfg, "one-star", menuPatternSlots), new RateIslandButton.Builder()
                        .setRating(Rating.ONE_STAR))
                .mapButtons(getSlots(cfg, "two-stars", menuPatternSlots), new RateIslandButton.Builder()
                        .setRating(Rating.TWO_STARS))
                .mapButtons(getSlots(cfg, "three-stars", menuPatternSlots), new RateIslandButton.Builder()
                        .setRating(Rating.THREE_STARS))
                .mapButtons(getSlots(cfg, "four-stars", menuPatternSlots), new RateIslandButton.Builder()
                        .setRating(Rating.FOUR_STARS))
                .mapButtons(getSlots(cfg, "five-stars", menuPatternSlots), new RateIslandButton.Builder()
                        .setRating(Rating.FIVE_STARS))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuIslandRate(superiorPlayer, island).open(previousMenu);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/ratings-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("rate-gui.title"));
        newMenu.set("type", "HOPPER");

        char[] patternChars = new char[5];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("rate-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("rate-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char oneStarChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char twoStarsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char threeStarsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char fourStarsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char fiveStarsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.one_star"), patternChars, oneStarChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.two_stars"), patternChars, twoStarsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.three_stars"), patternChars, threeStarsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.four_stars"), patternChars, fourStarsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.five_stars"), patternChars, fiveStarsChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("one-star", oneStarChar + "");
        newMenu.set("two-stars", twoStarsChar + "");
        newMenu.set("three-stars", threeStarsChar + "");
        newMenu.set("four-stars", fourStarsChar + "");
        newMenu.set("five-stars", fiveStarsChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(1, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
