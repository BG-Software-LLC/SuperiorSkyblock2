package com.bgsoftware.superiorskyblock.external.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.service.placeholders.PlaceholdersServiceImpl;
import org.bukkit.OfflinePlayer;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class PlaceholdersProvider_MVdWPlaceholderAPI implements PlaceholdersProvider {

    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return SuperiorSkyblockPlugin.getPlugin().getServices().getService(PlaceholdersService.class);
        }
    };

    private static final Pattern BUILT_IN_NUMERIC_PLACEHOLDER = Pattern.compile("\\{(\\d)}");

    public PlaceholdersProvider_MVdWPlaceholderAPI(SuperiorSkyblockPlugin plugin) {
        Log.info("Using MVdWPlaceholderAPI for placeholders support.");
        PlaceholderAPI.registerPlaceholder(plugin, "superior_*", e ->
                ((PlaceholdersServiceImpl) placeholdersService.get()).handlePluginPlaceholder(e.getOfflinePlayer(),
                        e.getPlaceholder().replace("superior_", "")));
    }

    @Override
    public String parsePlaceholders(OfflinePlayer offlinePlayer, String value) {
        return PlaceholderAPI.replacePlaceholders(offlinePlayer, BUILT_IN_NUMERIC_PLACEHOLDER.matcher(value)
                .replaceAll("{%$1}")).replace("{%", "{");
    }

}
