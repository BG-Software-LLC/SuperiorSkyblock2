package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class PermissionsProvider_Vault implements PermissionsProvider {

    private static Permission permission = null;

    public static boolean isCompatible() {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
            permission = permissionProvider.getProvider();

        if(permission != null){
            SuperiorSkyblockPlugin.log("Using Vault as a permissions provider.");
        }

        return permission != null;
    }

    @Override
    public boolean hasPermission(Player player, String permission) {
        for(String group : PermissionsProvider_Vault.permission.getPlayerGroups(player)) {
            if (PermissionsProvider_Vault.permission.groupHas(player.getWorld(), group, permission))
                return true;
        }

        return false;
    }

}
