package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmKick;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class KickButton extends AbstractMenuViewButton<MenuConfirmKick.View> {

    private KickButton(AbstractMenuTemplateButton<MenuConfirmKick.View> templateButton, MenuConfirmKick.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (getTemplate().kickPlayer)
            IslandUtils.handleKickPlayer(clickedPlayer, menuView.getIsland(), menuView.getTargetPlayer());

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
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, kickPlayer);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuConfirmKick.View> {

        private final boolean kickPlayer;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, boolean kickPlayer) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    KickButton.class, KickButton::new);
            this.kickPlayer = kickPlayer;
        }

    }

}
