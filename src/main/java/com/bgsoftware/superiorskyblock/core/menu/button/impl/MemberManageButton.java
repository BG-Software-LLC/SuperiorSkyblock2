package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.PlayerMenuView;
import com.bgsoftware.superiorskyblock.island.IslandUtils;

import java.util.Objects;

public class MemberManageButton extends AbstractMenuViewButton<PlayerMenuView> {

    private MemberManageButton(AbstractMenuTemplateButton<PlayerMenuView> templateButton, PlayerMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(ButtonClickContext<PlayerMenuView> context) {
        getTemplate().manageAction.onButtonClick(menuView, context);
    }

    public enum ManageAction {

        SET_ROLE {
            @Override
            void onButtonClick(PlayerMenuView menuView, ButtonClickContext<PlayerMenuView> context) {
                menuView.setPreviousMove(false);
                plugin.getMenus().openMemberRole(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), menuView.getSuperiorPlayer());
            }
        },
        BAN_MEMBER {
            @Override
            void onButtonClick(PlayerMenuView menuView, ButtonClickContext<PlayerMenuView> context) {
                SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
                if (plugin.getSettings().isBanConfirm()) {
                    Island island = inventoryViewer.getIsland();
                    if (IslandUtils.checkBanRestrictions(inventoryViewer, island, menuView.getSuperiorPlayer())) {
                        menuView.setPreviousMove(false);
                        plugin.getMenus().openConfirmBan(inventoryViewer, MenuViewWrapper.fromView(menuView), island, menuView.getSuperiorPlayer());
                    }
                } else {
                    plugin.getCommands().dispatchSubCommand(context.getPlayer(), "ban", menuView.getSuperiorPlayer().getName());
                }
            }
        },
        KICK_MEMBER {
            @Override
            void onButtonClick(PlayerMenuView menuView, ButtonClickContext<PlayerMenuView> context) {
                SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
                if (plugin.getSettings().isKickConfirm()) {
                    Island island = inventoryViewer.getIsland();

                    if (island == null)
                        return;

                    if (IslandUtils.checkKickRestrictions(inventoryViewer, island, menuView.getSuperiorPlayer())) {
                        menuView.setPreviousMove(false);
                        plugin.getMenus().openConfirmKick(inventoryViewer, MenuViewWrapper.fromView(menuView), island, menuView.getSuperiorPlayer());
                    }
                } else {
                    plugin.getCommands().dispatchSubCommand(context.getPlayer(), "kick", menuView.getSuperiorPlayer().getName());
                }
            }
        };

        ManageAction() {

        }

        abstract void onButtonClick(PlayerMenuView menuView, ButtonClickContext<PlayerMenuView> context);

    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<PlayerMenuView> {

        private ManageAction manageAction;

        public Builder setManageAction(ManageAction manageAction) {
            this.manageAction = manageAction;
            return this;
        }

        @Override
        public MenuTemplateButton<PlayerMenuView> build() {
            return new Template(this, manageAction);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<PlayerMenuView> {

        private final ManageAction manageAction;

        Template(AbstractBuilder<PlayerMenuView> builder, ManageAction manageAction) {
            super(builder, MemberManageButton.class, MemberManageButton::new);
            this.manageAction = Objects.requireNonNull(manageAction, "manageAction cannot be null");
        }

    }

}
