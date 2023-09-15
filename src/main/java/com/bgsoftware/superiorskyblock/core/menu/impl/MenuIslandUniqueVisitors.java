package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.UniqueVisitorPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;

import java.util.List;
import java.util.function.Function;

public class MenuIslandUniqueVisitors extends AbstractPagedMenu<MenuIslandUniqueVisitors.View, IslandViewArgs, MenuIslandUniqueVisitors.UniqueVisitorInfo> {

    private static final Function<Pair<SuperiorPlayer, Long>, UniqueVisitorInfo> VISITOR_INFO_MAPPER =
            visitor -> new UniqueVisitorInfo(visitor.getKey(), visitor.getValue());

    private MenuIslandUniqueVisitors(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_ISLAND_UNIQUE_VISITORS, parseResult, false);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        refreshViews(view -> view.island.equals(island));
    }

    @Nullable
    public static MenuIslandUniqueVisitors createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("unique-visitors.yml",
                null, new UniqueVisitorPagedObjectButton.Builder());
        return menuParseResult == null ? null : new MenuIslandUniqueVisitors(menuParseResult);
    }

    public static class View extends AbstractPagedMenuView<View, IslandViewArgs, UniqueVisitorInfo> {

        private final Island island;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{0}", island.getUniqueVisitorsWithTimes().size() + "");
        }

        @Override
        protected List<UniqueVisitorInfo> requestObjects() {
            return new SequentialListBuilder<UniqueVisitorInfo>()
                    .build(island.getUniqueVisitorsWithTimes(), VISITOR_INFO_MAPPER);
        }

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
