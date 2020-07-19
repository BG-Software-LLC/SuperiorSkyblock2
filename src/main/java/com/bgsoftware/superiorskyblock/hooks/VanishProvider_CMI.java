package com.bgsoftware.superiorskyblock.hooks;

import com.Zrips.CMI.CMI;
import org.bukkit.entity.Player;

public final class VanishProvider_CMI implements VanishProvider {

    @Override
    public boolean isVanished(Player player) {
        return CMI.getInstance().getVanishManager().getAllVanished().contains(player.getUniqueId());
    }

}
