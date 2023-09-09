package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CoopsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;

import java.util.List;

public class MenuCoops extends AbstractPagedMenu<MenuCoops.View, IslandViewArgs, SuperiorPlayer> {

    private MenuCoops(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_COOPS, parseResult, false);
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
    public static MenuCoops createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("coops.yml",
                null, new CoopsPagedObjectButton.Builder());
        return menuParseResult == null ? null : new MenuCoops(menuParseResult);
    }

    public static class View extends AbstractPagedMenuView<MenuCoops.View, IslandViewArgs, SuperiorPlayer> {

        private final Island island;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<MenuCoops.View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{0}", String.valueOf(this.island.getCoopPlayers().size()))
                    .replace("{1}", String.valueOf(this.island.getCoopLimit()));
        }

        @Override
        protected List<SuperiorPlayer> requestObjects() {
            return island.getCoopPlayers();
        }

    }

}
