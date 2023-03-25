package com.bgsoftware.superiorskyblock.external.afk;

import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.earth2me.essentials.Essentials;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AFKProvider_Essentials implements AFKProvider {

    private final Essentials instance;

    public AFKProvider_Essentials() {
        instance = JavaPlugin.getPlugin(Essentials.class);
        Log.info("Hooked into Essentials for support of afk status of players.");
    }

    @Override
    public boolean isAFK(Player player) {
        Preconditions.checkNotNull(player, "player parameter cannot be null.");
        return instance.getUser(player).isAfk();
    }

}
