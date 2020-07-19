package com.bgsoftware.superiorskyblock.hooks;

import com.earth2me.essentials.Essentials;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanishProvider_Essentials implements VanishProvider {

    private final Essentials instance;

    public VanishProvider_Essentials(){
        instance = JavaPlugin.getPlugin(Essentials.class);
    }

    @Override
    public boolean isVanished(Player player) {
        return instance.getUser(player).isVanished();
    }

}
