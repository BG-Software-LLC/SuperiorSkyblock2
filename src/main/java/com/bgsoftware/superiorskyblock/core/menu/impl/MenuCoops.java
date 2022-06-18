package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.CoopsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;

import java.util.List;

public class MenuCoops extends PagedSuperiorMenu<MenuCoops, SuperiorPlayer> {

    private static PagedMenuPattern<MenuCoops, SuperiorPlayer> menuPattern;

    private final Island island;

    private MenuCoops(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{0}", String.valueOf(island.getCoopPlayers().size()))
                .replace("{1}", String.valueOf(island.getCoopLimit()));
    }

    @Override
    protected List<SuperiorPlayer> requestObjects() {
        return island.getCoopPlayers();
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuCoops, SuperiorPlayer> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "coops.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots), new CoopsPagedObjectButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuCoops(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        SuperiorMenu.refreshMenus(MenuCoops.class, superiorMenu -> superiorMenu.island.equals(island));
    }

}
