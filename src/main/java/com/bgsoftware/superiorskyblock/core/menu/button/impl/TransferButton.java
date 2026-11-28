package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmTransfer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;

public class TransferButton extends AbstractMenuViewButton<MenuConfirmTransfer.View> {

    private TransferButton(AbstractMenuTemplateButton<MenuConfirmTransfer.View> templateButton, MenuConfirmTransfer.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuConfirmTransfer.View> context) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(context.getPlayer());

        if (getTemplate().newOwner)
            IslandUtils.handleTransferIsland(clickedPlayer, menuView.getIsland(), menuView.getSuperiorPlayer());

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuConfirmTransfer.View> {

        private boolean newOwner;

        public Builder setNewOwner(boolean newOwner) {
            this.newOwner = newOwner;
            return this;
        }

        @Override
        public MenuTemplateButton<MenuConfirmTransfer.View> build() {
            return new Template(this, newOwner);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuConfirmTransfer.View> {

        private final boolean newOwner;

        Template(AbstractBuilder<MenuConfirmTransfer.View> builder, boolean newOwner) {
            super(builder, TransferButton.class, TransferButton::new);
            this.newOwner = newOwner;
        }

    }

}
