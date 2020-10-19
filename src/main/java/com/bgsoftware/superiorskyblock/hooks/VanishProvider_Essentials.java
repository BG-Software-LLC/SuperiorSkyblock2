package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.earth2me.essentials.Essentials;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanishProvider_Essentials implements VanishProvider {

    private final Essentials instance;

    public VanishProvider_Essentials(){
        instance = JavaPlugin.getPlugin(Essentials.class);
        SuperiorSkyblockPlugin.log("Hooked into Essentials for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return instance.getUser(player).isVanished();
    }

}
