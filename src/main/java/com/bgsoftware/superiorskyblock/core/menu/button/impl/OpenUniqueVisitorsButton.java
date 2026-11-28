package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandVisitors;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;

public class OpenUniqueVisitorsButton extends AbstractMenuViewButton<MenuIslandVisitors.View> {

    private OpenUniqueVisitorsButton(AbstractMenuTemplateButton<MenuIslandVisitors.View> templateButton, MenuIslandVisitors.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuIslandVisitors.View> context) {
        menuView.setPreviousMove(false);
        plugin.getMenus().openUniqueVisitors(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), menuView.getIsland());
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuIslandVisitors.View> {

        @Override
        public MenuTemplateButton<MenuIslandVisitors.View> build() {
            return new MenuTemplateButtonImpl<>(this, OpenUniqueVisitorsButton.class,
                    OpenUniqueVisitorsButton::new);
        }

    }

}
