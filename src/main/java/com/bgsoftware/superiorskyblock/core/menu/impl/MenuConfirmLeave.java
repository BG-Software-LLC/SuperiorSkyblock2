package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.LeaveButton;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuConfirmLeave extends AbstractMenu<BaseMenuView, EmptyViewArgs> {

    private MenuConfirmLeave(MenuParseResult<BaseMenuView> parseResult) {
        super(MenuIdentifiers.MENU_CONFIRM_LEAVE, parseResult);
    }

    @Override
    protected BaseMenuView createViewInternal(SuperiorPlayer superiorPlayer, EmptyViewArgs unused,
                                              @Nullable MenuView<?, ?> previousMenuView) {
        return new BaseMenuView(superiorPlayer, previousMenuView, this);
    }

    @Nullable
    public static MenuConfirmLeave createInstance() {
        MenuParseResult<BaseMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("confirm-leave.yml",
                null);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<BaseMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "confirm", menuPatternSlots),
                new LeaveButton.Builder().setLeaveIsland(true));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "cancel", menuPatternSlots),
                new LeaveButton.Builder());

        return new MenuConfirmLeave(menuParseResult);
    }

}
