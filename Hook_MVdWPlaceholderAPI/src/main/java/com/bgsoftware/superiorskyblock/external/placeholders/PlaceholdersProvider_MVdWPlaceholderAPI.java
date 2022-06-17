package com.bgsoftware.superiorskyblock.external.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.external.placeholders.PlaceholdersProvider;
import com.bgsoftware.superiorskyblock.service.placeholders.PlaceholdersServiceImpl;
import org.bukkit.OfflinePlayer;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class PlaceholdersProvider_MVdWPlaceholderAPI implements PlaceholdersProvider {

    private static final Pattern BUILT_IN_NUMERIC_PLACEHOLDER = Pattern.compile("\\{(\\d)}");

    public PlaceholdersProvider_MVdWPlaceholderAPI(SuperiorSkyblockPlugin plugin) {
        SuperiorSkyblockPlugin.log("Using MVdWPlaceholderAPI for placeholders support.");
        PlaceholdersServiceImpl placeholdersService = (PlaceholdersServiceImpl) plugin.getServices().getPlaceholdersService();
        PlaceholderAPI.registerPlaceholder(plugin, "superior_*", e ->
                placeholdersService.handlePluginPlaceholder(e.getOfflinePlayer(), e.getPlaceholder().replace("superior_", "")));
    }

    @Override
    public String parsePlaceholders(OfflinePlayer offlinePlayer, String value) {
        return PlaceholderAPI.replacePlaceholders(offlinePlayer, BUILT_IN_NUMERIC_PLACEHOLDER.matcher(value)
                .replaceAll("{%$1}")).replace("{%", "{");
    }

}
