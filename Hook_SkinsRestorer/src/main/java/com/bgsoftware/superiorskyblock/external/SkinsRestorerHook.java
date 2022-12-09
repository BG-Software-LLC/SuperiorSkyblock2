package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.shared.exception.SkinRequestException;
import skinsrestorer.shared.storage.SkinStorage;

@SuppressWarnings("unused")
public class SkinsRestorerHook {

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin) {
        SkinsRestorerHook.plugin = plugin;
        plugin.getProviders().registerSkinsListener(SkinsRestorerHook::setSkinTexture);
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

    public static Property getSkin(SuperiorPlayer superiorPlayer) {
        try {
            SkinStorage skinStorage = SkinsRestorer.getInstance().getSkinStorage();
            return (Property) skinStorage.getOrCreateSkinForPlayer(superiorPlayer.getName(), true);
        } catch (SkinRequestException | NullPointerException ignored) {
            return null;
        }
    }

}
