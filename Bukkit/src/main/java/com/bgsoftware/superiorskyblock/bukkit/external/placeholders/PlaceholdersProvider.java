package com.bgsoftware.superiorskyblock.bukkit.external.placeholders;

import org.bukkit.OfflinePlayer;

public interface PlaceholdersProvider {

    String parsePlaceholders(OfflinePlayer offlinePlayer, String value);

}
