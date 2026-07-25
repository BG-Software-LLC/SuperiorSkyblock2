package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.mojang.authlib.properties.Property;
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.shared.exception.SkinRequestException;
import skinsrestorer.shared.storage.SkinStorage;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class SkinsRestorerHook {

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin, JavaPlugin javaPlugin) {
        SkinsRestorerHook.plugin = plugin;
        plugin.getProviders().registerSkinsListener(SkinsRestorerHook::setSkinTexture);
    }

    private static void setSkinTexture(SuperiorPlayer superiorPlayer) {
        plugin.getPlatform().getScheduler().ensureAsync(() -> setSkinTextureInternal(superiorPlayer));
    }

    private static void setSkinTextureInternal(SuperiorPlayer superiorPlayer) {
        Property property = getSkin(superiorPlayer);
        if (property != null)
            plugin.getPlatform().getScheduler().runSync(() -> plugin.getNMSPlayers().setSkinTexture(superiorPlayer, property));
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
