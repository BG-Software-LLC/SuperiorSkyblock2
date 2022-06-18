package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.KickButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;

public class MenuConfirmKick extends SuperiorMenu<MenuConfirmKick> {

    private static RegularMenuPattern<MenuConfirmKick> menuPattern;

    private final Island targetIsland;

    private MenuConfirmKick(SuperiorPlayer superiorPlayer, Island targetIsland, SuperiorPlayer targetPlayer) {
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

        RegularMenuPattern.Builder<MenuConfirmKick> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "confirm-kick.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "confirm", menuPatternSlots), new KickButton.Builder().setKickPlayer(true))
                .mapButtons(getSlots(cfg, "cancel", menuPatternSlots), new KickButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer targetPlayer) {
        new MenuConfirmKick(superiorPlayer, targetIsland, targetPlayer).open(previousMenu);
    }

}
