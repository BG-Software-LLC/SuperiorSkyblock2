package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.ChangeSortingTypeButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.TopIslandsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MenuTopIslands extends PagedSuperiorMenu<MenuTopIslands, Island> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<MenuTopIslands, Island> menuPattern;

    public static boolean sortGlowWhenSelected;

    private final Set<SortingType> alreadySorted = new HashSet<>();
    private SortingType sortingType;

    private MenuTopIslands(SuperiorPlayer superiorPlayer, SortingType sortingType) {
        super(menuPattern, superiorPlayer, true);
        this.sortingType = sortingType;
    }

    public SortingType getSortingType() {
        return sortingType;
    }

    public boolean setSortingType(SortingType sortingType) {
        this.sortingType = sortingType;
        this.updatePagedObjects();
        return this.alreadySorted.add(sortingType);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, sortingType);
    }

    @Override
    protected List<Island> requestObjects() {
        return plugin.getGrid().getIslands(sortingType);
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuTopIslands, Island> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "top-islands.yml", MenuTopIslands::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        sortGlowWhenSelected = cfg.getBoolean("sort-glow-when-selected", false);

        patternBuilder.mapButtons(getSlots(cfg, "worth-sort", menuPatternSlots),
                new ChangeSortingTypeButton.Builder().setSortingType(SortingTypes.BY_WORTH));

        patternBuilder.mapButtons(getSlots(cfg, "level-sort", menuPatternSlots),
                new ChangeSortingTypeButton.Builder().setSortingType(SortingTypes.BY_LEVEL));

        patternBuilder.mapButtons(getSlots(cfg, "rating-sort", menuPatternSlots),
                new ChangeSortingTypeButton.Builder().setSortingType(SortingTypes.BY_RATING));

        patternBuilder.mapButtons(getSlots(cfg, "players-sort", menuPatternSlots),
                new ChangeSortingTypeButton.Builder().setSortingType(SortingTypes.BY_PLAYERS));

        if (cfg.isConfigurationSection("items")) {
            for (String itemSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemSection = cfg.getConfigurationSection("items." + itemSectionName);

                if (!itemSection.isString("sorting-type"))
                    continue;

                SortingType sortingType = SortingType.getByName(itemSection.getString("sorting-type"));

                if (sortingType == null) {
                    SuperiorSkyblockPlugin.log("&c[top-islands.yml] The sorting type is invalid for the item " + itemSectionName);
                    continue;
                }

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemSectionName),
                        new ChangeSortingTypeButton.Builder().setSortingType(sortingType));
            }
        }

        if (cfg.isString("slots")) {
            boolean configuredSelfPlayerButton = false;

            for (char slotsChar : cfg.getString("slots", "").toCharArray()) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + slotsChar);

                if (itemsSection == null)
                    continue;

                TopIslandsPagedObjectButton.Builder slotsBuilder = new TopIslandsPagedObjectButton.Builder()
                        .setIslandItem(MenuParser.getItemStack("top-islands.yml", itemsSection.getConfigurationSection("island")))
                        .setNoIslandItem(MenuParser.getItemStack("top-islands.yml", itemsSection.getConfigurationSection("no-island")))
                        .setIslandSound(MenuParser.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".island")))
                        .setNoIslandSound(MenuParser.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".no-island")))
                        .setIslandCommands(cfg.getStringList("commands." + slotsChar + ".island"))
                        .setNoIslandCommands(cfg.getStringList("commands." + slotsChar + ".no-island"));

                patternBuilder.mapButtons(menuPatternSlots.getSlots(slotsChar), slotsBuilder);

                if (!configuredSelfPlayerButton) {
                    configuredSelfPlayerButton = true;
                    patternBuilder.mapButtons(getSlots(cfg, "player-island", menuPatternSlots),
                            slotsBuilder.copy().setPlayerSelfIsland(true));
                }
            }
        }

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, SortingType sortingType) {
        plugin.getGrid().sortIslands(sortingType, () -> new MenuTopIslands(superiorPlayer, sortingType).open(previousMenu));
    }

    public static void refreshMenus(SortingType sortingType) {
        SuperiorMenu.refreshMenus(MenuTopIslands.class, superiorMenu -> superiorMenu.sortingType.equals(sortingType));
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/top-islands.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("top-islands.title"));

        int size = cfg.getInt("top-islands.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("top-islands.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("top-islands.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char worthChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char levelChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char ratingChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char playersChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char playerIslandChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        for (String slot : cfg.getString("top-islands.slots").split(","))
            patternChars[Integer.parseInt(slot)] = slotsChar;

        ConfigurationSection islandItemSection = cfg.getConfigurationSection("top-islands.island-item");
        newMenu.set("items." + slotsChar + ".island", islandItemSection);
        newMenu.set("sounds." + slotsChar + ".island", islandItemSection.getConfigurationSection("sound"));
        islandItemSection.set("sound", null);

        ConfigurationSection noIslandItemSection = cfg.getConfigurationSection("top-islands.no-island-item");
        newMenu.set("items." + slotsChar + ".no-island", noIslandItemSection);
        newMenu.set("sounds." + slotsChar + ".no-island", noIslandItemSection.getConfigurationSection("sound"));
        noIslandItemSection.set("sound", null);

        if (cfg.contains("top-islands.worth-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.worth-sort"), patternChars, worthChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if (cfg.contains("top-islands.level-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.level-sort"), patternChars, levelChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if (cfg.contains("top-islands.rating-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.rating-sort"), patternChars, ratingChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if (cfg.contains("top-islands.players-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.players-sort"), patternChars, playersChar,
                    itemsSection, commandsSection, soundsSection);
        }

        if (cfg.contains("player-island-slot"))
            patternChars[cfg.getInt("player-island-slot")] = playerIslandChar;

        newMenu.set("worth-sort", worthChar);
        newMenu.set("level-sort", levelChar);
        newMenu.set("rating-sort", ratingChar);
        newMenu.set("players-sort", playersChar);
        newMenu.set("player-island", playerIslandChar);
        newMenu.set("sort-glow-when-selected", false);

        char invalidChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        newMenu.set("slots", slotsChar);
        newMenu.set("previous-page", invalidChar);
        newMenu.set("current-page", invalidChar);
        newMenu.set("next-page", invalidChar);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
