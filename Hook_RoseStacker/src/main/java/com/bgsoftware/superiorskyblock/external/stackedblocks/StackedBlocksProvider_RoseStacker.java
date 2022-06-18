package com.bgsoftware.superiorskyblock.external.stackedblocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.external.stackedblocks.StackedBlocksProvider_AutoDetect;
import com.bgsoftware.superiorskyblock.listener.ProtectionListener;
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
    private final Singleton<ProtectionListener> protectionListener;

    public StackedBlocksProvider_RoseStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.protectionListener = plugin.getListener(ProtectionListener.class);

        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), plugin);
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
                    Key blockKey = Key.of(entry.getKey());
                    blockKeys.put(blockKey, blockKeys.getOrDefault(blockKey, 0) + entry.getValue().getStackSize());
                });
        return blockKeys.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).collect(Collectors.toSet());
    }

    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockStack(BlockStackEvent e) {
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null) {
                int placedBlocksAmount = e.isNew() ? Math.max(1, e.getIncreaseAmount() - 1) : e.getIncreaseAmount();
                island.handleBlockPlace(e.getStack().getBlock(), placedBlocksAmount);
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
            if (protectionListener.get().preventBlockPlace(e.getStack().getBlock(), e.getPlayer(),
                    ProtectionListener.Flag.SEND_MESSAGES, ProtectionListener.Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBlockUnstackProtection(BlockUnstackEvent e) {
            if (e.getPlayer() != null && protectionListener.get().preventBlockPlace(e.getStack().getBlock(), e.getPlayer(),
                    ProtectionListener.Flag.SEND_MESSAGES, ProtectionListener.Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBlockStackInteractionProtection(PlayerInteractEvent e) {
            if (e.getClickedBlock() != null && e.getPlayer().isSneaking()) {
                StackedBlock stackedBlock = RoseStackerAPI.getInstance().getStackedBlock(e.getClickedBlock());
                if (stackedBlock != null) {
                    if (protectionListener.get().preventBlockPlace(e.getClickedBlock(), e.getPlayer(),
                            ProtectionListener.Flag.SEND_MESSAGES, ProtectionListener.Flag.PREVENT_OUTSIDE_ISLANDS))
                        e.setCancelled(true);
                }
            }
        }

    }

}
