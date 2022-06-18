package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.BanButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;

public class MenuConfirmBan extends SuperiorMenu<MenuConfirmBan> {

    private static RegularMenuPattern<MenuConfirmBan> menuPattern;

    private final Island targetIsland;

    private MenuConfirmBan(SuperiorPlayer superiorPlayer, Island targetIsland, SuperiorPlayer targetPlayer) {
        super(menuPattern, superiorPlayer);
        this.targetIsland = targetIsland;
        updateTargetPlayer(targetPlayer);
    }

    public Island getTargetIsland() {
        return targetIsland;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, targetIsland, targetPlayer);
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuConfirmBan> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "confirm-ban.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "confirm", menuPatternSlots), new BanButton.Builder().setBanPlayer(true))
                .mapButtons(getSlots(cfg, "cancel", menuPatternSlots), new BanButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer targetPlayer) {
        new MenuConfirmBan(superiorPlayer, targetIsland, targetPlayer).open(previousMenu);
    }

}
