package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpManageIconButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpManageLocationButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpManagePrivateButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpManageRenameButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;

public final class MenuWarpManage extends SuperiorMenu<MenuWarpManage> {

    private static RegularMenuPattern<MenuWarpManage> menuPattern;

    public static SoundWrapper successUpdateSound;

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

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "warp-manage.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        if (cfg.isConfigurationSection("success-update-sound"))
            successUpdateSound = FileUtils.getSound(cfg.getConfigurationSection("success-update-sound"));

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
