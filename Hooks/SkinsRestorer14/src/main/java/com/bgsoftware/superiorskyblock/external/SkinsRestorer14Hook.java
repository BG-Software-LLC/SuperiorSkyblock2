package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.mojang.authlib.properties.Property;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.bukkit.events.SkinApplyBukkitEvent;
import net.skinsrestorer.api.property.IProperty;
import net.skinsrestorer.shared.storage.Config;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;

public class SkinsRestorer14Hook {

    private static final ReflectMethod<Object> SKINS_RESTORER_GET_SKIN = new ReflectMethod<>(SkinsRestorerAPI.class, "getSkinData", String.class);

    private static SuperiorSkyblockPlugin plugin;

    public static boolean isCompatible() {
        if (Config.MYSQL_ENABLED)
            return true;

        ReflectField<Object> skinsFolderMethod = new ReflectField<>(net.skinsrestorer.shared.storage.SkinStorage.class,
                Object.class, "skinsFolder");

        if (!skinsFolderMethod.isValid())
            return false;

        net.skinsrestorer.bukkit.SkinsRestorer skinsRestorer = JavaPlugin.getPlugin(net.skinsrestorer.bukkit.SkinsRestorer.class);
        Object skinsFolder = skinsFolderMethod.get(skinsRestorer.getSkinStorage());

        if (skinsFolder instanceof File) {
            return ((File) skinsFolder).exists();
        } else if (skinsFolder instanceof Path) {
            return ((Path) skinsFolder).toFile().exists();
        }

        return false;
    }

    public static void register(SuperiorSkyblockPlugin plugin) {
        SkinsRestorer14Hook.plugin = plugin;

        plugin.getProviders().registerSkinsListener(SkinsRestorer14Hook::setSkinTexture);
        plugin.getServer().getPluginManager().registerEvents(new SkinsListener(), plugin);
    }

    private static void setSkinTexture(SuperiorPlayer superiorPlayer) {
        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> setSkinTexture(superiorPlayer));
            return;
        }

        Property property = getSkin(superiorPlayer);
        if (property != null)
            BukkitExecutor.sync(() -> plugin.getNMSPlayers().setSkinTexture(superiorPlayer, property));
    }

    private static Property getSkin(SuperiorPlayer superiorPlayer) {
        IProperty property;
        try {
            property = SkinsRestorerAPI.getApi().getSkinData(superiorPlayer.getName());
        } catch (Throwable ex) {
            property = (IProperty) SKINS_RESTORER_GET_SKIN.invoke(SkinsRestorerAPI.getApi(), superiorPlayer.getName());
        }

        if (property == null)
            return null;

        if (property instanceof Property)
            return ((Property) property);

        Object propertyHandle = property.getHandle();

        return propertyHandle instanceof Property ? ((Property) propertyHandle) : null;
    }

    private static class SkinsListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSkinApply(SkinApplyBukkitEvent event) {
            if (event.getProperty() instanceof Property) {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getWho());
                plugin.getNMSPlayers().setSkinTexture(superiorPlayer, (Property) event.getProperty());
            }
        }

    }

}
