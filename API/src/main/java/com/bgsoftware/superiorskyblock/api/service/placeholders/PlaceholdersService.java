package com.bgsoftware.superiorskyblock.api.service.placeholders;

import org.bukkit.OfflinePlayer;

public interface PlaceholdersService {

    /**
     * Parse placeholders in a given string.
     *
     * @param offlinePlayer The player to parse placeholders to.
     * @param value         The string to parse.
     * @return The same string with the placeholders after they were parsed.
     */
    String parsePlaceholders(OfflinePlayer offlinePlayer, String value);

    /**
     * Register a new placeholder.
     *
     * @param placeholderName     The name of the placeholder.
     *                            The name is the part in the placeholder after the prefix (`superior_<name>`)
     * @param placeholderFunction The parser to run when the placeholder is evaluated.
     *                            It is recommended to cache all values return by the placeholder for best performance.
     */
    void registerPlaceholder(String placeholderName, IslandPlaceholderParser placeholderFunction);

    /**
     * Register a new placeholder.
     *
     * @param placeholderName     The name of the placeholder.
     *                            The name is the part in the placeholder after the prefix (`superior_<name>`)
     * @param placeholderFunction The parser to run when the placeholder is evaluated.
     *                            It is recommended to cache all values return by the placeholder for best performance.
     */
    void registerPlaceholder(String placeholderName, PlayerPlaceholderParser placeholderFunction);

}
