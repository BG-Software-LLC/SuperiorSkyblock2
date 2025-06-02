package com.bgsoftware.superiorskyblock.external.stackedblocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.service.region.ProtectionHelper;
import com.google.common.base.Preconditions;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.BlockStackEvent;
import dev.rosewood.rosestacker.event.BlockUnstackEvent;
import dev.rosewood.rosestacker.stack.StackedBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StackedBlocksProvider_RoseStacker implements StackedBlocksProvider_AutoDetect {

    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;
    private final LazyReference<RegionManagerService> protectionManager = new LazyReference<RegionManagerService>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };

    public StackedBlocksProvider_RoseStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), plugin);
            registered = true;
            Log.info("Using RoseStacker as a stacked-blocks provider.");
        }
    }

    @Override
    public Collection<Pair<Key, Integer>> getBlocks(World world, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");

        if (!Bukkit.isPrimaryThread())
            return null;

        Map<Key, Integer> blockKeys = new HashMap<>();

        try (ChunkPosition chunkPosition = ChunkPosition.of(world, chunkX, chunkZ);
             ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            RoseStackerAPI.getInstance().getStackedBlocks().entrySet().stream()
                    .filter(entry ->
                            chunkPosition.isInsideChunk(entry.getKey().getLocation(wrapper.getHandle())))
                    .forEach(entry -> {
                        Key blockKey = Key.of(entry.getKey());
                        blockKeys.put(blockKey, blockKeys.getOrDefault(blockKey, 0) + entry.getValue().getStackSize());
                    });
        }

        return blockKeys.entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockStack(BlockStackEvent e) {
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null)
                return;

            Key blockKey = Keys.of(e.getStack().getBlock());

            int increaseAmount = e.getStack().getStackSize() + e.getIncreaseAmount() > e.getStack().getStackSettings().getMaxStackSize() ?
                    e.getStack().getStackSettings().getMaxStackSize() - e.getStack().getStackSize() : e.getIncreaseAmount();
            int newBlocksCount = e.isNew() ? Math.max(1, increaseAmount - 1) : increaseAmount;

            if (island.hasReachedBlockLimit(blockKey, newBlocksCount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            } else {
                island.handleBlockPlace(blockKey, newBlocksCount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockUnstack(BlockUnstackEvent e) {
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null) {
                island.handleBlockBreak(e.getStack().getBlock(), e.getDecreaseAmount());
            }
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBlockStackProtection(BlockStackEvent e) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            InteractionResult interactionResult = protectionManager.get().handleBlockPlace(superiorPlayer, e.getStack().getBlock());
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBlockUnstackProtection(BlockUnstackEvent e) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            InteractionResult interactionResult = protectionManager.get().handleBlockBreak(superiorPlayer, e.getStack().getBlock());
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBlockStackInteractionProtection(PlayerInteractEvent e) {
            if (e.getClickedBlock() == null || e.getPlayer().isSneaking())
                return;

            StackedBlock stackedBlock = RoseStackerAPI.getInstance().getStackedBlock(e.getClickedBlock());
            if (stackedBlock == null)
                return;


            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            InteractionResult interactionResult = protectionManager.get().handleBlockPlace(superiorPlayer, e.getClickedBlock());
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
                e.setCancelled(true);
        }

    }

}
