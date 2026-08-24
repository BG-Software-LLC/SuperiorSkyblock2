package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractIconProviderMenu;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public class WarpIconEditConfirmButton extends AbstractMenuViewButton<AbstractIconProviderMenu.View<IslandWarp>> {

    private WarpIconEditConfirmButton(AbstractMenuTemplateButton<AbstractIconProviderMenu.View<IslandWarp>> templateButton,
                                      AbstractIconProviderMenu.View<IslandWarp> menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<AbstractIconProviderMenu.View<IslandWarp>> context) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        IslandWarp islandWarp = menuView.getIconProvider();

        PluginEvent<PluginEventArgs.IslandChangeWarpIcon> event = PluginEventsFactory.callIslandChangeWarpIconEvent(
                islandWarp.getIsland(), inventoryViewer, islandWarp, menuView.getIconTemplate().build());

        if (event.isCancelled())
            return;

        context.getPlayer().closeInventory();

        Message.WARP_ICON_UPDATED.send(inventoryViewer);

        islandWarp.setIcon(event.getArgs().icon);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<AbstractIconProviderMenu.View<IslandWarp>> {

        @Override
        public MenuTemplateButton<AbstractIconProviderMenu.View<IslandWarp>> build() {
            return new MenuTemplateButtonImpl<>(this, WarpIconEditConfirmButton.class,
                    WarpIconEditConfirmButton::new);
        }

    }

}
