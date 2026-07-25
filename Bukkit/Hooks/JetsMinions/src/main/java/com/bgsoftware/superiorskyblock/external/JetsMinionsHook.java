package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.StackedBlocksInteractionService;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.service.stackedblocks.StackedBlocksServiceHelper;
import me.jet315.minions.events.MinerBlockBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class JetsMinionsHook implements Listener {

    @WorldRecordFlags
    private static final int REGULAR_RECORD_FLAGS = WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS;

    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };
    private final LazyReference<StackedBlocksInteractionService> stackedBlocksInteractionService = new LazyReference<StackedBlocksInteractionService>() {
        @Override
        protected StackedBlocksInteractionService create() {
            return plugin.getServices().getService(StackedBlocksInteractionService.class);
        }
    };

    private final SuperiorSkyblockPlugin plugin;

    private JetsMinionsHook(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public static void register(SuperiorSkyblockPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new JetsMinionsHook(plugin), plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMinionBreak(MinerBlockBreakEvent e) {
        Log.debug(Debug.BLOCK_BREAK, e.getBlock().getType());

        InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                .handleStackedBlockBreak(e.getBlock(), null);
        if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult)) {
            e.setCancelled(true);
        } else {
            this.worldRecordService.get().recordBlockBreak(e.getBlock(), REGULAR_RECORD_FLAGS);
        }
    }

}
