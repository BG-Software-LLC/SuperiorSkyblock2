package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.RatingsPagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MenuIslandRatings extends PagedSuperiorMenu<MenuIslandRatings.RatingInfo> {

    private static PagedMenuPattern<RatingInfo> menuPattern;

    private final Island island;

    private MenuIslandRatings(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
    }

    public Island getTargetIsland() {
        return island;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected List<RatingInfo> requestObjects() {
        return island.getRatings().entrySet().stream()
                .map(entry -> new RatingInfo(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<RatingInfo> patternBuilder = new PagedMenuPattern.Builder<>();

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "island-ratings.yml", MenuIslandRatings::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots), new RatingsPagedObjectButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuIslandRatings(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        SuperiorMenu.refreshMenus(MenuIslandRatings.class, superiorMenu -> superiorMenu.island.equals(island));
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

        newMenu.set("title", cfg.getString("ratings-gui.title"));

        int size = cfg.getInt("ratings-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("ratings-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("ratings-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("ratings-gui"),
                cfg.getConfigurationSection("ratings-gui.rate-item"), newMenu, patternChars,
                slotsChar, SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++], SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

    public static class RatingInfo {

        private final UUID playerUUID;
        private final Rating rating;

        public RatingInfo(UUID playerUUID, Rating rating) {
            this.playerUUID = playerUUID;
            this.rating = rating;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public Rating getRating() {
            return rating;
        }

    }

}
