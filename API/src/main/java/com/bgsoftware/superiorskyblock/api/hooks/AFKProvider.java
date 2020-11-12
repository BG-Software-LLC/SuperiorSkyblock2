package com.bgsoftware.superiorskyblock.api.hooks;

import org.bukkit.entity.Player;

public interface AFKProvider {

    boolean isAFK(Player player);

}
