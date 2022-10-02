package com.bgsoftware.superiorskyblock.external.permissions;

import com.bgsoftware.superiorskyblock.api.hooks.PermissionsProvider;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

@SuppressWarnings("unused")
public class PermissionsProvider_LuckPerms implements PermissionsProvider {

    private static LuckPerms luckPerms = null;

    public static boolean isCompatible() {
        try {
            CachedDataManager.class.getMethod("getPermissionData");
            luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();
            Log.info("Using LuckPerms as a permissions provider.");
            return true;
        } catch (Throwable ex) {
            Log.warn("You are using an outdated version of LuckPerms. It's recommended to update for a more optimized experience (v5.1+).");
            return false;
        }
    }

    @Override
    public boolean hasPermission(Player player, String permission) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && user.getCachedData().getPermissionData().getPermissionMap()
                .getOrDefault(permission.toLowerCase(Locale.ENGLISH), false);
    }

}
