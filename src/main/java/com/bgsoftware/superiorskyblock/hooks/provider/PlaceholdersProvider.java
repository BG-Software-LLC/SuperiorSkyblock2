package com.bgsoftware.superiorskyblock.hooks.provider;

import org.bukkit.OfflinePlayer;

public interface PlaceholdersProvider {

    String parsePlaceholder(OfflinePlayer offlinePlayer, String value);

}
