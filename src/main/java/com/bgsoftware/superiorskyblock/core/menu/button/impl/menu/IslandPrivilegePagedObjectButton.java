package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandPrivileges;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IslandPrivilegePagedObjectButton extends PagedObjectButton<MenuIslandPrivileges,
        MenuIslandPrivileges.IslandPrivilegeInfo> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandPrivilegePagedObjectButton(String requiredPermission, GameSound lackPermissionSound,
                                             int objectIndex) {
        super(null, null, null, requiredPermission, lackPermissionSound, null,
                objectIndex);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandPrivileges superiorMenu,
                              InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = superiorMenu.getTargetIsland();
        Object permissionHolder = superiorMenu.getPermissionHolder();

        if (permissionHolder instanceof PlayerRole) {
            onRoleButtonClick(island, clickedPlayer, superiorMenu, clickEvent);
        } else if (permissionHolder instanceof SuperiorPlayer) {
            onPlayerButtonClick(island, clickedPlayer, superiorMenu, (SuperiorPlayer) permissionHolder);
        }
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuIslandPrivileges superiorMenu,
                                      MenuIslandPrivileges.IslandPrivilegeInfo islandPrivilegeInfo) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        Island targetIsland = superiorMenu.getTargetIsland();
        Object permissionHolder = superiorMenu.getPermissionHolder();

        ItemBuilder permissionItem = new ItemBuilder(Material.AIR);

        if (permissionHolder instanceof PlayerRole) {
            if (islandPrivilegeInfo.getRoleIslandPrivilegeItem() != null) {
                permissionItem = modifyRoleButtonItem(targetIsland, islandPrivilegeInfo);
            }
        } else if (permissionHolder instanceof SuperiorPlayer) {
            boolean hasPermission = targetIsland.getPermissionNode((SuperiorPlayer) permissionHolder)
                    .hasPermission(islandPrivilegeInfo.getIslandPrivilege());
            permissionItem = hasPermission ? islandPrivilegeInfo.getEnabledIslandPrivilegeItem() :
                    islandPrivilegeInfo.getDisabledIslandPrivilegeItem();
        }

        return permissionItem.build(inventoryViewer);
    }

    private void onRoleButtonClick(Island island, SuperiorPlayer clickedPlayer, MenuIslandPrivileges superiorMenu,
                                   InventoryClickEvent clickEvent) {
        PlayerRole currentRole = island.getRequiredPlayerRole(pagedObject.getIslandPrivilege());

        if (clickedPlayer.getPlayerRole().isLessThan(currentRole)) {
            onFailurePermissionChange(clickedPlayer, false);
            return;
        }

        PlayerRole newRole = null;

        if (clickEvent.getClick().isLeftClick()) {
            newRole = SPlayerRole.of(currentRole.getWeight() - 1);

            if (newRole == null)
                newRole = clickedPlayer.getPlayerRole();
        } else {
            if (clickedPlayer.getPlayerRole().isHigherThan(currentRole)) {
                newRole = SPlayerRole.of(currentRole.getWeight() + 1);
            }

            if (newRole == null)
                newRole = SPlayerRole.guestRole();
        }

        if (plugin.getEventsBus().callIslandChangeRolePrivilegeEvent(island, clickedPlayer, newRole)) {
            island.setPermission(newRole, pagedObject.getIslandPrivilege());
            onSuccessfulPermissionChange(island, clickedPlayer, superiorMenu,
                    Formatters.CAPITALIZED_FORMATTER.format(pagedObject.getIslandPrivilege().getName()));
        }
    }

    private void onPlayerButtonClick(Island island, SuperiorPlayer clickedPlayer, MenuIslandPrivileges superiorMenu,
                                     SuperiorPlayer permissionHolder) {
        if (island.hasPermission(clickedPlayer, pagedObject.getIslandPrivilege())) {
            PermissionNode permissionNode = island.getPermissionNode(permissionHolder);

            String permissionHolderName = permissionHolder.getName();

            boolean currentValue = permissionNode.hasPermission(pagedObject.getIslandPrivilege());

            if (!plugin.getEventsBus().callIslandChangePlayerPrivilegeEvent(island, clickedPlayer, permissionHolder, !currentValue))
                return;

            island.setPermission(permissionHolder, pagedObject.getIslandPrivilege(), !currentValue);

            onSuccessfulPermissionChange(island, clickedPlayer, superiorMenu, permissionHolderName);
        }
    }

    private void onSuccessfulPermissionChange(Island island, SuperiorPlayer clickedPlayer,
                                              MenuIslandPrivileges superiorMenu,
                                              String permissionHolderName) {
        Player player = clickedPlayer.asPlayer();

        if (player == null)
            return;

        Message.UPDATED_PERMISSION.send(clickedPlayer, permissionHolderName);

        GameSound accessSound = pagedObject.getAccessSound();
        if (accessSound != null)
            accessSound.playSound(player);

        pagedObject.getAccessCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));

        MenuIslandPrivileges.refreshMenus(island);
    }

    private void onFailurePermissionChange(SuperiorPlayer clickedPlayer,
                                           boolean sendFailMessage) {
        Player player = clickedPlayer.asPlayer();

        if (player == null)
            return;

        if (sendFailMessage)
            Message.LACK_CHANGE_PERMISSION.send(clickedPlayer);

        GameSound noAccessSound = pagedObject.getNoAccessSound();
        if (noAccessSound != null)
            noAccessSound.playSound(player);

        pagedObject.getNoAccessCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));
    }

    private ItemBuilder modifyRoleButtonItem(Island island, MenuIslandPrivileges.IslandPrivilegeInfo islandPrivilegeInfo) {
        Preconditions.checkNotNull(islandPrivilegeInfo.getRoleIslandPrivilegeItem(), "role item cannot be null.");
        PlayerRole requiredRole = island.getRequiredPlayerRole(islandPrivilegeInfo.getIslandPrivilege());
        ItemBuilder permissionItem = islandPrivilegeInfo.getRoleIslandPrivilegeItem()
                .replaceAll("{}", requiredRole.toString());

        if (!MenuIslandPrivileges.noRolePermission.isEmpty() && !MenuIslandPrivileges.exactRolePermission.isEmpty() &&
                !MenuIslandPrivileges.higherRolePermission.isEmpty()) {
            List<String> roleString = new ArrayList<>();

            int roleWeight = requiredRole.getWeight();
            PlayerRole currentRole;

            for (int i = -2; (currentRole = SPlayerRole.of(i)) != null; i++) {
                if (i < roleWeight) {
                    roleString.add(MenuIslandPrivileges.noRolePermission.replace("{}", currentRole + ""));
                } else if (i == roleWeight) {
                    roleString.add(MenuIslandPrivileges.exactRolePermission.replace("{}", currentRole + ""));
                } else {
                    roleString.add(MenuIslandPrivileges.higherRolePermission.replace("{}", currentRole + ""));
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

    public static class Builder extends PagedObjectBuilder<Builder, IslandPrivilegePagedObjectButton, MenuIslandPrivileges> {

        @Override
        public IslandPrivilegePagedObjectButton build() {
            return new IslandPrivilegePagedObjectButton(requiredPermission, lackPermissionSound, getObjectIndex());
        }

    }

}
