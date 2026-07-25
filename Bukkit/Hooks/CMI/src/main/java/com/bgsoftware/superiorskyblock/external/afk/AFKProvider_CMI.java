package com.bgsoftware.superiorskyblock.external.afk;

import com.Zrips.CMI.CMI;
import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;

public class AFKProvider_CMI implements AFKProvider {

    public AFKProvider_CMI() {
        Log.info("Hooked into CMI for support of afk status of players.");
    }

    @Override
    public boolean isAFK(Player player) {
        Preconditions.checkNotNull(player, "player parameter cannot be null.");
        return CMI.getInstance().getPlayerManager().getUser(player).isAfk();
    }

}
