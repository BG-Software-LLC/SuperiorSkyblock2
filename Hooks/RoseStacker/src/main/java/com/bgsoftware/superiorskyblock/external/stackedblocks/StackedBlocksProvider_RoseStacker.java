package com.bgsoftware.superiorskyblock.external.stackedblocks;

import com.bgsoftware.common.collections.Maps;
import com.bgsoftware.common.collections.Sets;
import com.bgsoftware.common.collections.transform.Transformer;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.logging.Log;
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
import java.util.Map;

public class StackedBlocksProvider_RoseStacker implements StackedBlocksProvider_AutoDetect {

    private static final TransformerImpl STAKCED_BLOCKS_TRANSFORMER = new TransformerImpl();

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

        ChunkPosition chunkPosition = ChunkPosition.of(world, chunkX, chunkZ);
        Map<Key, Counter> stackedBlocks = Maps.newLinkedHashMap();

        RoseStackerAPI.getInstance().getStackedBlocks().forEach((block, stackedBlock) -> {
            if (chunkPosition.isInsideChunk(block.getLocation()))
                stackedBlocks.computeIfAbsent(Key.of(block), k -> new Counter(0)).inc(stackedBlock.getStackSize());
        });

        return Sets.transform(stackedBlocks.entrySet(), STAKCED_BLOCKS_TRANSFORMER);
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

    private static class TransformerImpl extends Transformer<Map.Entry<Key, Counter>, Pair<Key, Integer>> {
        @Override
        public Pair<Key, Integer> transformA(Map.Entry<Key, Counter> entry) {
            return new Pair<>(entry.getKey(), entry.getValue().get());
        }

        @Override
        public Map.Entry<Key, Counter> transformB(Pair<Key, Integer> pair) {
            return new Map.Entry<Key, Counter>() {
                private final Counter value = new Counter(pair.getValue());

                @Override
                public Key getKey() {
                    return pair.getKey();
                }

                @Override
                public Counter getValue() {
                    return value;
                }

                @Override
                public Counter setValue(Counter value) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

}
