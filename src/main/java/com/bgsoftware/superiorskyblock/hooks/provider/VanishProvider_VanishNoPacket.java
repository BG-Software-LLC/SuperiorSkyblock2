package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.logic.PlayersLogic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.event.VanishStatusChangeEvent;

public final class VanishProvider_VanishNoPacket implements VanishProvider, Listener {

    private static boolean alreadyEnabled = false;

    private final SuperiorSkyblockPlugin plugin;
    private final VanishPlugin instance;

    public VanishProvider_VanishNoPacket(SuperiorSkyblockPlugin plugin){
        instance = JavaPlugin.getPlugin(VanishPlugin.class);
        this.plugin = plugin;

        if(!alreadyEnabled){
            alreadyEnabled = true;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        SuperiorSkyblockPlugin.log("Hooked into VanishNoPacket for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return instance.getManager().isVanished(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVanish(VanishStatusChangeEvent e){
        if(e.isVanishing()) {
            PlayersLogic.handleQuit(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
        }
        else{
            PlayersLogic.handleJoin(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
        }
    }

}
