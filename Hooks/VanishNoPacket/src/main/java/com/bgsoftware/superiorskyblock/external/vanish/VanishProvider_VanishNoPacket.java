package com.bgsoftware.superiorskyblock.external.vanish;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.VanishProvider;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.island.notifications.IslandNotifications;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.event.VanishStatusChangeEvent;

public class VanishProvider_VanishNoPacket implements VanishProvider, Listener {

    private static boolean alreadyEnabled = false;

    private final SuperiorSkyblockPlugin plugin;
    private final VanishPlugin instance;

    public VanishProvider_VanishNoPacket(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.instance = JavaPlugin.getPlugin(VanishPlugin.class);

        if (!alreadyEnabled) {
            alreadyEnabled = true;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        Log.info("Hooked into VanishNoPacket for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return instance.getManager().isVanished(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVanish(VanishStatusChangeEvent e) {
        if (e.isVanishing()) {
            IslandNotifications.notifyPlayerQuit(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
        } else {
            IslandNotifications.notifyPlayerJoin(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
        }
    }

}
