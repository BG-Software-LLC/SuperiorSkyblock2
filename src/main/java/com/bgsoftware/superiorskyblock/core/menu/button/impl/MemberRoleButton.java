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
import com.bgsoftware.superiorskyblock.core.menu.view.impl.PlayerMenuView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class MemberRoleButton extends AbstractMenuViewButton<PlayerMenuView> {

    private MemberRoleButton(AbstractMenuTemplateButton<PlayerMenuView> templateButton, PlayerMenuView menuView) {
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
        PlayerRole playerRole = plugin.getRoles().getPlayerRoleFromId(getTemplate().playerRoleId);

        if (playerRole.isLastRole()) {
            plugin.getCommands().dispatchSubCommand(inventoryViewer, "transfer",
                    targetPlayer.getName());
        } else {
            plugin.getCommands().dispatchSubCommand(inventoryViewer, "setrole",
                    targetPlayer.getName() + " " + playerRole);
        }
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<PlayerMenuView> {

        private int playerRoleId;

        public Builder setPlayerRole(PlayerRole playerRole) {
            this.playerRoleId = Objects.requireNonNull(playerRole, "playerRole cannot be null").getId();
            return this;
        }

        @Override
        public MenuTemplateButton<PlayerMenuView> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, playerRoleId);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<PlayerMenuView> {

        private final int playerRoleId;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, int playerRoleId) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    MemberRoleButton.class, MemberRoleButton::new);
            this.playerRoleId = playerRoleId;
        }

    }

}
