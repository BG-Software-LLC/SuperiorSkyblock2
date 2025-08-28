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
import com.bgsoftware.superiorskyblock.core.menu.button.impl.WarpCategoryPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.IIslandMenuView;
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

    public static class View extends AbstractPagedMenuView<View, IslandViewArgs, WarpCategory> implements IIslandMenuView {

        private final Island island;
        private final boolean hasManagePerms;

        protected View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                       Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
            this.hasManagePerms = island.hasPermission(inventoryViewer, IslandPrivileges.SET_WARP);
        }

        @Override
        public Island getIsland() {
            return island;
        }

        @Override
        protected List<WarpCategory> requestObjects() {
            DynamicArray<WarpCategory> warpCategories = new DynamicArray<>();
            island.getWarpCategories().values().forEach(warpCategory -> warpCategory.getWarps()
                    .stream()
                    .filter(islandWarp -> island.isMember(getInventoryViewer()) || !islandWarp.hasPrivateFlag())
                    .findAny()
                    .ifPresent(unused -> warpCategories.set(warpCategory.getSlot(), warpCategory)));
            return warpCategories.toList();
        }

        public boolean hasManagePerms() {
            return hasManagePerms;
        }

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        if (newMenu.isString("slots") || !newMenu.isConfigurationSection("items"))
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
