package com.bgsoftware.superiorskyblock.dependencies.provider;

import org.bukkit.OfflinePlayer;

public interface PlaceholdersProvider {

    String parsePlaceholders(OfflinePlayer offlinePlayer, String value);

}
