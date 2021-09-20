package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.logic.PlayersLogic;
import de.myzelyam.api.vanish.PlayerVanishStateChangeEvent;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class VanishProvider_SuperVanish implements VanishProvider, Listener {

    private static boolean alreadyEnabled = false;

    private final SuperiorSkyblockPlugin plugin;

    public VanishProvider_SuperVanish(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;

        if(!alreadyEnabled){
            alreadyEnabled = true;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        SuperiorSkyblockPlugin.log("Hooked into SuperVanish for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return VanishAPI.isInvisible(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVanish(PlayerVanishStateChangeEvent e){
        if(e.isVanishing()) {
            PlayersLogic.handleQuit(plugin.getPlayers().getSuperiorPlayer(e.getUUID()));
        }
        else{
            PlayersLogic.handleJoin(plugin.getPlayers().getSuperiorPlayer(e.getUUID()));
        }
    }

}
