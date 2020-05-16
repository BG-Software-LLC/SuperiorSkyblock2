package com.bgsoftware.superiorskyblock.hooks;

import org.bukkit.entity.Player;

public interface PermissionsProvider {

    boolean hasPermission(Player player, String permission);

}
