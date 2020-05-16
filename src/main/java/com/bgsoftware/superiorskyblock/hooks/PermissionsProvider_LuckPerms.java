package com.bgsoftware.superiorskyblock.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PermissionsProvider_LuckPerms implements PermissionsProvider {

    private final LuckPerms luckPerms;

    public PermissionsProvider_LuckPerms(){
        luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();
    }

    @Override
    public boolean hasPermission(Player player, String permission) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && user.getCachedData().getPermissionData().getPermissionMap().getOrDefault(permission, false);
    }

}
