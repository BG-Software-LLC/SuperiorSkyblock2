package com.bgsoftware.superiorskyblock.hooks;

import com.Zrips.CMI.CMI;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.entity.Player;

public final class VanishProvider_CMI implements VanishProvider {

    public VanishProvider_CMI(){
        SuperiorSkyblockPlugin.log("Hooked into CMI for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return CMI.getInstance().getVanishManager().getAllVanished().contains(player.getUniqueId());
    }

}
