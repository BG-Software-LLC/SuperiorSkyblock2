package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public class WarpManagePrivateButton extends AbstractMenuViewButton<MenuWarpManage.View> {

    private WarpManagePrivateButton(AbstractMenuTemplateButton<MenuWarpManage.View> templateButton, MenuWarpManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuWarpManage.View> context) {
        IslandWarp islandWarp = menuView.getIslandWarp();

        boolean openToPublic = islandWarp.hasPrivateFlag();

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getPlayer());

        if (openToPublic ? !PluginEventsFactory.callIslandOpenWarpEvent(islandWarp.getIsland(), superiorPlayer, islandWarp) :
                !PluginEventsFactory.callIslandCloseWarpEvent(islandWarp.getIsland(), superiorPlayer, islandWarp))
            return;

        islandWarp.setPrivateFlag(!openToPublic);

        if (openToPublic)
            Message.WARP_PUBLIC_UPDATE.send(superiorPlayer);
        else
            Message.WARP_PRIVATE_UPDATE.send(superiorPlayer);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuWarpManage.View> {

        @Override
        public MenuTemplateButton<MenuWarpManage.View> build() {
            return new MenuTemplateButtonImpl<>(this, WarpManagePrivateButton.class,
                    WarpManagePrivateButton::new);
        }

    }

}
