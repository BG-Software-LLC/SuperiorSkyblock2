package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.KickButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuConfirmKick extends AbstractMenu<MenuConfirmKick.View, MenuConfirmKick.Args> {

    private MenuConfirmKick(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_CONFIRM_KICK, parseResult);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Nullable
    public static MenuConfirmKick createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("confirm-kick.yml",
                null);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<View> patternBuilder = menuParseResult.getLayoutBuilder();

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "confirm", menuPatternSlots),
                new KickButton.Builder().setKickPlayer(true));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "cancel", menuPatternSlots),
                new KickButton.Builder());

        return new MenuConfirmKick(menuParseResult);
    }

    public static class Args extends IslandViewArgs {

        private final SuperiorPlayer targetPlayer;

        public Args(Island island, SuperiorPlayer targetPlayer) {
            super(island);
            this.targetPlayer = targetPlayer;
        }

    }

    public static class View extends AbstractMenuView<View, Args> {

        private final Island island;
        private SuperiorPlayer targetPlayer;

        protected View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                       Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
            this.targetPlayer = args.targetPlayer;
        }

        public Island getIsland() {
            return island;
        }

        public void setTargetPlayer(SuperiorPlayer targetPlayer) {
            this.targetPlayer = targetPlayer;
        }

        public SuperiorPlayer getTargetPlayer() {
            return targetPlayer;
        }

    }

}
