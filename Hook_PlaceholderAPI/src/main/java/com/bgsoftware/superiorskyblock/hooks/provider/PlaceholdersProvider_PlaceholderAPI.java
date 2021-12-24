package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public final class PlaceholdersProvider_PlaceholderAPI extends PlaceholderHook implements PlaceholdersProvider {

    private final SuperiorSkyblockPlugin plugin;

    public PlaceholdersProvider_PlaceholderAPI(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        new EZPlaceholder().register();

        SuperiorSkyblockPlugin.log("Using PlaceholderAPI for placeholders support.");
    }

    @Override
    public String parsePlaceholder(OfflinePlayer offlinePlayer, String value) {
        return PlaceholderAPI.setPlaceholders(offlinePlayer, value);
    }

    private class EZPlaceholder extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "superior";
        }

        @Override
        public String getAuthor() {
            return "Ome_R";
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onRequest(OfflinePlayer player, String placeholder) {
            return handlePluginPlaceholder(player, placeholder);
        }

        @Override
        public String onPlaceholderRequest(Player player, String placeholder) {
            return onRequest(player, placeholder);
        }
    }

}
