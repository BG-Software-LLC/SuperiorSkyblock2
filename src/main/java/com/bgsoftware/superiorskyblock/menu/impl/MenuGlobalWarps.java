package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.GlobalWarpsPagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.menu.file.MenuParser;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MenuGlobalWarps extends PagedSuperiorMenu<MenuGlobalWarps, Island> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<MenuGlobalWarps, Island> menuPattern;

    public static boolean visitorWarps;

    private MenuGlobalWarps(SuperiorPlayer superiorPlayer) {
        super(menuPattern, superiorPlayer);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu);
    }

    @Override
    protected List<Island> requestObjects() {
        return getFilteredIslands().sorted(SortingComparators.WORTH_COMPARATOR).collect(Collectors.toList());
    }

    private Stream<Island> getFilteredIslands() {
        return plugin.getGrid().getIslands().stream()
                .filter(island -> {
                    if (visitorWarps)
                        return island.getVisitorsLocation() != null;
                    else if (island.equals(inventoryViewer.getIsland()))
                        return !island.getIslandWarps().isEmpty();
                    else
                        return island.getIslandWarps().values().stream().anyMatch(islandWarp -> !islandWarp.hasPrivateFlag());
                });
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuGlobalWarps, Island> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "global-warps.yml", MenuGlobalWarps::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        visitorWarps = cfg.getBoolean("visitor-warps", false);

        List<Integer> slots = new ArrayList<>();

        if (cfg.contains("warps"))
            slots.addAll(getSlots(cfg, "warps", menuPatternSlots));
        if (cfg.contains("slots"))
            slots.addAll(getSlots(cfg, "slots", menuPatternSlots));
        if (slots.isEmpty())
            slots.add(-1);

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(slots, new GlobalWarpsPagedObjectButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new MenuGlobalWarps(superiorPlayer).open(previousMenu);
    }

    public static void refreshMenus() {
        SuperiorMenu.refreshMenus(MenuGlobalWarps.class, superiorMenu -> true);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("global-gui.title"));

        int size = cfg.getInt("global-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("global-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("global-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("global-gui"),
                cfg.getConfigurationSection("global-gui.warp-item"),
                newMenu, patternChars,
                slotsChar, SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("visitor-warps", cfg.getConfigurationSection("global-gui.visitor-warps"));
        newMenu.set("warps", newMenu.getString("slots"));
        newMenu.set("slots", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}