package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.logic.BlocksLogic;
import com.bgsoftware.superiorskyblock.utils.logic.StackedBlocksLogic;
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
        if(StackedBlocksLogic.tryUnstack(null, e.getBlock(), plugin))
            e.setCancelled(true);
        else
            BlocksLogic.handleBreak(e.getBlock());
    }

    public static void register(SuperiorSkyblockPlugin plugin){
        Bukkit.getPluginManager().registerEvents(new JetsMinionsHook(plugin), plugin);
    }

}
