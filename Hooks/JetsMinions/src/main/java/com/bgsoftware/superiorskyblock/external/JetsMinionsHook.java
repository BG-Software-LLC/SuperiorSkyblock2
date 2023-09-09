package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
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
        Log.debug(Debug.BLOCK_BREAK, e.getBlock().getType());

        StackedBlocksListener.UnstackResult unstackResult = stackedBlocksListener.get().tryUnstack(null, e.getBlock());

        if (unstackResult.shouldCancelOriginalEvent()) {
            e.setCancelled(true);
        } else {
            blockChangesListener.get().onBlockBreak(Keys.of(e.getBlock()), e.getBlock().getLocation(),
                    plugin.getNMSWorld().getDefaultAmount(e.getBlock()),
                    BlockChangesListener.BlockTrackFlags.DIRTY_CHUNKS | BlockChangesListener.BlockTrackFlags.SAVE_BLOCK_COUNT);
        }
    }

}
