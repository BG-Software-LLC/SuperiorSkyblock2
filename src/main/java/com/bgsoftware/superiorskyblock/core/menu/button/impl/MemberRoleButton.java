package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.PlayerMenuView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

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
        PlayerRole playerRole = getTemplate().playerRole;

        if (playerRole.isLastRole()) {
            plugin.getCommands().dispatchSubCommand(inventoryViewer, "transfer",
                    targetPlayer.getName());
        } else {
            plugin.getCommands().dispatchSubCommand(inventoryViewer, "setrole",
                    targetPlayer.getName() + " " + playerRole);
        }
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<PlayerMenuView> {

        private PlayerRole playerRole;

        public Builder setPlayerRole(PlayerRole playerRole) {
            this.playerRole = playerRole;
            return this;
        }

        @Override
        public MenuTemplateButton<PlayerMenuView> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, playerRole);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<PlayerMenuView> {

        private final PlayerRole playerRole;

        Template(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                 String requiredPermission, GameSound lackPermissionSound, PlayerRole playerRole) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    MemberRoleButton.class, MemberRoleButton::new);
            this.playerRole = playerRole;
        }

    }

}
