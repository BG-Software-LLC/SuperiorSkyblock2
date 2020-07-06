package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import me.jet315.minions.events.MinerBlockBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class JetsMinionsHook implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    private JetsMinionsHook(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMinionBreak(MinerBlockBreakEvent e){
        SuperiorSkyblockPlugin.debug("Action: Jets Minion Break, Block: " + e.getBlock().getLocation() + ", Type: " + e.getBlock().getType());
        if(BlocksListener.tryUnstack(null, e.getBlock(), plugin))
            e.setCancelled(true);
        else
            BlocksListener.handleBlockBreak(plugin, e.getBlock());
    }

    public static void register(SuperiorSkyblockPlugin plugin){
        Bukkit.getPluginManager().registerEvents(new JetsMinionsHook(plugin), plugin);
    }

}
