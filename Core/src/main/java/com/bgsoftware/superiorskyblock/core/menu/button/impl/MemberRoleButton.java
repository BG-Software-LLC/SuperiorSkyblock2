package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.PlayerMenuView;
import org.bukkit.entity.Player;

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
    public void onButtonClick(ButtonClickContext<PlayerMenuView> context) {
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
            return new Template(this, playerRoleId);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<PlayerMenuView> {

        private final int playerRoleId;

        Template(AbstractBuilder<PlayerMenuView> builder, int playerRoleId) {
            super(builder, MemberRoleButton.class, MemberRoleButton::new);
            this.playerRoleId = playerRoleId;
        }

    }

}
