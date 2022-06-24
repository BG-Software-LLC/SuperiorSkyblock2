package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collections;
import java.util.List;

public class UpgradeTypeBlockLimits implements IUpgradeType {

    private static final ReflectMethod<EquipmentSlot> INTERACT_GET_HAND = new ReflectMethod<>(
            PlayerInteractEvent.class, "getHand");

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeBlockLimits(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Listener getListener() {
        return new BlockLimitsListener();
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return Collections.emptyList();
    }

    private class BlockLimitsListener implements Listener {

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBlockPlaced().getLocation());

            if (island == null)
                return;

            Key blockKey = KeyImpl.of(e.getBlock());

            if (island.hasReachedBlockLimit(blockKey)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onCartPlace(PlayerInteractEvent e) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null ||
                    !Materials.isRail(e.getClickedBlock().getType()) ||
                    !Materials.isMinecart(e.getItem().getType()))
                return;

            if (INTERACT_GET_HAND.isValid() && INTERACT_GET_HAND.invoke(e) != EquipmentSlot.HAND)
                return;

            Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

            if (island == null)
                return;

            Key key = null;

            switch (e.getItem().getType().name()) {
                case "HOPPER_MINECART":
                    key = ConstantKeys.HOPPER;
                    break;
                case "COMMAND_MINECART":
                case "COMMAND_BLOCK_MINECART":
                    key = ServerVersion.isAtLeast(ServerVersion.v1_13) ? ConstantKeys.COMMAND_BLOCK : ConstantKeys.COMMAND;
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

            if (key != null && island.hasReachedBlockLimit(key)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(key.getGlobalKey()));
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBucketEmpty(PlayerBucketEmptyEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

            if (island == null)
                return;

            Key blockKey = KeyImpl.of(e.getBucket().name().replace("_BUCKET", ""));

            if (island.hasReachedBlockLimit(blockKey)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBlockGrow(BlockGrowEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

            if (island == null)
                return;

            Key blockKey = KeyImpl.of(e.getNewState());

            if (island.hasReachedBlockLimit(blockKey))
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onStructureGrow(StructureGrowEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getLocation());

            if (island == null)
                return;

            e.getBlocks().removeIf(blockState -> island.hasReachedBlockLimit(KeyImpl.of(blockState)));
        }

    }

}
