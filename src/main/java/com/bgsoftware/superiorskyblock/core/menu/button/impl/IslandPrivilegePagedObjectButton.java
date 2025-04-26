package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandPrivileges;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IslandPrivilegePagedObjectButton extends AbstractPagedMenuButton<MenuIslandPrivileges.View, MenuIslandPrivileges.IslandPrivilegeInfo> {

    private final PrivilegeButtonActions actions;

    private IslandPrivilegePagedObjectButton(MenuTemplateButton<MenuIslandPrivileges.View> templateButton, MenuIslandPrivileges.View menuView) {
        super(templateButton, menuView);

        if (menuView.getPermissionHolder() instanceof PlayerRole) {
            this.actions = RolePrivilegeButtonActions.INSTANCE;
        } else {
            this.actions = PlayerPrivilegeButtonActions.INSTANCE;
        }
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        this.actions.onButtonClick(clickEvent, this);
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        return this.actions.modifyViewItem(buttonItem, this);
    }

    private static void onSuccessfulPermissionChange(IslandPrivilegePagedObjectButton button,
                                                     SuperiorPlayer clickedPlayer, String permissionHolderName) {
        Player player = clickedPlayer.asPlayer();

        if (player == null)
            return;

        Message.UPDATED_PERMISSION.send(clickedPlayer, permissionHolderName);

        GameSoundImpl.playSound(player, button.pagedObject.getAccessSound());

        button.pagedObject.getAccessCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));

        Menus.MENU_ISLAND_PRIVILEGES.refreshViews();
    }

    private static void onFailurePermissionChange(IslandPrivilegePagedObjectButton button,
                                                  SuperiorPlayer clickedPlayer, boolean sendFailMessage) {
        Player player = clickedPlayer.asPlayer();

        if (player == null)
            return;

        if (sendFailMessage)
            Message.LACK_CHANGE_PERMISSION.send(clickedPlayer);

        GameSoundImpl.playSound(player, button.pagedObject.getNoAccessSound());

        button.pagedObject.getNoAccessCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));
    }

    private static boolean isRoleAllowed(@Nullable PlayerRole playerRole, @Nullable IslandPrivilege.Type islandPrivilegeType) {
        if (!plugin.getSettings().isCoopMembers() && playerRole == SPlayerRole.coopRole())
            return false;

        if (islandPrivilegeType == IslandPrivilege.Type.COMMAND &&
                playerRole != null && playerRole.isLessThan(SPlayerRole.defaultRole())) {
            return false;
        }

        return true;
    }

    private interface PrivilegeButtonActions {

        void onButtonClick(InventoryClickEvent clickEvent, IslandPrivilegePagedObjectButton button);

        ItemStack modifyViewItem(ItemStack buttonItem, IslandPrivilegePagedObjectButton button);

    }

    private static class RolePrivilegeButtonActions implements PrivilegeButtonActions {

        private static final RolePrivilegeButtonActions INSTANCE = new RolePrivilegeButtonActions();

        @Override
        public void onButtonClick(InventoryClickEvent clickEvent, IslandPrivilegePagedObjectButton button) {
            IslandPrivilege islandPrivilege = button.pagedObject.getIslandPrivilege();

            if (islandPrivilege == null)
                return;

            Island island = button.menuView.getIsland();
            SuperiorPlayer clickedPlayer = button.menuView.getInventoryViewer();
            IslandPrivilege.Type islandPrivilegeType = islandPrivilege.getType();

            PlayerRole currentRole = island.getRequiredPlayerRole(islandPrivilege);

            if (clickedPlayer.getPlayerRole().isLessThan(currentRole)) {
                onFailurePermissionChange(button, clickedPlayer, false);
                return;
            }

            PlayerRole newRole = null;

            if (clickEvent.getClick().isLeftClick()) {
                newRole = currentRole;
                do {
                    newRole = SPlayerRole.of(newRole.getWeight() - 1);
                } while (newRole != null && !isRoleAllowed(newRole, islandPrivilegeType));

                if (newRole == null)
                    newRole = clickedPlayer.getPlayerRole();
            } else {
                if (clickedPlayer.getPlayerRole().isHigherThan(currentRole)) {
                    newRole = currentRole;
                    do {
                        newRole = SPlayerRole.of(newRole.getWeight() + 1);
                    } while (newRole != null && !isRoleAllowed(newRole, islandPrivilegeType));
                }

                if (newRole == null) {
                    newRole = islandPrivilegeType == IslandPrivilege.Type.COMMAND ? SPlayerRole.defaultRole() : SPlayerRole.guestRole();
                }
            }

            if (PluginEventsFactory.callIslandChangeRolePrivilegeEvent(island, clickedPlayer, newRole)) {
                island.setPermission(newRole, islandPrivilege);
                onSuccessfulPermissionChange(button, clickedPlayer,
                        Formatters.CAPITALIZED_FORMATTER.format(islandPrivilege.getName()));
            }
        }

        @Override
        public ItemStack modifyViewItem(ItemStack buttonItem, IslandPrivilegePagedObjectButton button) {
            ItemBuilder permissionItem = button.pagedObject.getRoleIslandPrivilegeItem();
            if (permissionItem == null)
                return new ItemStack(Material.AIR);

            IslandPrivilege islandPrivilege = button.pagedObject.getIslandPrivilege();
            Island island = button.menuView.getIsland();

            PlayerRole requiredRole = islandPrivilege == null ? null : island.getRequiredPlayerRole(islandPrivilege);
            IslandPrivilege.Type islandPrivilegeType = islandPrivilege == null ? null : islandPrivilege.getType();

            permissionItem.replaceAll("{}", requiredRole == null ? "" : requiredRole.toString());

            if (!Menus.MENU_ISLAND_PRIVILEGES.getNoRolePermission().isEmpty() &&
                    !Menus.MENU_ISLAND_PRIVILEGES.getExactRolePermission().isEmpty() &&
                    !Menus.MENU_ISLAND_PRIVILEGES.getHigherRolePermission().isEmpty()) {
                List<String> roleString = new ArrayList<>();

                int roleWeight = requiredRole == null ? Integer.MAX_VALUE : requiredRole.getWeight();

                PlayerRole currentRole;
                for (int i = -2; (currentRole = SPlayerRole.of(i)) != null; i++) {
                    if (!isRoleAllowed(currentRole, islandPrivilegeType))
                        continue;

                    if (i < roleWeight) {
                        roleString.add(Menus.MENU_ISLAND_PRIVILEGES.getNoRolePermission().replace("{}", currentRole + ""));
                    } else if (i == roleWeight) {
                        roleString.add(Menus.MENU_ISLAND_PRIVILEGES.getExactRolePermission().replace("{}", currentRole + ""));
                    } else {
                        roleString.add(Menus.MENU_ISLAND_PRIVILEGES.getHigherRolePermission().replace("{}", currentRole + ""));
                    }
                }

                ItemMeta itemMeta = permissionItem.getItemMeta();

                if (itemMeta != null) {
                    List<String> lore = itemMeta.getLore();

                    for (int i = 0; i < lore.size(); i++) {
                        String line = lore.get(i);
                        if (line.equals("{0}")) {
                            lore.set(i, roleString.get(0));
                            for (int j = 1; j < roleString.size(); j++) {
                                lore.add(i + j, roleString.get(j));
                            }
                            i += roleString.size();
                        }
                    }

                    permissionItem.withLore(lore);
                }
            }

            return permissionItem.build(button.menuView.getInventoryViewer());
        }

    }

    private static class PlayerPrivilegeButtonActions implements PrivilegeButtonActions {

        private static final PlayerPrivilegeButtonActions INSTANCE = new PlayerPrivilegeButtonActions();

        @Override
        public void onButtonClick(InventoryClickEvent clickEvent, IslandPrivilegePagedObjectButton button) {
            IslandPrivilege islandPrivilege = button.pagedObject.getIslandPrivilege();

            if (islandPrivilege == null)
                return;

            Island island = button.menuView.getIsland();
            SuperiorPlayer clickedPlayer = button.menuView.getInventoryViewer();

            if (!island.hasPermission(clickedPlayer, islandPrivilege))
                return;

            SuperiorPlayer permissiblePlayer = (SuperiorPlayer) button.menuView.getPermissionHolder();

            PermissionNode permissionNode = island.getPermissionNode(permissiblePlayer);

            String permissionHolderName = permissiblePlayer.getName();

            boolean currentValue = permissionNode.hasPermission(islandPrivilege);

            if (!PluginEventsFactory.callIslandChangePlayerPrivilegeEvent(island, clickedPlayer, permissiblePlayer, !currentValue))
                return;

            island.setPermission(permissiblePlayer, islandPrivilege, !currentValue);

            onSuccessfulPermissionChange(button, clickedPlayer, permissionHolderName);
        }

        @Override
        public ItemStack modifyViewItem(ItemStack buttonItem, IslandPrivilegePagedObjectButton button) {
            IslandPrivilege islandPrivilege = button.pagedObject.getIslandPrivilege();
            Island targetIsland = button.menuView.getIsland();
            SuperiorPlayer permissiblePlayer = (SuperiorPlayer) button.menuView.getPermissionHolder();

            boolean hasPermission = islandPrivilege != null &&
                    targetIsland.getPermissionNode(permissiblePlayer).hasPermission(islandPrivilege);

            ItemBuilder permissionItem = hasPermission ? button.pagedObject.getEnabledIslandPrivilegeItem() :
                    button.pagedObject.getDisabledIslandPrivilegeItem();

            return permissionItem.build(button.menuView.getInventoryViewer());
        }

    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandPrivileges.View, MenuIslandPrivileges.IslandPrivilegeInfo> {

        @Override
        public PagedMenuTemplateButton<MenuIslandPrivileges.View, MenuIslandPrivileges.IslandPrivilegeInfo> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), IslandPrivilegePagedObjectButton.class,
                    IslandPrivilegePagedObjectButton::new);
        }

    }

}
