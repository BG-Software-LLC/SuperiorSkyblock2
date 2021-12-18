package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuIconEdit;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.IconDisplayButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.IconEditLoreButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.IconEditTypeButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.IconRenameButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpIconEditConfirmButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;

public final class MenuWarpIconEdit extends SuperiorMenuIconEdit<MenuWarpIconEdit, IslandWarp> {

    private static RegularMenuPattern<MenuWarpIconEdit> menuPattern;

    private MenuWarpIconEdit(SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        super(menuPattern, superiorPlayer, islandWarp, islandWarp == null ? null : islandWarp.getRawIcon() == null ?
                SIslandWarp.DEFAULT_WARP_ICON.clone() : new ItemBuilder(islandWarp.getRawIcon()));
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

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "warp-icon-edit.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "icon-type", menuPatternSlots),
                        new IconEditTypeButton.Builder<>(Locale.WARP_ICON_NEW_TYPE))
                .mapButtons(getSlots(cfg, "icon-rename", menuPatternSlots),
                        new IconRenameButton.Builder<>(Locale.WARP_ICON_NEW_NAME))
                .mapButtons(getSlots(cfg, "icon-relore", menuPatternSlots),
                        new IconEditLoreButton.Builder<>(Locale.WARP_ICON_NEW_LORE))
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
