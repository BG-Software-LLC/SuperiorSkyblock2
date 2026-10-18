package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.mutable.MutableObject;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.BlockLimitReservations;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddBlockLimit;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminRemoveBlockLimit;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetBlockLimit;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UpgradeTypeBlockLimits implements IUpgradeType {

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeBlockLimits(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<Listener> getListeners() {
        return Collections.singletonList(new BlockLimitsListener());
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return Arrays.asList(new CmdAdminAddBlockLimit(), new CmdAdminRemoveBlockLimit(), new CmdAdminSetBlockLimit());
    }

    private class BlockLimitsListener implements Listener {

        private final Map<Event, List<BlockLimitReservations.Reservation>> reservations = new ConcurrentHashMap<>();

        private boolean admit(Event event, Island island, Key key, Location location) {
            return admit(event, island, key, location, null, 1);
        }

        private boolean admit(Event event, Island island, Key key, Location location, @Nullable Key replacedKey, int amount) {
            if (!BukkitExecutor.isFolia())
                return !island.hasReachedBlockLimit(key);
            List<BlockLimitReservations.Reservation> pending = reservations.get(event);
            if (pending != null && !(event instanceof StructureGrowEvent)) {
                for (BlockLimitReservations.Reservation reservation : pending) {
                    if (reservation.matches(key, location))
                        return true;
                }
            }
            BlockLimitReservations.Reservation reservation = BlockLimitReservations.reserve(island, key, amount, location, replacedKey);
            if (reservation == null)
                return false;
            if (reservation.getAmount() != amount) {
                reservation.close();
                return false;
            }
            if (pending == null) {
                pending = new ArrayList<>();
                reservations.put(event, pending);
                try {
                    plugin.getTaskScheduler().region(location, () -> release(event), 2L, 0L);
                } catch (Throwable error) {
                    reservations.remove(event);
                    reservation.close();
                    ((Cancellable) event).setCancelled(true);
                    throw error;
                }
            }
            pending.add(reservation);
            return true;
        }

        private void release(Event event) {
            List<BlockLimitReservations.Reservation> pending = reservations.remove(event);
            if (pending != null)
                pending.forEach(BlockLimitReservations.Reservation::close);
        }

        private void releaseCancelled(Event event) {
            if (((Cancellable) event).isCancelled())
                release(event);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent e) {
            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getBlockPlaced().getLocation(wrapper.getHandle()));
            }
            if (island == null)
                return;

            Key blockKey = Keys.of(e.getBlock());

            if (!admit(e, island, blockKey, e.getBlockPlaced().getLocation())) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onPlayerRightClickBlock(PlayerInteractEvent e) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;

            PlayerHand playerHand = BukkitItems.getHand(e);
            ItemStack handItem = BukkitItems.getHandItem(e.getPlayer(), playerHand);
            if (handItem == null)
                return;

            Material clickedBlockType = e.getClickedBlock().getType();

            if (onCartPlaceInternal(e, clickedBlockType, handItem) ||
                    onSpawnerChangeInternal(e, clickedBlockType, handItem))
                e.setCancelled(true);
        }

        private boolean onCartPlaceInternal(PlayerInteractEvent e, Material clickedBlockType, ItemStack handItem) {
            if (!Materials.isRail(clickedBlockType))
                return false;

            Material handItemType = handItem.getType();

            if (!Materials.isMinecart(handItemType))
                return false;

            MutableObject<Key> minecraftKey = new MutableObject<>(null);

            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                if (preventMinecartPlace(e, handItemType, e.getClickedBlock().getLocation(wrapper.getHandle()), minecraftKey)) {
                    Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(
                            minecraftKey.getValue().getGlobalKey()));
                    return true;
                }
            }

            return false;
        }

        private boolean onSpawnerChangeInternal(PlayerInteractEvent e, Material clickedBlockType, ItemStack handItem) {
            if (!plugin.getProviders().shouldListenToSpawnerChangesUsingEggs())
                return false;

            if (clickedBlockType != Materials.SPAWNER.toBukkitType())
                return false;

            Material handItemType = handItem.getType();
            if (!Materials.isSpawnEgg(handItemType))
                return false;

            Key oldSpawnerKey = Keys.of(e.getClickedBlock());
            Key newSpawnerKey = Keys.ofSpawner(BukkitItems.getEntityType(e.getItem()));

            if (oldSpawnerKey.equals(newSpawnerKey))
                return false;

            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation(wrapper.getHandle()));
            }

            if (island == null)
                return false;

            if (BukkitExecutor.isFolia()) {
                int spawnerCount = plugin.getProviders().getSpawnersProvider().getSpawner(e.getClickedBlock().getLocation()).getKey();
                if (!admit(e, island, newSpawnerKey, e.getClickedBlock().getLocation(), oldSpawnerKey, spawnerCount)) {
                    Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(newSpawnerKey.toString()));
                    return true;
                }
                return false;
            }

            try {
                island.handleBlockBreak(oldSpawnerKey, 1, 0);
                if (island.hasReachedBlockLimit(newSpawnerKey)) {
                    Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(newSpawnerKey.toString()));
                    return true;
                }
            } finally {
                island.handleBlockPlace(oldSpawnerKey, 1, 0);
            }

            return false;
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        private void onMinecartPlaceByDispenser(BlockDispenseEvent e) {
            Material dispenseItemType = e.getItem().getType();

            if (!Materials.isMinecart(dispenseItemType) || e.getBlock().getType() != Material.DISPENSER)
                return;

            Block targetBlock = null;

            if (ServerVersion.isLegacy()) {
                MaterialData materialData = e.getBlock().getState().getData();
                if (materialData instanceof Directional) {
                    targetBlock = e.getBlock().getRelative(((Directional) materialData).getFacing());
                }
            } else {
                Object blockData = plugin.getNMSWorld().getBlockData(e.getBlock());
                if (blockData instanceof org.bukkit.block.data.Directional) {
                    targetBlock = e.getBlock().getRelative(((org.bukkit.block.data.Directional) blockData).getFacing());
                }
            }

            if (targetBlock == null)
                return;

            if (!Materials.isRail(targetBlock.getType()))
                return;

            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                if (preventMinecartPlace(e, dispenseItemType, targetBlock.getLocation(wrapper.getHandle()), null))
                    e.setCancelled(true);
            }

        }

        private boolean preventMinecartPlace(Event event, Material minecartType, Location location, @Nullable MutableObject<Key> minecraftKey) {
            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null)
                return false;

            Key key = null;

            switch (minecartType.name()) {
                case "HOPPER_MINECART":
                    key = ConstantKeys.HOPPER;
                    break;
                case "COMMAND_MINECART":
                case "COMMAND_BLOCK_MINECART":
                    key = ConstantKeys.COMMAND_BLOCK;
                    break;
                case "EXPLOSIVE_MINECART":
                case "TNT_MINECART":
                    key = ConstantKeys.TNT;
                    break;
                case "POWERED_MINECART":
                case "FURNACE_MINECART":
                    key = ConstantKeys.FURNACE;
                    break;
                case "STORAGE_MINECART":
                case "CHEST_MINECART":
                    key = ConstantKeys.CHEST;
                    break;
            }

            if (key != null && !admit(event, island, key, location)) {
                if (minecraftKey != null)
                    minecraftKey.setValue(key);
                return true;
            }

            return false;
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBucketEmpty(PlayerBucketEmptyEvent e) {
            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation(wrapper.getHandle()));
            }
            if (island == null)
                return;

            Key blockKey = Keys.ofMaterialAndData(e.getBucket().name().replace("_BUCKET", ""));

            if (!admit(e, island, blockKey, e.getBlockClicked().getLocation())) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBlockGrow(BlockGrowEvent e) {
            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getBlock().getLocation(wrapper.getHandle()));
            }

            if (island == null)
                return;

            Key blockKey = Keys.of(e.getNewState());

            if (!admit(e, island, blockKey, e.getBlock().getLocation()))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onStructureGrow(StructureGrowEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getLocation());

            if (island == null)
                return;

            e.getBlocks().removeIf(blockState -> !admit(e, island, Keys.of(blockState), e.getLocation()));
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBlockForm(BlockFormEvent e) {
            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getBlock().getLocation(wrapper.getHandle()));
            }
            if (island == null)
                return;

            Key blockKey = Keys.of(e.getNewState());

            if (!admit(e, island, blockKey, e.getBlock().getLocation())) {
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onFinished(BlockPlaceEvent event) {
            releaseCancelled(event);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onFinished(PlayerBucketEmptyEvent event) {
            releaseCancelled(event);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onFinished(BlockGrowEvent event) {
            releaseCancelled(event);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onFinished(BlockFormEvent event) {
            releaseCancelled(event);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onFinished(StructureGrowEvent event) {
            releaseCancelled(event);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onFinished(BlockDispenseEvent event) {
            releaseCancelled(event);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onFinished(PlayerInteractEvent event) {
            releaseCancelled(event);
        }

    }

}
