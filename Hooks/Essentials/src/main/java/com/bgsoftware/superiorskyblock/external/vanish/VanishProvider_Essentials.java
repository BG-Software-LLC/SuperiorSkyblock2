package com.bgsoftware.superiorskyblock.external.vanish;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.VanishProvider;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.island.notifications.IslandNotifications;
import com.earth2me.essentials.Essentials;
import net.ess3.api.events.VanishStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class VanishProvider_Essentials implements VanishProvider, Listener {

    private static boolean alreadyEnabled = false;

    private final SuperiorSkyblockPlugin plugin;
    private final Essentials instance;

    public VanishProvider_Essentials(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.instance = JavaPlugin.getPlugin(Essentials.class);

        if (!alreadyEnabled) {
            alreadyEnabled = true;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        Log.info("Hooked into Essentials for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return instance.getUser(player).isVanished();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVanish(VanishStatusChangeEvent e) {
        Player affectedPlayer = e.getAffected() == null ? e.getController().getBase() : e.getAffected().getBase();
        if (e.getValue()) {
            IslandNotifications.notifyPlayerQuit(plugin.getPlayers().getSuperiorPlayer(affectedPlayer));
        } else {
            IslandNotifications.notifyPlayerJoin(plugin.getPlayers().getSuperiorPlayer(affectedPlayer));
        }
    }

}
