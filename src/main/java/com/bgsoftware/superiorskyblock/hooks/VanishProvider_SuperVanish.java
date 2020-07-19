package com.bgsoftware.superiorskyblock.hooks;

import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.entity.Player;

public final class VanishProvider_SuperVanish implements VanishProvider {

    @Override
    public boolean isVanished(Player player) {
        return VanishAPI.isInvisible(player);
    }

}
