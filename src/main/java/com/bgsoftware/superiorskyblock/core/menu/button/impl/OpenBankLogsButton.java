package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;

public class OpenBankLogsButton extends AbstractMenuViewButton<IslandMenuView> {

    private OpenBankLogsButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<IslandMenuView> context) {
        menuView.setPreviousMove(false);
        plugin.getMenus().openBankLogs(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), menuView.getIsland());
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            return new MenuTemplateButtonImpl<>(this, OpenBankLogsButton.class, OpenBankLogsButton::new);
        }

    }

}
