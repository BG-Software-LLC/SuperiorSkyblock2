package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.earth2me.essentials.Essentials;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AFKProvider_Essentials implements AFKProvider {

    private final Essentials instance;

    public AFKProvider_Essentials(){
        instance = JavaPlugin.getPlugin(Essentials.class);
        SuperiorSkyblockPlugin.log("Hooked into Essentials for support of afk status of players.");
    }

    @Override
    public boolean isAFK(Player player) {
        return instance.getUser(player).isAfk();
    }

}
