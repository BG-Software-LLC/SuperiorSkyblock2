package com.bgsoftware.superiorskyblock.external.slimefun;

import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class ProtectionModule_RC13 {

    private ProtectionModule_RC13() {
    }

    public static void register(Plugin plugin, PermissionCheck permissionCheck) {
        new ProtectionModuleImpl(plugin, permissionCheck).register();
    }

    private static class ProtectionModuleImpl implements ProtectionModule {

        private final Plugin plugin;
        private final PermissionCheck permissionCheck;

        ProtectionModuleImpl(Plugin plugin, PermissionCheck permissionCheck) {
            this.plugin = plugin;
            this.permissionCheck = permissionCheck;
        }

        @Override
        public void load() {

        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public boolean hasPermission(OfflinePlayer offlinePlayer, Location location, ProtectableAction protectableAction) {
            return permissionCheck.checkPermission(offlinePlayer, location, protectableAction.name());
        }

        void register() {
            SlimefunPlugin.getProtectionManager().registerModule(Bukkit.getServer(), plugin.getName(), pl -> this);
        }
    }

    public interface PermissionCheck {

        boolean checkPermission(OfflinePlayer offlinePlayer, Location location, String permission);

    }

}
