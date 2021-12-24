package com.bgsoftware.superiorskyblock.hooks.provider;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import org.bukkit.OfflinePlayer;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class PlaceholdersProvider_MVdWPlaceholderAPI extends PlaceholderHook implements PlaceholdersProvider {

    private static final Pattern BUILT_IN_NUMERIC_PLACEHOLDER = Pattern.compile("\\{(\\d)}");

    public PlaceholdersProvider_MVdWPlaceholderAPI(SuperiorSkyblockPlugin plugin) {
        SuperiorSkyblockPlugin.log("Using MVdWPlaceholderAPI for placeholders support.");
        PlaceholderAPI.registerPlaceholder(plugin, "superior_*", e ->
                handlePluginPlaceholder(e.getOfflinePlayer(), e.getPlaceholder().replace("superior_", "")));
    }

    @Override
    public String parsePlaceholder(OfflinePlayer offlinePlayer, String value) {
        return PlaceholderAPI.replacePlaceholders(offlinePlayer, BUILT_IN_NUMERIC_PLACEHOLDER.matcher(value)
                .replaceAll("{%$1}")).replace("{%", "{");
    }

}
