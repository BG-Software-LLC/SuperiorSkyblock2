package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.zmenu.utils.Permission;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;

import java.util.List;

public class IslandPermissionButton extends SuperiorButton implements PaginateButton {

    private final List<Permission> permissions;

    public IslandPermissionButton(SuperiorSkyblockPlugin plugin, List<Permission> permissions) {
        super(plugin);
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

    }

    @Override
    public int getPaginationSize(Player player) {
        return this.permissions.size();
    }
}
