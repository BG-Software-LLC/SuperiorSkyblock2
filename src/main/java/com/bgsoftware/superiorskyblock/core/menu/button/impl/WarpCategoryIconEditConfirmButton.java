package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
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

public class WarpCategoryIconEditConfirmButton extends AbstractMenuViewButton<AbstractIconProviderMenu.View<WarpCategory>> {

    private WarpCategoryIconEditConfirmButton(AbstractMenuTemplateButton<AbstractIconProviderMenu.View<WarpCategory>> templateButton,
                                              AbstractIconProviderMenu.View<WarpCategory> menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<AbstractIconProviderMenu.View<WarpCategory>> context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getPlayer());

        WarpCategory warpCategory = menuView.getIconProvider();

        PluginEvent<PluginEventArgs.IslandChangeWarpCategoryIcon> event = PluginEventsFactory.callIslandChangeWarpCategoryIconEvent(
                warpCategory.getIsland(), superiorPlayer, warpCategory, menuView.getIconTemplate().build());

        if (event.isCancelled())
            return;

        context.getPlayer().closeInventory();

        Message.WARP_CATEGORY_ICON_UPDATED.send(superiorPlayer);

        warpCategory.setIcon(event.getArgs().icon);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<AbstractIconProviderMenu.View<WarpCategory>> {

        @Override
        public MenuTemplateButton<AbstractIconProviderMenu.View<WarpCategory>> build() {
            return new MenuTemplateButtonImpl<>(this, WarpCategoryIconEditConfirmButton.class,
                    WarpCategoryIconEditConfirmButton::new);
        }

    }

}
