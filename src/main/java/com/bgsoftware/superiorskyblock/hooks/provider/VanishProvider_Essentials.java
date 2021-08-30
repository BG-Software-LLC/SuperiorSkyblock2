package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.logic.PlayersLogic;
import com.earth2me.essentials.Essentials;
import net.ess3.api.events.VanishStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanishProvider_Essentials implements VanishProvider, Listener {

    private static boolean alreadyEnabled = false;

    private final Essentials instance;
    private final SuperiorSkyblockPlugin plugin;

    public VanishProvider_Essentials(SuperiorSkyblockPlugin plugin){
        instance = JavaPlugin.getPlugin(Essentials.class);
        this.plugin = plugin;

        if(!alreadyEnabled){
            alreadyEnabled = true;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        SuperiorSkyblockPlugin.log("Hooked into Essentials for support of vanish status of players.");
    }

    @Override
    public boolean isVanished(Player player) {
        return instance.getUser(player).isVanished();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVanish(VanishStatusChangeEvent e){
        if(e.getValue()) {
            PlayersLogic.handleQuit(plugin.getPlayers().getSuperiorPlayer(e.getAffected().getBase()));
        }
        else{
            PlayersLogic.handleJoin(plugin.getPlayers().getSuperiorPlayer(e.getAffected().getBase()));
        }
    }

}
