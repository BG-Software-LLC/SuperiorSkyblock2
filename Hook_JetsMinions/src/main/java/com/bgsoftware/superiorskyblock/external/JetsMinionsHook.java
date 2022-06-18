package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.listener.BlockChangesListener;
import com.bgsoftware.superiorskyblock.listener.StackedBlocksListener;
import me.jet315.minions.events.MinerBlockBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public class JetsMinionsHook implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    private final Singleton<StackedBlocksListener> stackedBlocksListener;
    private final Singleton<BlockChangesListener> blockChangesListener;

    private JetsMinionsHook(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.stackedBlocksListener = plugin.getListener(StackedBlocksListener.class);
        this.blockChangesListener = plugin.getListener(BlockChangesListener.class);
    }

    public static void register(SuperiorSkyblockPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new JetsMinionsHook(plugin), plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMinionBreak(MinerBlockBreakEvent e) {
        PluginDebugger.debug("Action: Jets Minion Break, Block: " + e.getBlock().getLocation() + ", Type: " + e.getBlock().getType());
        if (stackedBlocksListener.get().tryUnstack(null, e.getBlock())) {
            e.setCancelled(true);
        } else {
            blockChangesListener.get().onBlockBreak(KeyImpl.of(e.getBlock()), e.getBlock().getLocation(),
                    plugin.getNMSWorld().getDefaultAmount(e.getBlock()),
                    BlockChangesListener.Flag.DIRTY_CHUNK, BlockChangesListener.Flag.SAVE_BLOCK_COUNT);
        }
    }

}
