package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuMemberManage;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class MemberManageButton extends AbstractMenuViewButton<MenuMemberManage.View> {

    private MemberManageButton(AbstractMenuTemplateButton<MenuMemberManage.View> templateButton, MenuMemberManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        getTemplate().manageAction.onButtonClick(menuView, clickEvent);
    }

    public enum ManageAction {

        SET_ROLE {
            @Override
            void onButtonClick(MenuMemberManage.View menuView, InventoryClickEvent clickEvent) {
                menuView.setPreviousMove(false);
                plugin.getMenus().openMemberRole(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), menuView.getSuperiorPlayer());
            }
        },
        BAN_MEMBER {
            @Override
            void onButtonClick(MenuMemberManage.View menuView, InventoryClickEvent clickEvent) {
                SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
                if (plugin.getSettings().isBanConfirm()) {
                    Island island = inventoryViewer.getIsland();
                    if (IslandUtils.checkBanRestrictions(inventoryViewer, island, menuView.getSuperiorPlayer())) {
                        menuView.setPreviousMove(false);
                        plugin.getMenus().openConfirmBan(inventoryViewer, MenuViewWrapper.fromView(menuView), island, menuView.getSuperiorPlayer());
                    }
                } else {
                    plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "ban", menuView.getSuperiorPlayer().getName());
                }
            }
        },
        KICK_MEMBER {
            @Override
            void onButtonClick(MenuMemberManage.View menuView, InventoryClickEvent clickEvent) {
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
                    plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "kick", menuView.getSuperiorPlayer().getName());
                }
            }
        };

        ManageAction() {

        }

        abstract void onButtonClick(MenuMemberManage.View menuView, InventoryClickEvent clickEvent);

    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuMemberManage.View> {

        private ManageAction manageAction;

        public Builder setManageAction(ManageAction manageAction) {
            this.manageAction = manageAction;
            return this;
        }

        @Override
        public MenuTemplateButton<MenuMemberManage.View> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, manageAction);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuMemberManage.View> {

        private final ManageAction manageAction;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, ManageAction manageAction) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    MemberManageButton.class, MemberManageButton::new);
            this.manageAction = Objects.requireNonNull(manageAction, "manageAction cannot be null");
        }

    }

}
