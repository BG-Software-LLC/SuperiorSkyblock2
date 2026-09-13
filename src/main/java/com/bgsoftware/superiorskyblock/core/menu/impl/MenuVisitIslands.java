package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuSlotsMap;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.SwitchIslandsSortingTypeButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.VisitIslandsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.parser.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.parser.MenuParserUtils;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractSortedIslandsMenu;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MenuVisitIslands extends AbstractPagedMenu<MenuVisitIslands.View, MenuVisitIslands.Args, Island> {

    private final String selectedSortingType;
    private final String unselectedSortingType;

    private MenuVisitIslands(MenuParseResult<View> parseResult, String selectedSortingType, String unselectedSortingType) {
        super(MenuIdentifiers.MENU_VISIT_ISLANDS, parseResult, false);
        this.selectedSortingType = selectedSortingType;
        this.unselectedSortingType = unselectedSortingType;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Override
    public CompletableFuture<MenuVisitIslands.View> refreshView(MenuVisitIslands.View view) {
        CompletableFuture<MenuVisitIslands.View> res = new CompletableFuture<>();
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
    public static MenuVisitIslands createInstance() {
        MenuParseResult<MenuVisitIslands.View> menuParseResult = MenuParserImpl.getInstance().loadMenu("visit-islands.yml",
                MenuVisitIslands::convertOldGUI, new VisitIslandsPagedObjectButton.Builder());

        if (menuParseResult == null) {
            return null;
        }

        MenuSlotsMap menuSlotsMap = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<View> patternBuilder = menuParseResult.getLayoutBuilder();

        String sort = cfg.getString("sort-islands", null);
        String selectedSortingType = cfg.getString("messages.selected-sorting-type");
        String unselectedSortingType = cfg.getString("messages.unselected-sorting-type");

        if (sort != null && cfg.isConfigurationSection("items")) {
            for (String itemSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemSection = cfg.getConfigurationSection("items." + itemSectionName);

                if (sort.equals(itemSectionName)) {
                    SwitchIslandsSortingTypeButton.Builder<MenuVisitIslands.View> button = new SwitchIslandsSortingTypeButton.Builder<>();

                    for (String sortSectionName : itemSection.getKeys(false)) {
                        ConfigurationSection sortSection = cfg.getConfigurationSection("items." + itemSectionName + "." + sortSectionName);

                        SortingType sortingType = SortingType.getByName(sortSectionName);

                        if (sortingType == null) {
                            Log.warnFromFile("visit-islands.yml", "The sorting type is invalid for the item ", itemSectionName);
                            continue;
                        }

                        String displayName = sortSection.getString("display-name", sortingType.getName());

                        button.addItem(sortingType, displayName, MenuParserUtils.getItemStack("menus/visit-islands.yml", sortSection));
                    }

                    patternBuilder.mapButtons(menuSlotsMap.getSlots(itemSectionName), button);
                }
            }
        }

        return new MenuVisitIslands(menuParseResult, selectedSortingType, unselectedSortingType);
    }

    public static class Args extends AbstractSortedIslandsMenu.Args {

        private final Dimension dimension;

        public Args(SortingType sortingType, Dimension dimension) {
            super(sortingType, Menus.MENU_VISIT_ISLANDS.selectedSortingType, Menus.MENU_VISIT_ISLANDS.unselectedSortingType);
            this.dimension = dimension;
        }

    }

    public static class View extends AbstractSortedIslandsMenu.View<View, Args> {

        private final Dimension dimension;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu, args);
            this.dimension = args.dimension;
            this.cachedTitleArgs = new Object[]{Formatters.CAPITALIZED_FORMATTER.format(dimension.getName())};
        }

        @Override
        protected List<Island> requestObjects() {
            return new SequentialListBuilder<Island>()
                    .filter(island -> island.getVisitorsPosition(dimension) != null)
                    .build(plugin.getGrid().getIslands(getSortingType()));
        }

        public Dimension getDimension() {
            return dimension;
        }

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "menus/global-warps.yml");

        if (!oldFile.exists()) {
            return false;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(oldFile);

        if (!config.isBoolean("visitor-warps")) {
            return false;
        }

        try {
            newMenu.loadFromString(config.saveToString());
        } catch (Exception error) {
            Log.warnFromFile("menus/visit-islands.yml", "An unexpected error occurred while saving file:", error);
        }

        if (newMenu.isBoolean("visitor-warps")) {
            newMenu.set("visitor-warps", null);
        }
        if (newMenu.isString("warps")) {
            newMenu.set("slots", newMenu.getString("warps"));
            newMenu.set("warps", null);
        }

        // The visit-islands menu performs the convert last, so it is responsible for removing the visitor-warps option
        // from the global-warps menu to prevent the convert from running again on next startups.
        if (config.isBoolean("visitor-warps")) {
            config.set("visitor-warps", null);

            try {
                config.save(oldFile);
            } catch (Exception error) {
                Log.warnFromFile("menus/global-warps.yml", "An unexpected error occurred while saving file:", error);
            }
        }

        return true;
    }

}
