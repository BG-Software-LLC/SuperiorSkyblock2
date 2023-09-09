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
import com.bgsoftware.superiorskyblock.core.menu.view.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class ControlPanelButton extends AbstractMenuViewButton<IslandMenuView> {

    private ControlPanelButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island targetIsland = menuView.getIsland();

        switch (getTemplate().controlPanelAction) {
            case OPEN_MEMBERS:
                plugin.getMenus().openMembers(inventoryViewer, MenuViewWrapper.fromView(menuView), targetIsland);
                break;
            case OPEN_SETTINGS:
                if (inventoryViewer.hasPermission("superior.island.settings")) {
                    if (!inventoryViewer.hasPermission(IslandPrivileges.SET_SETTINGS)) {
                        Message.NO_SET_SETTINGS_PERMISSION.send(inventoryViewer,
                                targetIsland.getRequiredPlayerRole(IslandPrivileges.SET_SETTINGS));
                        return;
                    }

                    plugin.getMenus().openSettings(inventoryViewer, MenuViewWrapper.fromView(menuView), targetIsland);
                }
                break;
            case OPEN_VISITORS:
                plugin.getMenus().openVisitors(inventoryViewer, MenuViewWrapper.fromView(menuView), targetIsland);
                break;
        }

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public enum ControlPanelAction {

        OPEN_MEMBERS,
        OPEN_SETTINGS,
        OPEN_VISITORS

    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        private ControlPanelAction controlPanelAction;

        public Builder setAction(ControlPanelAction controlPanelAction) {
            this.controlPanelAction = controlPanelAction;
            return this;
        }

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, controlPanelAction);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        private final ControlPanelAction controlPanelAction;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound,
                 ControlPanelAction controlPanelAction) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    ControlPanelButton.class, ControlPanelButton::new);
            this.controlPanelAction = Objects.requireNonNull(controlPanelAction, "controlPanelAction cannot be null");
        }

    }

}
