package com.bgsoftware.superiorskyblock.external.slimefun;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionManager;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;

public class ProtectionModule_Dev999 {

    private static final ReflectMethod<Void> OLD_REGISTER_MODULE = new ReflectMethod<>(ProtectionManager.class,
            "registerModule", Server.class, String.class, Function.class);

    private ProtectionModule_Dev999() {
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
            // Do nothing.
        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public boolean hasPermission(OfflinePlayer offlinePlayer, Location location, Interaction interaction) {
            return this.permissionCheck.checkPermission(offlinePlayer, location, interaction.name());
        }

        void register() {
            BukkitExecutor.sync(() -> {
                if (OLD_REGISTER_MODULE.isValid()) {
                    OLD_REGISTER_MODULE.invoke(Slimefun.getProtectionManager(), Bukkit.getServer(), plugin.getName(),
                            (Function<Plugin, ProtectionModule>) pl -> this);
                } else {
                    Slimefun.getProtectionManager().registerModule(Bukkit.getServer().getPluginManager(),
                            plugin.getName(), pl -> this);
                }
            }, 2L);
        }

    }

    public interface PermissionCheck {

        boolean checkPermission(OfflinePlayer offlinePlayer, Location location, String permission);

    }

}
