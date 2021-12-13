package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpCategoryIconDisplayButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpCategoryIconEditConfirmButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpCategoryIconEditLoreButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpCategoryIconEditTypeButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpCategoryIconRenameButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;

public final class MenuWarpCategoryIconEdit extends SuperiorMenu<MenuWarpCategoryIconEdit> {

    private static RegularMenuPattern<MenuWarpCategoryIconEdit> menuPattern;

    private final WarpCategory warpCategory;
    private final ItemBuilder itemBuilder;

    private MenuWarpCategoryIconEdit(SuperiorPlayer superiorPlayer, WarpCategory warpCategory) {
        super(menuPattern, superiorPlayer);
        this.warpCategory = warpCategory;
        this.itemBuilder = warpCategory == null ? null : new ItemBuilder(warpCategory.getRawIcon());
    }

    public WarpCategory getWarpCategory() {
        return warpCategory;
    }

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
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

        RegularMenuPattern.Builder<MenuWarpCategoryIconEdit> patternBuilder = new RegularMenuPattern.Builder<>();

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "warp-category-icon-edit.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "icon-type", menuPatternSlots),
                        new WarpCategoryIconEditTypeButton.Builder())
                .mapButtons(getSlots(cfg, "icon-rename", menuPatternSlots),
                        new WarpCategoryIconRenameButton.Builder())
                .mapButtons(getSlots(cfg, "icon-relore", menuPatternSlots),
                        new WarpCategoryIconEditLoreButton.Builder())
                .mapButtons(getSlots(cfg, "icon-confirm", menuPatternSlots),
                        new WarpCategoryIconEditConfirmButton.Builder())
                .mapButtons(getSlots(cfg, "icon-slots", menuPatternSlots),
                        new WarpCategoryIconDisplayButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, WarpCategory warpCategory) {
        new MenuWarpCategoryIconEdit(superiorPlayer, warpCategory).open(previousMenu);
    }

}
