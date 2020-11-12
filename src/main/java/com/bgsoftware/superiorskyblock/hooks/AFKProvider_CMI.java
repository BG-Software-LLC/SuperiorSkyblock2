package com.bgsoftware.superiorskyblock.hooks;

import com.Zrips.CMI.CMI;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import org.bukkit.entity.Player;

public final class AFKProvider_CMI implements AFKProvider {

    public AFKProvider_CMI(){
        SuperiorSkyblockPlugin.log("Hooked into CMI for support of afk status of players.");
    }

    @Override
    public boolean isAFK(Player player) {
        return CMI.getInstance().getPlayerManager().getUser(player).isAfk();
    }

}
