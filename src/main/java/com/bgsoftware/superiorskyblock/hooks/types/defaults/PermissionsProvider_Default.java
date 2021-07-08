package com.bgsoftware.superiorskyblock.hooks.types.defaults;

import com.bgsoftware.superiorskyblock.hooks.PermissionsProvider;
import org.bukkit.entity.Player;

public final class PermissionsProvider_Default implements PermissionsProvider {

    @Override
    public boolean hasPermission(Player player, String permission) {
        return player.getEffectivePermissions().stream().anyMatch(permissionAttachmentInfo ->
                permissionAttachmentInfo.getPermission().equalsIgnoreCase(permission));
    }

}