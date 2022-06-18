package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.IconDisplayButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.IconEditLoreButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.IconEditTypeButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.IconRenameButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.WarpIconEditConfirmButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.warp.SIslandWarp;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenuIconEdit;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;

public class MenuWarpIconEdit extends SuperiorMenuIconEdit<MenuWarpIconEdit, IslandWarp> {

    private static RegularMenuPattern<MenuWarpIconEdit> menuPattern;

    private MenuWarpIconEdit(SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        super(menuPattern, superiorPlayer, islandWarp, islandWarp == null ? null : islandWarp.getRawIcon() == null ?
                SIslandWarp.DEFAULT_WARP_ICON : new TemplateItem(new ItemBuilder(islandWarp.getRawIcon())));
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, iconProvider);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{0}", iconProvider.getName());
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuWarpIconEdit> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "warp-icon-edit.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "icon-type", menuPatternSlots),
                        new IconEditTypeButton.Builder<>(Message.WARP_ICON_NEW_TYPE))
                .mapButtons(getSlots(cfg, "icon-rename", menuPatternSlots),
                        new IconRenameButton.Builder<>(Message.WARP_ICON_NEW_NAME))
                .mapButtons(getSlots(cfg, "icon-relore", menuPatternSlots),
                        new IconEditLoreButton.Builder<>(Message.WARP_ICON_NEW_LORE))
                .mapButtons(getSlots(cfg, "icon-confirm", menuPatternSlots),
                        new WarpIconEditConfirmButton.Builder())
                .mapButtons(getSlots(cfg, "icon-slots", menuPatternSlots),
                        new IconDisplayButton.Builder<>())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, IslandWarp islandWarp) {
        new MenuWarpIconEdit(superiorPlayer, islandWarp).open(previousMenu);
    }

}
