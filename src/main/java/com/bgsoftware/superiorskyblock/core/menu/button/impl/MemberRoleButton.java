package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuMemberRole;
import com.bgsoftware.superiorskyblock.core.menu.view.PlayerMenuView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class MemberRoleButton extends AbstractMenuViewButton<MenuMemberRole.View> {

    private MemberRoleButton(AbstractMenuTemplateButton<MenuMemberRole.View> templateButton, MenuMemberRole.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        Player inventoryViewer = menuView.getInventoryViewer().asPlayer();
        SuperiorPlayer targetPlayer = menuView.getSuperiorPlayer();
        PlayerRole playerRole = getTemplate().playerRole;

        if (playerRole.isLastRole()) {
            plugin.getCommands().dispatchSubCommand(inventoryViewer, "transfer",
                    targetPlayer.getName());
        } else {
            plugin.getCommands().dispatchSubCommand(inventoryViewer, "setrole",
                    targetPlayer.getName() + " " + playerRole);
        }
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuMemberRole.View> {

        private PlayerRole playerRole;

        public Builder setPlayerRole(PlayerRole playerRole) {
            this.playerRole = playerRole;
            return this;
        }

        @Override
        public MenuTemplateButton<MenuMemberRole.View> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, playerRole);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuMemberRole.View> {

        private final PlayerRole playerRole;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, PlayerRole playerRole) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    MemberRoleButton.class, MemberRoleButton::new);
            this.playerRole = Objects.requireNonNull(playerRole, "playerRole cannot be null");
        }

    }

}
