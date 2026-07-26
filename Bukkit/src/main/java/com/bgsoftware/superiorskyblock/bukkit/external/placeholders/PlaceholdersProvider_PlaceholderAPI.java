package com.bgsoftware.superiorskyblock.bukkit.external.placeholders;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.bukkit.service.placeholders.BukkitPlaceholdersService;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class PlaceholdersProvider_PlaceholderAPI implements PlaceholdersProvider {

    private final SuperiorSkyblockPlugin plugin;

    public PlaceholdersProvider_PlaceholderAPI(SuperiorSkyblockPlugin plugin, JavaPlugin javaPlugin) {
        this.plugin = plugin;
        new EZPlaceholder((BukkitPlaceholdersService) plugin.getServices().getService(PlaceholdersService.class)).register();

        Log.info("Using PlaceholderAPI for placeholders support.");
    }

    @Override
    public String parsePlaceholders(OfflinePlayer offlinePlayer, String value) {
        return PlaceholderAPI.setPlaceholders(offlinePlayer, value);
    }

    private class EZPlaceholder extends PlaceholderExpansion {

        private final BukkitPlaceholdersService placeholdersService;

        public EZPlaceholder(BukkitPlaceholdersService placeholdersService) {
            this.placeholdersService = placeholdersService;
        }

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
            return plugin.getPluginVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onRequest(OfflinePlayer player, String placeholder) {
            return placeholdersService.handlePluginPlaceholder(player, placeholder);
        }

        @Override
        public String onPlaceholderRequest(Player player, String placeholder) {
            return onRequest(player, placeholder);
        }
    }

}
