package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuSlotsMap;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.SwitchIslandsSortingTypeButton;
import com.bgsoftware.superiorskyblock.core.menu.parser.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.GlobalWarpsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.parser.MenuParserUtils;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractSortedIslandsMenu;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class MenuGlobalWarps extends AbstractPagedMenu<MenuGlobalWarps.View, MenuGlobalWarps.Args, Island> {

    private final String selectedSortingType;
    private final String unselectedSortingType;

    private MenuGlobalWarps(MenuParseResult<View> parseResult, String selectedSortingType, String unselectedSortingType) {
        super(MenuIdentifiers.MENU_GLOBAL_WARPS, parseResult, false);
        this.selectedSortingType = selectedSortingType;
        this.unselectedSortingType = unselectedSortingType;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Override
    public CompletableFuture<MenuGlobalWarps.View> refreshView(MenuGlobalWarps.View view) {
        CompletableFuture<MenuGlobalWarps.View> res = new CompletableFuture<>();
        plugin.getGrid().sortIslands(view.getSortingType(), () ->
                super.refreshView(view).whenComplete((v, err) -> {
                    if (err != null) {
                        res.completeExceptionally(err);
                    } else {
                        res.complete(v);
                    }
                }));
        return res;
    }

    public void refreshViews(SortingType sortingType) {
        refreshViews(view -> view.getSortingType().equals(sortingType));
    }

    @Nullable
    public static MenuGlobalWarps createInstance() {
        MenuParseResult<MenuGlobalWarps.View> menuParseResult = MenuParserImpl.getInstance().loadMenu("global-warps.yml",
                MenuGlobalWarps::convertOldGUI, new GlobalWarpsPagedObjectButton.Builder());

        if (menuParseResult == null) {
            return null;
        }

        MenuSlotsMap menuSlotsMap = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<MenuGlobalWarps.View> patternBuilder = menuParseResult.getLayoutBuilder();

        String sort = cfg.getString("sort-islands", null);
        String selectedSortingType = cfg.getString("messages.selected-sorting-type");
        String unselectedSortingType = cfg.getString("messages.unselected-sorting-type");

        if (sort != null && cfg.isConfigurationSection("items")) {
            for (String itemSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemSection = cfg.getConfigurationSection("items." + itemSectionName);

                if (sort.equals(itemSectionName)) {
                    SwitchIslandsSortingTypeButton.Builder<MenuGlobalWarps.View> button = new SwitchIslandsSortingTypeButton.Builder<>();

                    for (String sortSectionName : itemSection.getKeys(false)) {
                        ConfigurationSection sortSection = cfg.getConfigurationSection("items." + itemSectionName + "." + sortSectionName);

                        SortingType sortingType = SortingType.getByName(sortSectionName);

                        if (sortingType == null) {
                            Log.warnFromFile("global-warps.yml", "The sorting type is invalid for the item ", itemSectionName);
                            continue;
                        }

                        String displayName = sortSection.getString("display-name", sortingType.getName());

                        button.addItem(sortingType, displayName, MenuParserUtils.getItemStack("menus/global-warps.yml", sortSection));
                    }

                    patternBuilder.mapButtons(menuSlotsMap.getSlots(itemSectionName), button);
                }
            }
        }

        return new MenuGlobalWarps(menuParseResult, selectedSortingType, unselectedSortingType);
    }

    public static class Args extends AbstractSortedIslandsMenu.Args {

        public Args(SortingType sortingType) {
            super(sortingType, Menus.MENU_GLOBAL_WARPS.selectedSortingType, Menus.MENU_GLOBAL_WARPS.unselectedSortingType);
        }

    }

    public static class View extends AbstractSortedIslandsMenu.View<View, Args> {

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu, args);
        }

        @Override
        protected List<Island> requestObjects() {
            return new SequentialListBuilder<Island>()
                    .filter(ISLANDS_FILTER)
                    .build(plugin.getGrid().getIslands(getSortingType()));
        }

        private final Predicate<Island> ISLANDS_FILTER = island -> {
            if (island.equals(getInventoryViewer().getIsland())) {
                return !island.getIslandWarps().isEmpty();
            } else {
                return island.getIslandWarps().values().stream().anyMatch(islandWarp -> !islandWarp.hasPrivateFlag());
            }
        };

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if (!oldFile.exists()) {
            return convertWarpsToSlots(newMenu);
        }

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

        if (cfg.isConfigurationSection("global-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("global-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("global-gui"),
                cfg.getConfigurationSection("global-gui.warp-item"),
                newMenu, patternChars,
                slotsChar, AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("visitor-warps", cfg.getConfigurationSection("global-gui.visitor-warps"));
        newMenu.set("warps", newMenu.getString("slots"));
        newMenu.set("slots", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

    private static boolean convertWarpsToSlots(YamlConfiguration newMenu) {
        if (newMenu.isString("warps")) {
            newMenu.set("slots", newMenu.getString("warps"));
            newMenu.set("warps", null);
            return true;
        }

        return false;
    }

}
