package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.VanishPlugin;

public final class VanishProvider_VanishNoPacket implements VanishProvider {

    private final VanishPlugin instance;

    public VanishProvider_VanishNoPacket(){
        instance = JavaPlugin.getPlugin(VanishPlugin.class);
        SuperiorSkyblockPlugin.log("Hooked into VanishNoPacket for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return instance.getManager().isVanished(player);
    }

}
