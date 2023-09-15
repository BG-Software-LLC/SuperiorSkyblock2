package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmBan;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class BanButton extends AbstractMenuViewButton<MenuConfirmBan.View> {

    private BanButton(AbstractMenuTemplateButton<MenuConfirmBan.View> templateButton, MenuConfirmBan.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (getTemplate().banPlayer)
            IslandUtils.handleBanPlayer(clickedPlayer, menuView.getIsland(), menuView.getTargetPlayer());

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuConfirmBan.View> {

        private boolean banPlayer;

        public Builder setBanPlayer(boolean banPlayer) {
            this.banPlayer = banPlayer;
            return this;
        }

        @Override
        public MenuTemplateButton<MenuConfirmBan.View> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, banPlayer);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuConfirmBan.View> {

        private final boolean banPlayer;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, boolean banPlayer) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    BanButton.class, BanButton::new);
            this.banPlayer = banPlayer;
        }

    }

}
