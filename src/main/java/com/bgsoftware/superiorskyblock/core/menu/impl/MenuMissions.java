package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.OpenMissionCategoryButton;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;

public class MenuMissions extends AbstractMenu<BaseMenuView, EmptyViewArgs> {

    private MenuMissions(MenuParseResult<BaseMenuView> parseResult) {
        super(MenuIdentifiers.MENU_MISSIONS, parseResult);
    }

    @Override
    protected BaseMenuView createViewInternal(SuperiorPlayer superiorPlayer, EmptyViewArgs unused,
                                              @Nullable MenuView<?, ?> previousMenuView) {
        return new BaseMenuView(superiorPlayer, previousMenuView, this);
    }

    @Nullable
    public static MenuMissions createInstance() {
        MenuParseResult<BaseMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("missions.yml",
                null);

        if (menuParseResult == null) {
            return null;
        }

        MenuLayout.Builder<BaseMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        plugin.getMissions().getMissionCategories().forEach(missionCategory -> {
            patternBuilder.mapButton(missionCategory.getSlot(),
                    new OpenMissionCategoryButton.Builder().setMissionsCategory(missionCategory));
        });

        return new MenuMissions(menuParseResult);
    }

}
