package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpCategoryButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class MenuWarpCategories extends SuperiorMenu<MenuWarpCategories> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static RegularMenuPattern<MenuWarpCategories> menuPattern;

    public static int rowsSize;
    public static List<String> editLore;

    private final Island island;
    private final boolean hasManagePerms;

    private MenuWarpCategories(SuperiorPlayer superiorPlayer, Island island) {
        super(buildPattern(island, superiorPlayer), superiorPlayer);
        this.island = island;
        hasManagePerms = island.hasPermission(superiorPlayer, IslandPrivileges.SET_WARP);
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

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuWarpCategories> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "warp-categories.yml",
                MenuWarpCategories::convertOldGUI);

        if (menuLoadResult == null)
            return;

        CommentedConfiguration cfg = menuLoadResult.getConfig();

        editLore = cfg.getStringList("edit-lore");
        rowsSize = cfg.getStringList("pattern").size();

        menuPattern = patternBuilder.build();
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

    private static RegularMenuPattern<MenuWarpCategories> buildPattern(Island island, SuperiorPlayer inventoryViewer) {
        if (menuPattern == null)
            return null;

        RegularMenuPattern.Builder<MenuWarpCategories> patternBuilder = menuPattern.builder();
        for (WarpCategory warpCategory : island.getWarpCategories().values()) {
            long accessAmount = warpCategory.getWarps().stream().filter(
                    islandWarp -> island.isMember(inventoryViewer) || !islandWarp.hasPrivateFlag()
            ).count();
            if (accessAmount > 0) {
                patternBuilder.mapButton(warpCategory.getSlot(), new WarpCategoryButton.Builder(warpCategory));
            }
        }
        return patternBuilder.build();
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
