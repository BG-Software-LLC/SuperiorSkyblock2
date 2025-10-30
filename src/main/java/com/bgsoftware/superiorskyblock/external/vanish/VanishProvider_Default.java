package com.bgsoftware.superiorskyblock.external.vanish;

import com.bgsoftware.superiorskyblock.api.hooks.VanishProvider;
import org.bukkit.entity.Player;

public class VanishProvider_Default implements VanishProvider {

    @Override
    public boolean isVanished(Player player) {
        return player.hasMetadata("vanished");
    }
}
