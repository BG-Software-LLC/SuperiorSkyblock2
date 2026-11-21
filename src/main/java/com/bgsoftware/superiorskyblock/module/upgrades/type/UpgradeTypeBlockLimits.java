package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddBlockCategoryLimit;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddBlockLimit;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminRemoveBlockCategoryLimit;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminRemoveBlockLimit;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetBlockCategoryLimit;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetBlockLimit;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        return Arrays.asList(new CmdAdminAddBlockCategoryLimit(), new CmdAdminAddBlockLimit(),
                new CmdAdminRemoveBlockCategoryLimit(), new CmdAdminRemoveBlockLimit(),
                new CmdAdminSetBlockCategoryLimit(), new CmdAdminSetBlockLimit());
    }

    private boolean hasReachedLimit(Island island, Key key, @Nullable Player player) {
        if (island.hasReachedBlockLimit(key)) {
            if (player != null) {
                Message.REACHED_BLOCK_LIMIT.send(player, Formatters.CAPITALIZED_FORMATTER.format(key.toString()));
            }

            return true;
        }

        for (KeyCategory blockCategory : plugin.getSettings().getBlockCategoriesMapping().getCategories(key)) {
            if (island.hasReachedBlockCategoryLimit(blockCategory)) {
                if (player != null) {
                    Message.REACHED_BLOCK_CATEGORY_LIMIT.send(player, Formatters.CAPITALIZED_FORMATTER.format(blockCategory.getName()));
                }

                return true;
            }
        }

        return false;
    }

    private class BlockLimitsListener implements Listener {

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent e) {
            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getBlockPlaced().getLocation(wrapper.getHandle()));
            }
            if (island == null)
                return;

            Key blockKey = Keys.of(e.getBlock());

            if (hasReachedLimit(island, blockKey, e.getPlayer())) {
                e.setCancelled(true);
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

            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                if (preventMinecartPlace(handItemType, e.getClickedBlock().getLocation(wrapper.getHandle()), e.getPlayer())) {
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

            try {
                island.handleBlockBreak(oldSpawnerKey, 1, 0);
                if (hasReachedLimit(island, newSpawnerKey, e.getPlayer())) {
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
                if (preventMinecartPlace(dispenseItemType, targetBlock.getLocation(wrapper.getHandle()), null))
                    e.setCancelled(true);
            }

        }

        private boolean preventMinecartPlace(Material minecartType, Location location, @Nullable Player player) {
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

            return key != null && hasReachedLimit(island, key, player);
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

            if (hasReachedLimit(island, blockKey, e.getPlayer())) {
                e.setCancelled(true);
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

            if (hasReachedLimit(island, blockKey, null))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onStructureGrow(StructureGrowEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getLocation());

            if (island == null)
                return;

            e.getBlocks().removeIf(blockState -> hasReachedLimit(island, Keys.of(blockState), null));
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

            if (hasReachedLimit(island, blockKey, null)) {
                e.setCancelled(true);
            }
        }


    }

}
