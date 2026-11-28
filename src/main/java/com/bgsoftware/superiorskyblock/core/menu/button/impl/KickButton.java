package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmKick;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;

public class KickButton extends AbstractMenuViewButton<MenuConfirmKick.View> {

    private KickButton(AbstractMenuTemplateButton<MenuConfirmKick.View> templateButton, MenuConfirmKick.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuConfirmKick.View> context) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(context.getPlayer());

        if (getTemplate().kickPlayer)
            IslandUtils.handleKickPlayer(clickedPlayer, menuView.getIsland(), menuView.getSuperiorPlayer());

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuConfirmKick.View> {

        private boolean kickPlayer;

        public Builder setKickPlayer(boolean kickPlayer) {
            this.kickPlayer = kickPlayer;
            return this;
        }

        @Override
        public MenuTemplateButton<MenuConfirmKick.View> build() {
            return new Template(this, kickPlayer);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuConfirmKick.View> {

        private final boolean kickPlayer;

        Template(AbstractBuilder<MenuConfirmKick.View> builder, boolean kickPlayer) {
            super(builder, KickButton.class, KickButton::new);
            this.kickPlayer = kickPlayer;
        }

    }

}
