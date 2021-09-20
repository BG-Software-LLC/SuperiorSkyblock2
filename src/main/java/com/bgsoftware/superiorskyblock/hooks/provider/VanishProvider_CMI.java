package com.bgsoftware.superiorskyblock.hooks.provider;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.events.CMIPlayerUnVanishEvent;
import com.Zrips.CMI.events.CMIPlayerVanishEvent;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.logic.PlayersLogic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class VanishProvider_CMI implements VanishProvider, Listener {

    private static boolean alreadyEnabled = false;

    private final SuperiorSkyblockPlugin plugin;

    public VanishProvider_CMI(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;

        if(!alreadyEnabled){
            alreadyEnabled = true;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        SuperiorSkyblockPlugin.log("Hooked into CMI for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return CMI.getInstance().getVanishManager().getAllVanished().contains(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVanish(CMIPlayerVanishEvent e){
        PlayersLogic.handleQuit(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerUnvanish(CMIPlayerUnVanishEvent e){
        PlayersLogic.handleJoin(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
    }

}
