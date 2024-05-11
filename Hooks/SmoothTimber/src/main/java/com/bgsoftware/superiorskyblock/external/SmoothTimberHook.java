package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.service.region.ProtectionHelper;
import com.syntaxphoenix.spigot.smoothtimber.event.AsyncPlayerChopTreeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SmoothTimberHook {

    private static SuperiorSkyblockPlugin plugin;
    private static final LazyReference<RegionManagerService> protectionManager = new LazyReference<>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };

    public static void register(SuperiorSkyblockPlugin plugin) {
        SmoothTimberHook.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new ChopListener(), plugin);
    }

    private static class ChopListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onChopEvent(AsyncPlayerChopTreeEvent e) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

            InteractionResult interactionResult = protectionManager.get().handleBlockBreak(superiorPlayer, e.getTreeLocation().getBlock());
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true)) {
                e.getBlockLocations().clear();
                return;
            }

            Island island = plugin.getGrid().getIslandAt(e.getTreeLocation());
            if (island != null)
                e.getBlockLocations().removeIf(location -> !island.isInsideRange(location));
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onChopEventMonitor(AsyncPlayerChopTreeEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getTreeLocation());

            if (island == null)
                return;

            e.getBlockLocations().forEach(location -> island.handleBlockBreak(Keys.of(location.getBlock())));
        }

    }

}
