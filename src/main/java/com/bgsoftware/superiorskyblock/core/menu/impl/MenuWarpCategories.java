package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.DynamicArray;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.WarpCategoryPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class MenuWarpCategories extends AbstractPagedMenu<MenuWarpCategories.View, IslandViewArgs, WarpCategory> {

    private final List<String> editLore;
    private final int rowsSize;

    private MenuWarpCategories(MenuParseResult<View> parseResult, List<String> editLore, int rowsSize) {
        super(MenuIdentifiers.MENU_WARP_CATEGORIES, parseResult, true);
        this.editLore = editLore;
        this.rowsSize = rowsSize;
    }

    public List<String> getEditLore() {
        return editLore;
    }

    public int getRowsSize() {
        return rowsSize;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        refreshViews(view -> view.island.equals(island));
    }

    public void closeViews(Island island) {
        closeViews(view -> view.getIsland().equals(island));
    }

    public void openMenu(SuperiorPlayer superiorPlayer, @Nullable MenuView<?, ?> previousMenu, Island island) {
        // The warp categories menu should be opened only if A) its enabled B) there are more than 1 categories
        if (plugin.getSettings().isWarpCategories() && island.getWarpCategories().size() > 1) {
            plugin.getMenus().openWarpCategories(superiorPlayer, MenuViewWrapper.fromView(previousMenu), island);
        } else {
            WarpCategory warpCategory = island.getWarpCategories().values().stream().findFirst()
                    .orElseGet(() -> island.createWarpCategory("Default Category"));
            Menus.MENU_WARPS.openMenu(superiorPlayer, previousMenu, warpCategory);
        }
    }

    @Nullable
    public static MenuWarpCategories createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("warp-categories.yml",
                MenuWarpCategories::convertOldGUI, new WarpCategoryPagedObjectButton.Builder());

        if (menuParseResult == null)
            return null;

        YamlConfiguration cfg = menuParseResult.getConfig();

        List<String> editLore = cfg.getStringList("edit-lore");
        int rowsSize = cfg.getStringList("pattern").size();

        return new MenuWarpCategories(menuParseResult, editLore, rowsSize);
    }

    public static class View extends AbstractPagedMenuView<View, IslandViewArgs, WarpCategory> {

        private final Island island;
        private final boolean hasManagePerms;

        protected View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                       Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
            this.hasManagePerms = island.hasPermission(inventoryViewer, IslandPrivileges.SET_WARP);
        }

        @Override
        protected List<WarpCategory> requestObjects() {
            DynamicArray<WarpCategory> warpCategories = new DynamicArray<>();
            island.getWarpCategories().values().forEach(warpCategory -> {
                warpCategory.getWarps()
                        .stream()
                        .filter(islandWarp -> island.isMember(getInventoryViewer()) || !islandWarp.hasPrivateFlag())
                        .findAny()
                        .ifPresent(unused -> warpCategories.set(warpCategory.getSlot(), warpCategory));
            });
            return warpCategories.toList();
        }

        public Island getIsland() {
            return island;
        }

        public boolean hasManagePerms() {
            return hasManagePerms;
        }

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        if (newMenu.contains("slots") || !newMenu.contains("items"))
            return false;

        String itemChar = newMenu.getConfigurationSection("items")
                .getKeys(false).stream()
                .findFirst()
                .orElse(null);

        if (itemChar == null)
            return false;

        newMenu.set("slots", itemChar);

        return true;
    }

}
