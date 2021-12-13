package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpCategoriesPagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class MenuWarpCategories extends PagedSuperiorMenu<MenuWarpCategories, WarpCategory> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static PagedMenuPattern<MenuWarpCategories, WarpCategory> menuPattern;

    public static int rowsSize;
    public static List<String> editLore;

    private final Island island;
    private final boolean hasManagePerms;

    private MenuWarpCategories(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
        hasManagePerms = island != null && island.hasPermission(superiorPlayer, IslandPrivileges.SET_WARP);
    }

    public Island getTargetIsland() {
        return island;
    }

    public boolean hasManagePerms() {
        return hasManagePerms;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    @Override
    protected List<WarpCategory> requestObjects() {
        return new ArrayList<>(island.getWarpCategories().values());
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuWarpCategories, WarpCategory> patternBuilder = new PagedMenuPattern.Builder<>();

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "warp-categories.yml", MenuWarpCategories::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        editLore = cfg.getStringList("edit-lore");

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots),
                        new WarpCategoriesPagedObjectButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        MenuWarpCategories menuWarpCategories = new MenuWarpCategories(superiorPlayer, island);
        if (hasOnlyOneItem(island)) {
            plugin.getMenus().openWarps(superiorPlayer, previousMenu, getOnlyOneItem(island));
        } else {
            menuWarpCategories.open(previousMenu);
        }
    }

    public static void refreshMenus(Island island) {
        refreshMenus(MenuWarpCategories.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static void destroyMenus(Island island) {
        destroyMenus(MenuWarpCategories.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static boolean hasOnlyOneItem(Island island) {
        return island.getWarpCategories().size() <= 1;
    }

    private static WarpCategory getOnlyOneItem(Island island) {
        return island.getWarpCategories().values().stream().findFirst().orElseGet(() -> island.createWarpCategory("Default Category"));
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
