package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandPrivileges;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IslandPrivilegePagedObjectButton extends AbstractPagedMenuButton<MenuIslandPrivileges.View, MenuIslandPrivileges.IslandPrivilegeInfo> {

    private IslandPrivilegePagedObjectButton(MenuTemplateButton<MenuIslandPrivileges.View> templateButton, MenuIslandPrivileges.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        Island island = menuView.getIsland();
        Object permissionHolder = menuView.getPermissionHolder();

        if (permissionHolder instanceof PlayerRole) {
            onRoleButtonClick(island, menuView.getInventoryViewer(), clickEvent);
        } else if (permissionHolder instanceof SuperiorPlayer) {
            onPlayerButtonClick(island, menuView.getInventoryViewer(), (SuperiorPlayer) permissionHolder);
        }
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island targetIsland = menuView.getIsland();
        Object permissionHolder = menuView.getPermissionHolder();

        ItemBuilder permissionItem = new ItemBuilder(Material.AIR);

        if (permissionHolder instanceof PlayerRole) {
            if (pagedObject.getRoleIslandPrivilegeItem() != null) {
                permissionItem = modifyRoleButtonItem(targetIsland);
            }
        } else if (permissionHolder instanceof SuperiorPlayer) {
            IslandPrivilege islandPrivilege = pagedObject.getIslandPrivilege();
            boolean hasPermission = islandPrivilege != null && targetIsland.getPermissionNode(
                    (SuperiorPlayer) permissionHolder).hasPermission(islandPrivilege);
            permissionItem = hasPermission ? pagedObject.getEnabledIslandPrivilegeItem() :
                    pagedObject.getDisabledIslandPrivilegeItem();
        }

        return permissionItem.build(inventoryViewer);
    }

    private void onRoleButtonClick(Island island, SuperiorPlayer clickedPlayer, InventoryClickEvent clickEvent) {
        IslandPrivilege islandPrivilege = pagedObject.getIslandPrivilege();

        if (islandPrivilege == null)
            return;

        PlayerRole currentRole = island.getRequiredPlayerRole(islandPrivilege);

        if (clickedPlayer.getPlayerRole().isLessThan(currentRole)) {
            onFailurePermissionChange(clickedPlayer, false);
            return;
        }

        PlayerRole newRole = null;

        if (clickEvent.getClick().isLeftClick()) {
            newRole = SPlayerRole.of(currentRole.getWeight() - 1);

            if (!plugin.getSettings().isCoopMembers() && newRole == SPlayerRole.coopRole()) {
                assert newRole != null;
                newRole = SPlayerRole.of(newRole.getWeight() - 1);
            }

            if (newRole == null)
                newRole = clickedPlayer.getPlayerRole();
        } else {
            if (clickedPlayer.getPlayerRole().isHigherThan(currentRole)) {
                newRole = SPlayerRole.of(currentRole.getWeight() + 1);
            }

            if (!plugin.getSettings().isCoopMembers() && newRole == SPlayerRole.coopRole()) {
                assert newRole != null;
                newRole = SPlayerRole.of(newRole.getWeight() + 1);
            }

            if (newRole == null)
                newRole = SPlayerRole.guestRole();
        }

        if (plugin.getEventsBus().callIslandChangeRolePrivilegeEvent(island, clickedPlayer, newRole)) {
            island.setPermission(newRole, islandPrivilege);
            onSuccessfulPermissionChange(clickedPlayer, Formatters.CAPITALIZED_FORMATTER.format(islandPrivilege.getName()));
        }
    }

    private void onPlayerButtonClick(Island island, SuperiorPlayer clickedPlayer, SuperiorPlayer permissionHolder) {
        IslandPrivilege islandPrivilege = pagedObject.getIslandPrivilege();

        if (islandPrivilege == null || !island.hasPermission(clickedPlayer, islandPrivilege))
            return;

        PermissionNode permissionNode = island.getPermissionNode(permissionHolder);

        String permissionHolderName = permissionHolder.getName();

        boolean currentValue = permissionNode.hasPermission(islandPrivilege);

        if (!plugin.getEventsBus().callIslandChangePlayerPrivilegeEvent(island, clickedPlayer, permissionHolder, !currentValue))
            return;

        island.setPermission(permissionHolder, islandPrivilege, !currentValue);

        onSuccessfulPermissionChange(clickedPlayer, permissionHolderName);
    }

    private void onSuccessfulPermissionChange(SuperiorPlayer clickedPlayer, String permissionHolderName) {
        Player player = clickedPlayer.asPlayer();

        if (player == null)
            return;

        Message.UPDATED_PERMISSION.send(clickedPlayer, permissionHolderName);

        GameSoundImpl.playSound(player, pagedObject.getAccessSound());

        pagedObject.getAccessCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));

        Menus.MENU_ISLAND_PRIVILEGES.refreshViews();
    }

    private void onFailurePermissionChange(SuperiorPlayer clickedPlayer, boolean sendFailMessage) {
        Player player = clickedPlayer.asPlayer();

        if (player == null)
            return;

        if (sendFailMessage)
            Message.LACK_CHANGE_PERMISSION.send(clickedPlayer);

        GameSoundImpl.playSound(player, pagedObject.getNoAccessSound());

        pagedObject.getNoAccessCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));
    }

    private ItemBuilder modifyRoleButtonItem(Island island) {
        Preconditions.checkNotNull(pagedObject.getRoleIslandPrivilegeItem(), "role item cannot be null.");

        IslandPrivilege islandPrivilege = pagedObject.getIslandPrivilege();

        PlayerRole requiredRole = islandPrivilege == null ? null : island.getRequiredPlayerRole(islandPrivilege);
        ItemBuilder permissionItem = pagedObject.getRoleIslandPrivilegeItem()
                .replaceAll("{}", requiredRole == null ? "" : requiredRole.toString());

        if (!Menus.MENU_ISLAND_PRIVILEGES.getNoRolePermission().isEmpty() &&
                !Menus.MENU_ISLAND_PRIVILEGES.getExactRolePermission().isEmpty() &&
                !Menus.MENU_ISLAND_PRIVILEGES.getHigherRolePermission().isEmpty()) {
            List<String> roleString = new ArrayList<>();

            int roleWeight = requiredRole == null ? Integer.MAX_VALUE : requiredRole.getWeight();
            PlayerRole currentRole;

            for (int i = -2; (currentRole = SPlayerRole.of(i)) != null; i++) {
                if (!plugin.getSettings().isCoopMembers() && currentRole == SPlayerRole.coopRole())
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

        return permissionItem;
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
