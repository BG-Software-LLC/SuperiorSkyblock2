package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.zmenu.utils.Permission;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.MetaUpdater;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IslandPermissionButton extends SuperiorButton implements PaginateButton {

    private final String noRolePermission;
    private final String exactRolePermission;
    private final String higherRolePermission;
    private final List<Permission> permissions;

    public IslandPermissionButton(SuperiorSkyblockPlugin plugin, String noRolePermission, String exactRolePermission, String higherRolePermission, List<Permission> permissions) {
        super(plugin);
        this.noRolePermission = noRolePermission;
        this.exactRolePermission = exactRolePermission;
        this.higherRolePermission = higherRolePermission;
        this.permissions = permissions;
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {

        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);
        Island island = superiorPlayer.getIsland();

        Pagination<Permission> pagination = new Pagination<>();
        List<Permission> permissions = pagination.paginate(this.permissions, this.slots.size(), inventory.getPage());

        MetaUpdater updater = plugin.getZMenumanager().getInventoryManager().getMeta();

        for (int i = 0; i != Math.min(permissions.size(), this.slots.size()); i++) {
            int slot = slots.get(i);
            Permission permission = permissions.get(i);

            PlayerRole requiredRole = permission.getIslandPrivilege() == null ? null : island.getRequiredPlayerRole(permission.getIslandPrivilege());

            Placeholders placeholders = new Placeholders();
            placeholders.register("role", requiredRole == null ? "" : requiredRole.getName());

            MenuItemStack menuItemStack = permission.getItemStackPermission();

            ItemStack itemStack = menuItemStack.build(player, false, placeholders);
            ItemMeta itemMeta = itemStack.getItemMeta();

            List<String> strings = new ArrayList<>(menuItemStack.getLore().stream().map(placeholders::parse).collect(Collectors.toList()));
            int roleWeight = requiredRole == null ? Integer.MAX_VALUE : requiredRole.getWeight();

            PlayerRole currentRole;
            for (int j = -2; (currentRole = SPlayerRole.of(j)) != null; j++) {
                if (!plugin.getSettings().isCoopMembers() && currentRole == SPlayerRole.coopRole())
                    continue;

                if (j < roleWeight) {
                    strings.add(this.noRolePermission.replace("%role%", String.valueOf(currentRole)));
                } else if (j == roleWeight) {
                    strings.add(this.exactRolePermission.replace("%role%", String.valueOf(currentRole)));
                } else {
                    strings.add(this.higherRolePermission.replace("%role%", String.valueOf(currentRole)));
                }
            }

            updater.updateLore(itemMeta, strings, player);
            itemStack.setItemMeta(itemMeta);

            inventory.addItem(slot, itemStack).setClick(event -> {

            });
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        return this.permissions.size();
    }

    @Override
    public boolean isPermanent() {
        return super.isPermanent();
    }
}
