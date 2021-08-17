package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.logic.ProtectionLogic;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class StackedBlocksProvider_RoseStacker implements StackedBlocksProvider_AutoDetect {

    private static boolean registered = false;

    public StackedBlocksProvider_RoseStacker() {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;
            SuperiorSkyblockPlugin.log("Using RoseStacker as a stacked-blocks provider.");
        }
    }

    @Override
    public Collection<Pair<Key, Integer>> getBlocks(World world, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");

        if (!Bukkit.isPrimaryThread())
            return null;

        ChunkPosition chunkPosition = ChunkPosition.of(world, chunkX, chunkZ);

        Map<Key, Integer> blockKeys = new HashMap<>();
        RoseStackerAPI.getInstance().getStackedBlocks().entrySet().stream()
                .filter(entry -> chunkPosition.isInsideChunk(entry.getKey().getLocation()))
                .forEach(entry -> {
                    com.bgsoftware.superiorskyblock.api.key.Key blockKey = com.bgsoftware.superiorskyblock.utils.key.Key.of(entry.getKey());
                    blockKeys.put(blockKey, blockKeys.getOrDefault(blockKey, 0) + entry.getValue().getStackSize());
                });
        return blockKeys.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).collect(Collectors.toSet());
    }

    @SuppressWarnings("unused")
    private static final class StackerListener implements Listener {

        private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR)
        public void onBlockPlace(BlockPlaceEvent e) {
            Location location = e.getBlock().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null && e.isCancelled()) {
                StackedBlock stackedBlock = RoseStackerAPI.getInstance().getStackedBlock(e.getBlockAgainst());
                if (stackedBlock != null) {
                    // We want to add additional one block, to adjust the counts to the correct value.
                    com.bgsoftware.superiorskyblock.utils.key.Key blockKey = com.bgsoftware.superiorskyblock.utils.key.Key.of(e.getBlock());
                    island.handleBlockPlace(blockKey, 1);
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockStack(BlockStackEvent e) {
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null) {
                com.bgsoftware.superiorskyblock.utils.key.Key blockKey = com.bgsoftware.superiorskyblock.utils.key.Key.of(e.getStack().getBlock());
                int placedBlocksAmount = e.isNew() ? e.getIncreaseAmount() - 1 : e.getIncreaseAmount();
                if (placedBlocksAmount > 0)
                    island.handleBlockPlace(blockKey, placedBlocksAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockUnstack(BlockUnstackEvent e) {
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null) {
                com.bgsoftware.superiorskyblock.utils.key.Key blockKey = com.bgsoftware.superiorskyblock.utils.key.Key.of(e.getStack().getBlock());
                island.handleBlockBreak(blockKey, e.getDecreaseAmount());
            }
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBlockStackProtection(BlockStackEvent e) {
            if (!ProtectionLogic.handleBlockPlace(e.getStack().getBlock(), e.getPlayer(), true))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBlockUnstackProtection(BlockUnstackEvent e) {
            if (e.getPlayer() != null && !ProtectionLogic.handleBlockBreak(
                    e.getStack().getBlock(), e.getPlayer(), true))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBlockStackInteractionProtection(PlayerInteractEvent e) {
            if (e.getClickedBlock() == null || !e.getPlayer().isSneaking())
                return;

            Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

            if (island == null)
                return;

            StackedBlock stackedBlock = RoseStackerAPI.getInstance().getStackedBlock(e.getClickedBlock());

            if (stackedBlock == null)
                return;

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

            if (!island.hasPermission(superiorPlayer, IslandPrivileges.BUILD)) {
                Locale.sendProtectionMessage(superiorPlayer);
                e.setCancelled(true);
            }
        }

    }

}
