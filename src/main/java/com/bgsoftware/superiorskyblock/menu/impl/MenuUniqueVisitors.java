package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.UniqueVisitorPagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;

import java.util.List;
import java.util.stream.Collectors;

public final class MenuUniqueVisitors extends PagedSuperiorMenu<MenuUniqueVisitors, MenuUniqueVisitors.UniqueVisitorInfo> {

    private static PagedMenuPattern<MenuUniqueVisitors, UniqueVisitorInfo> menuPattern;

    private final Island island;

    private MenuUniqueVisitors(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
    }

    public Island getTargetIsland() {
        return island;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{0}", island.getUniqueVisitorsWithTimes().size() + "");
    }

    @Override
    protected List<UniqueVisitorInfo> requestObjects() {
        return island.getUniqueVisitorsWithTimes().stream()
                .map(pair -> new UniqueVisitorInfo(pair.getKey(), pair.getValue()))
                .collect(Collectors.toList());
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuUniqueVisitors, UniqueVisitorInfo> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = FileUtils.loadMenu(patternBuilder, "unique-visitors.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots),
                        new UniqueVisitorPagedObjectButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuUniqueVisitors(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        refreshMenus(MenuUniqueVisitors.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static final class UniqueVisitorInfo {

        private final SuperiorPlayer visitor;
        private final long visitTime;

        public UniqueVisitorInfo(SuperiorPlayer visitor, long visitTime) {
            this.visitor = visitor;
            this.visitTime = visitTime;
        }

        public SuperiorPlayer getVisitor() {
            return visitor;
        }

        public long getVisitTime() {
            return visitTime;
        }

    }

}
