package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmTransfer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class TransferButton extends AbstractMenuViewButton<MenuConfirmTransfer.View> {

    private TransferButton(AbstractMenuTemplateButton<MenuConfirmTransfer.View> templateButton, MenuConfirmTransfer.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

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
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, newOwner);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuConfirmTransfer.View> {

        private final boolean newOwner;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, boolean newOwner) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    TransferButton.class, TransferButton::new);
            this.newOwner = newOwner;
        }

    }

}
