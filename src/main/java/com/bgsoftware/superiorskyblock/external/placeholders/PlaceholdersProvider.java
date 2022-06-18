package com.bgsoftware.superiorskyblock.external.placeholders;

import org.bukkit.OfflinePlayer;

public interface PlaceholdersProvider {

    String parsePlaceholders(OfflinePlayer offlinePlayer, String value);

}
