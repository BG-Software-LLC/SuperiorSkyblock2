package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.LeaveButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;

/**
 * @author Callum Jay Seabrook (BomBardyGamer)
 */
public class MenuConfirmLeave extends SuperiorMenu<MenuConfirmLeave> {

    private static RegularMenuPattern<MenuConfirmLeave> menuPattern;

    private MenuConfirmLeave(SuperiorPlayer superiorPlayer) {
        super(menuPattern, superiorPlayer);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu);
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuConfirmLeave> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "confirm-leave.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "confirm", menuPatternSlots), new LeaveButton.Builder().setLeaveIsland(true))
                .mapButtons(getSlots(cfg, "cancel", menuPatternSlots), new LeaveButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new MenuConfirmLeave(superiorPlayer).open(previousMenu);
    }

}
