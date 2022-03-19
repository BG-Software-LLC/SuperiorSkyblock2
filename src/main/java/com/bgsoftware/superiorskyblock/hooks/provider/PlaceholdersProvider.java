package com.bgsoftware.superiorskyblock.hooks.provider;

import org.bukkit.OfflinePlayer;

public interface PlaceholdersProvider {

    String parsePlaceholders(OfflinePlayer offlinePlayer, String value);

}
