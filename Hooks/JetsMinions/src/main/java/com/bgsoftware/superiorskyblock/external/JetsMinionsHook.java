package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlag;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.listener.StackedBlocksListener;
import me.jet315.minions.events.MinerBlockBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public class JetsMinionsHook implements Listener {

    private static final WorldRecordFlag REGULAR_RECORD_FLAGS = WorldRecordFlag.SAVE_BLOCK_COUNT.and(WorldRecordFlag.DIRTY_CHUNK);

    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };
    private final SuperiorSkyblockPlugin plugin;

    private final Singleton<StackedBlocksListener> stackedBlocksListener;

    private JetsMinionsHook(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.stackedBlocksListener = plugin.getListener(StackedBlocksListener.class);
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
            this.worldRecordService.get().recordBlockBreak(e.getBlock(), REGULAR_RECORD_FLAGS);
        }
    }

}
