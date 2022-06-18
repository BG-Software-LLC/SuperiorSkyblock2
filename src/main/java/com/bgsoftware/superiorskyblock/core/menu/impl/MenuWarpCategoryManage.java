package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpCategoryManageIconButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpCategoryManageRenameButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpCategoryManageWarpsButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;

public class MenuWarpCategoryManage extends SuperiorMenu<MenuWarpCategoryManage> {

    private static RegularMenuPattern<MenuWarpCategoryManage> menuPattern;

    public static GameSound successUpdateSound;

    private final WarpCategory warpCategory;

    private MenuWarpCategoryManage(SuperiorPlayer superiorPlayer, WarpCategory warpCategory) {
        super(menuPattern, superiorPlayer);
        this.warpCategory = warpCategory;
    }

    public WarpCategory getWarpCategory() {
        return warpCategory;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, warpCategory);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{0}", warpCategory.getName());
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuWarpCategoryManage> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "warp-category-manage.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        if (cfg.isConfigurationSection("success-update-sound"))
            successUpdateSound = MenuParser.getSound(cfg.getConfigurationSection("success-update-sound"));

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "category-rename", menuPatternSlots),
                        new WarpCategoryManageRenameButton.Builder())
                .mapButtons(getSlots(cfg, "category-icon", menuPatternSlots),
                        new WarpCategoryManageIconButton.Builder())
                .mapButtons(getSlots(cfg, "category-warps", menuPatternSlots),
                        new WarpCategoryManageWarpsButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, WarpCategory warpCategory) {
        new MenuWarpCategoryManage(superiorPlayer, warpCategory).open(previousMenu);
    }

    public static void refreshMenus(WarpCategory warpCategory) {
        refreshMenus(MenuWarpCategoryManage.class, superiorMenu -> superiorMenu.warpCategory.equals(warpCategory));
    }

}
