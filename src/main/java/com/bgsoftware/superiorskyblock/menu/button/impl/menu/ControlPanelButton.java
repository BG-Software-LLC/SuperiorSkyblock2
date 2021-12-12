package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuControlPanel;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class ControlPanelButton extends SuperiorMenuButton {

    private final ControlPanelAction controlPanelAction;

    private ControlPanelButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound,
                               ControlPanelAction controlPanelAction) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.controlPanelAction = controlPanelAction;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        Preconditions.checkArgument(superiorMenu instanceof MenuControlPanel, "superiorMenu must be MenuControlPanel");

        MenuControlPanel menuControlPanel = (MenuControlPanel) superiorMenu;

        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island targetIsland = menuControlPanel.getTargetIsland();

        switch (controlPanelAction) {
            case OPEN_MEMBERS:
                plugin.getMenus().openMembers(clickedPlayer, superiorMenu, targetIsland);
                break;
            case OPEN_SETTINGS:
                if (clickedPlayer.hasPermission("superior.island.settings")) {
                    if (!clickedPlayer.hasPermission(IslandPrivileges.SET_SETTINGS)) {
                        Locale.NO_SET_SETTINGS_PERMISSION.send(clickedPlayer,
                                targetIsland.getRequiredPlayerRole(IslandPrivileges.SET_SETTINGS));
                        return;
                    }

                    plugin.getMenus().openSettings(clickedPlayer, superiorMenu, targetIsland);
                }
                break;
            case OPEN_VISITORS:
                plugin.getMenus().openVisitors(clickedPlayer, superiorMenu, targetIsland);
                break;
        }

        Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, ControlPanelButton> {

        private ControlPanelAction controlPanelAction;

        public Builder setAction(ControlPanelAction controlPanelAction) {
            this.controlPanelAction = controlPanelAction;
            return this;
        }

        @Override
        public ControlPanelButton build() {
            return new ControlPanelButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, controlPanelAction);
        }

    }

    public enum ControlPanelAction {

        OPEN_MEMBERS,
        OPEN_SETTINGS,
        OPEN_VISITORS

    }

}
