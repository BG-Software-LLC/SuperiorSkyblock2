package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;

public class WarpCategoryManageWarpsButton extends AbstractMenuViewButton<MenuWarpCategoryManage.View> {

    private WarpCategoryManageWarpsButton(AbstractMenuTemplateButton<MenuWarpCategoryManage.View> templateButton, MenuWarpCategoryManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuWarpCategoryManage.View> context) {
        menuView.setPreviousMove(false);
        plugin.getMenus().openWarps(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), menuView.getWarpCategory());
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuWarpCategoryManage.View> {

        @Override
        public MenuTemplateButton<MenuWarpCategoryManage.View> build() {
            return new MenuTemplateButtonImpl<>(this, WarpCategoryManageWarpsButton.class,
                    WarpCategoryManageWarpsButton::new);
        }

    }

}
