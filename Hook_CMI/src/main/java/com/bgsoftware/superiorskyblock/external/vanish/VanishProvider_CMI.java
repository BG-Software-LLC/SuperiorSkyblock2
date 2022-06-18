package com.bgsoftware.superiorskyblock.external.vanish;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.events.CMIPlayerUnVanishEvent;
import com.Zrips.CMI.events.CMIPlayerVanishEvent;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.VanishProvider;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.listener.PlayersListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class VanishProvider_CMI implements VanishProvider, Listener {

    private static boolean alreadyEnabled = false;

    private final SuperiorSkyblockPlugin plugin;
    private final Singleton<PlayersListener> playersListener;

    public VanishProvider_CMI(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.playersListener = plugin.getListener(PlayersListener.class);

        if (!alreadyEnabled) {
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
    public void onPlayerVanish(CMIPlayerVanishEvent e) {
        this.playersListener.get().notifyPlayerQuit(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerUnvanish(CMIPlayerUnVanishEvent e) {
        this.playersListener.get().notifyPlayerJoin(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
    }

}
