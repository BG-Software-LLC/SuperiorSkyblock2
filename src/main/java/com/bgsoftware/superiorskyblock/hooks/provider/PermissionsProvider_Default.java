package com.bgsoftware.superiorskyblock.hooks.provider;

import org.bukkit.entity.Player;

public final class PermissionsProvider_Default implements PermissionsProvider {

    @Override
    public boolean hasPermission(Player player, String permission) {
        return player.getEffectivePermissions().stream().anyMatch(permissionAttachmentInfo ->
                permissionAttachmentInfo.getPermission().equalsIgnoreCase(permission));
    }

}
