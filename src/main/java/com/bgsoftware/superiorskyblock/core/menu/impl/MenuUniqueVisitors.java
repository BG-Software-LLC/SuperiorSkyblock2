package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.UniqueVisitorPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;

import java.util.List;
import java.util.function.Function;

public class MenuUniqueVisitors extends PagedSuperiorMenu<MenuUniqueVisitors, MenuUniqueVisitors.UniqueVisitorInfo> {

    private static final Function<Pair<SuperiorPlayer, Long>, UniqueVisitorInfo> VISITOR_INFO_MAPPER =
            visitor -> new UniqueVisitorInfo(visitor.getKey(), visitor.getValue());

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
        return new SequentialListBuilder<UniqueVisitorInfo>()
                .build(island.getUniqueVisitorsWithTimes(), VISITOR_INFO_MAPPER);
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuUniqueVisitors, UniqueVisitorInfo> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "unique-visitors.yml", null);

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

    public static class UniqueVisitorInfo {

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
