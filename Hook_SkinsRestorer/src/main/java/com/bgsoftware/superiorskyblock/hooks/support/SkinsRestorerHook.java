package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.mojang.authlib.properties.Property;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.bukkit.events.SkinApplyBukkitEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.shared.exception.SkinRequestException;
import skinsrestorer.shared.storage.SkinStorage;

@SuppressWarnings("unused")
public final class SkinsRestorerHook {

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
        plugin.getProviders().registerSkinsListener(SkinsRestorerHook::setSkinTexture);
        plugin.getServer().getPluginManager().registerEvents(new SkinsListener(), plugin);
    }

    private static void setSkinTexture(SuperiorPlayer superiorPlayer) {
        if (Bukkit.isPrimaryThread()) {
            Executor.async(() -> setSkinTexture(superiorPlayer));
            return;
        }

        Property property = skinsRestorer.getSkin(superiorPlayer);
        if (property != null)
            Executor.sync(() -> plugin.getNMSPlayers().setSkinTexture(superiorPlayer, property));
    }

    interface ISkinsRestorer {

        Property getSkin(SuperiorPlayer superiorPlayer);

    }

    private static final class SkinsRestorerOld implements ISkinsRestorer {

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

    private static final class SkinsRestorerNew implements ISkinsRestorer {

        private static final ReflectMethod<Object> SKINS_RESTORER_GET_SKIN = new ReflectMethod<>(SkinsRestorerAPI.class, "getSkinData", String.class);

        @Override
        public Property getSkin(SuperiorPlayer superiorPlayer) {
            try {
                return (Property) SkinsRestorerAPI.getApi().getSkinData(superiorPlayer.getName());
            } catch (Throwable ex) {
                return (Property) SKINS_RESTORER_GET_SKIN.invoke(SkinsRestorerAPI.getApi(), superiorPlayer.getName());
            }
        }

    }

    private static final class SkinsListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSkinApply(SkinApplyBukkitEvent event) {
            if(event.getProperty() instanceof Property) {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getWho());
                plugin.getNMSPlayers().setSkinTexture(superiorPlayer, (Property) event.getProperty());
            }
        }

    }

}
