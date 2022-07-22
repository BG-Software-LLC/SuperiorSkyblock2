package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
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
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.shared.exception.SkinRequestException;
import skinsrestorer.shared.storage.SkinStorage;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class SkinsRestorerHook {

    private static SuperiorSkyblockPlugin plugin;
    private static ISkinsRestorer skinsRestorer = null;

    public static void register(SuperiorSkyblockPlugin plugin) {
        SkinsRestorerHook.plugin = plugin;
        try {
            Class.forName("net.skinsrestorer.bukkit.SkinsRestorer");
            skinsRestorer = new SkinsRestorerNew();
        } catch (Exception ex) {
            skinsRestorer = new SkinsRestorerOld();
        }
        if (skinsRestorer.isLocalMode()) {
            plugin.getProviders().registerSkinsListener(SkinsRestorerHook::setSkinTexture);
            plugin.getServer().getPluginManager().registerEvents(new SkinsListener(), plugin);
        }
    }

    private static void setSkinTexture(SuperiorPlayer superiorPlayer) {
        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> setSkinTexture(superiorPlayer));
            return;
        }

        if (skinsRestorer.isLocalMode()) {
            Property property = skinsRestorer.getSkin(superiorPlayer);
            if (property != null)
                BukkitExecutor.sync(() -> plugin.getNMSPlayers().setSkinTexture(superiorPlayer, property));
        }
    }

    interface ISkinsRestorer {

        boolean isLocalMode();

        @Nullable
        Property getSkin(SuperiorPlayer superiorPlayer);

    }

    private static class SkinsRestorerOld implements ISkinsRestorer {

        @Override
        public boolean isLocalMode() {
            return true;
        }

        @Override
        public Property getSkin(SuperiorPlayer superiorPlayer) {
            try {
                SkinStorage skinStorage = SkinsRestorer.getInstance().getSkinStorage();
                return (Property) skinStorage.getOrCreateSkinForPlayer(superiorPlayer.getName(), true);
            } catch (SkinRequestException | NullPointerException ex) {
                PluginDebugger.debug(ex);
                return null;
            }
        }

    }

    private static class SkinsRestorerNew implements ISkinsRestorer {

        private static final ReflectMethod<Object> SKINS_RESTORER_GET_SKIN = new ReflectMethod<>(SkinsRestorerAPI.class, "getSkinData", String.class);

        private final boolean localMode = checkForLocalMode();

        private boolean checkForLocalMode() {
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

        @Override
        public boolean isLocalMode() {
            return localMode;
        }

        @Override
        public Property getSkin(SuperiorPlayer superiorPlayer) {
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
