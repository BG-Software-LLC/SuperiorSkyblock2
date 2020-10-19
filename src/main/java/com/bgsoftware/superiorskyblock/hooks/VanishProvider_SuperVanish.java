package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.entity.Player;

public final class VanishProvider_SuperVanish implements VanishProvider {

    public VanishProvider_SuperVanish(){
        SuperiorSkyblockPlugin.log("Hooked into SuperVanish for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return VanishAPI.isInvisible(player);
    }

}
