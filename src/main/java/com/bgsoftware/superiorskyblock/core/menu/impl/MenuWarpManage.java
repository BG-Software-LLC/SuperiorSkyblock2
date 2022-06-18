package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpManageIconButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpManageLocationButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpManagePrivateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpManageRenameButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;

public class MenuWarpManage extends SuperiorMenu<MenuWarpManage> {

    private static RegularMenuPattern<MenuWarpManage> menuPattern;

    public static GameSound successUpdateSound;

    private final IslandWarp islandWarp;

    private MenuWarpManage(SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        super(menuPattern, superiorPlayer);
        this.islandWarp = islandWarp;
    }

    public IslandWarp getIslandWarp() {
        return islandWarp;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, islandWarp);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{0}", islandWarp.getName());
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuWarpManage> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "warp-manage.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        if (cfg.isConfigurationSection("success-update-sound"))
            successUpdateSound = MenuParser.getSound(cfg.getConfigurationSection("success-update-sound"));

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "warp-rename", menuPatternSlots),
                        new WarpManageRenameButton.Builder())
                .mapButtons(getSlots(cfg, "warp-icon", menuPatternSlots),
                        new WarpManageIconButton.Builder())
                .mapButtons(getSlots(cfg, "warp-location", menuPatternSlots),
                        new WarpManageLocationButton.Builder())
                .mapButtons(getSlots(cfg, "warp-private", menuPatternSlots),
                        new WarpManagePrivateButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, IslandWarp islandWarp) {
        new MenuWarpManage(superiorPlayer, islandWarp).open(previousMenu);
    }

    public static void refreshMenus(IslandWarp islandWarp) {
        refreshMenus(MenuWarpManage.class, superiorMenu -> superiorMenu.islandWarp.equals(islandWarp));
    }

}
