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
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BannedPlayersPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;

import java.util.List;

public class MenuIslandBannedPlayers extends AbstractPagedMenu<MenuIslandBannedPlayers.View, IslandViewArgs, SuperiorPlayer> {

    private MenuIslandBannedPlayers(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_ISLAND_BANNED_PLAYERS, parseResult, false);
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
    public static MenuIslandBannedPlayers createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("banned-players.yml",
                null, new BannedPlayersPagedObjectButton.Builder());
        return menuParseResult == null ? null : new MenuIslandBannedPlayers(menuParseResult);
    }

    public static class View extends AbstractPagedMenuView<MenuIslandBannedPlayers.View, IslandViewArgs, SuperiorPlayer> {

        private final Island island;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
        }

        @Override
        protected List<SuperiorPlayer> requestObjects() {
            return island.getBannedPlayers();
        }

    }

}
