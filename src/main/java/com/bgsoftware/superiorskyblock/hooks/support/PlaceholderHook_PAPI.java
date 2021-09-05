package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PlaceholderHook_PAPI extends PlaceholderHook {

    PlaceholderHook_PAPI(){
        SuperiorSkyblockPlugin.log("Using PlaceholderAPI for placeholders support.");
        new EZPlaceholder().register();
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
        public String onPlaceholderRequest(Player player, String placeholder) {
            return onRequest(player, placeholder);
        }

        @Override
        public String onRequest(OfflinePlayer player, String placeholder) {
            return parsePlaceholder(player, placeholder);
        }

        @Override
        public boolean persist() {
            return true;
        }
    }

    public static String parse(OfflinePlayer offlinePlayer, String str){
        return PlaceholderAPI.setPlaceholders(offlinePlayer, str);
    }

}
