package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.StackedBlocksInteractionService;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.StackedBlocksDepositMenu;
import com.bgsoftware.superiorskyblock.service.stackedblocks.StackedBlocksServiceHelper;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StackedBlocksListener implements Listener {

    @Nullable
    private static final Material COPPER_BLOCK = EnumHelper.getEnum(Material.class, "COPPER_BLOCK");
    private static final Material HONEYCOMB = EnumHelper.getEnum(Material.class, "HONEYCOMB");
    private final Map<CreatureSpawnEvent.SpawnReason, List<BlockOffset>> ENTITY_TEMPLATE_OFFSETS = buildEntityTemplateOffsetsMap();

    private final SuperiorSkyblockPlugin plugin;
    private final LazyReference<StackedBlocksInteractionService> stackedBlocksInteractionService = new LazyReference<StackedBlocksInteractionService>() {
        @Override
        protected StackedBlocksInteractionService create() {
            return plugin.getServices().getService(StackedBlocksInteractionService.class);
        }
    };

    public StackedBlocksListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.registerPhysicsListener();
        this.registerSpongeListener();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(BlockPlaceEvent e) {
        if (e.getBlockAgainst().equals(e.getBlock()))
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
            plugin.getStackedBlocks().setStackedBlock(e.getBlock(), 1);

        // We do not stack blocks when the hand items has a name or a lore.
        ItemStack inHand = e.getItemInHand();
        if (inHand.hasItemMeta() && (inHand.getItemMeta().hasDisplayName() || inHand.getItemMeta().hasLore()))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        PlayerHand usedHand = BukkitItems.getHand(e);

        InteractionResult interactionResult = this.stackedBlocksInteractionService.get().handleStackedBlockPlace(
                superiorPlayer, e.getBlockAgainst(), usedHand.getEquipmentSlot());
        if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block clickedBlock = e.getClickedBlock();
        Material clickedBlockType = clickedBlock.getType();

        ItemStack inHand = e.getItem();

        if (clickedBlockType == Material.DRAGON_EGG) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

            if (plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) > 1) {
                e.setCancelled(true);
                if (inHand == null)
                    this.stackedBlocksInteractionService.get().handleStackedBlockBreak(clickedBlock, superiorPlayer);
            }

            if (inHand != null) {
                PlayerHand usedHand = BukkitItems.getHand(e);
                InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                        .handleStackedBlockPlace(superiorPlayer, clickedBlock, usedHand.getEquipmentSlot());
                if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult))
                    e.setCancelled(true);
            }
        } else if (clickedBlockType == COPPER_BLOCK && inHand != null && inHand.getType() == HONEYCOMB &&
                plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) > 1) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(BlockBreakEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                .handleStackedBlockBreak(e.getBlock(), superiorPlayer);
        if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() != null ||
                BukkitItems.getHand(e) != PlayerHand.MAIN_HAND)
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getClickedBlock()) <= 1)
            return;

        if (plugin.getSettings().getStackedBlocks().getDepositMenu().isEnabled() && e.getPlayer().isSneaking()) {
            StackedBlocksDepositMenu depositMenu = new StackedBlocksDepositMenu(e.getClickedBlock().getLocation());
            e.getPlayer().openInventory(depositMenu.getInventory());
        } else {
            ItemStack offHandItem = BukkitItems.getHandItem(e.getPlayer(), PlayerHand.OFF_HAND);
            if (offHandItem != null && offHandItem.getType() == e.getClickedBlock().getType())
                return;

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                    .handleStackedBlockBreak(e.getClickedBlock(), superiorPlayer);
            if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(EntityChangeBlockEvent e) {
        InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                .handleStackedBlockBreak(e.getBlock(), null);
        if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        List<Block> blockList = new LinkedList<>(e.blockList());
        ItemStack blockItem;

        for (Block block : blockList) {
            // Check if block is stackable
            if (!plugin.getSettings().getStackedBlocks().getWhitelisted().contains(Keys.of(block)))
                continue;

            int amount = plugin.getStackedBlocks().getStackedBlockAmount(block);

            if (amount <= 1)
                continue;

            // All checks are done. We can remove the block from the list.
            e.blockList().remove(block);

            blockItem = block.getState().getData().toItemStack(amount);

            Location location = block.getLocation();

            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null)
                island.handleBlockBreak(block, amount);

            plugin.getStackedBlocks().removeStackedBlock(location);
            block.setType(Material.AIR);

            // Dropping the item
            block.getWorld().dropItemNaturally(location, blockItem);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(block) > 1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks()) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(block) > 1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockChangeState(BlockFormEvent e) {
        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGolemCreate(CreatureSpawnEvent e) {
        List<BlockOffset> entityTemplateOffsets = ENTITY_TEMPLATE_OFFSETS.get(e.getSpawnReason());

        if (entityTemplateOffsets == null)
            return;

        Location entityLocation = e.getEntity().getLocation();

        if (plugin.getStackedBlocks().getStackedBlockAmount(entityLocation) > 1) {
            e.setCancelled(true);
            return;
        }

        for (BlockOffset blockOffset : entityTemplateOffsets) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(blockOffset.applyToLocation(entityLocation)) > 1) {
                e.setCancelled(true);
                return;
            }
        }
    }

    private void registerPhysicsListener() {
        if (plugin.getSettings().isPhysicsListener())
            Bukkit.getPluginManager().registerEvents(new PhysicsListener(), plugin);
    }

    private void registerSpongeListener() {
        try {
            Class.forName("org.bukkit.event.block.SpongeAbsorbEvent");
            Bukkit.getPluginManager().registerEvents(new SpongeAbsorbListener(), plugin);
        } catch (Throwable ignored) {
        }
    }

    private static Map<CreatureSpawnEvent.SpawnReason, List<BlockOffset>> buildEntityTemplateOffsetsMap() {
        EnumMap<CreatureSpawnEvent.SpawnReason, List<BlockOffset>> offsetsMap = new EnumMap<>(CreatureSpawnEvent.SpawnReason.class);

        offsetsMap.put(CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM, Arrays.asList(
                SBlockOffset.fromOffsets(0, 1, 0),
                SBlockOffset.fromOffsets(1, 1, 0),
                SBlockOffset.fromOffsets(1, 1, 1),
                SBlockOffset.fromOffsets(-1, 1, 0),
                SBlockOffset.fromOffsets(-1, 1, -1),
                SBlockOffset.fromOffsets(0, 2, 0)
        ));

        offsetsMap.put(CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN, Arrays.asList(
                SBlockOffset.fromOffsets(0, 1, 0),
                SBlockOffset.fromOffsets(0, 2, 0)
        ));

        offsetsMap.put(CreatureSpawnEvent.SpawnReason.BUILD_WITHER, Arrays.asList(
                SBlockOffset.fromOffsets(0, 1, 0),
                SBlockOffset.fromOffsets(1, 1, 0),
                SBlockOffset.fromOffsets(1, 1, 1),
                SBlockOffset.fromOffsets(-1, 1, 0),
                SBlockOffset.fromOffsets(-1, 1, -1),
                SBlockOffset.fromOffsets(0, 2, 0),
                SBlockOffset.fromOffsets(1, 2, 0),
                SBlockOffset.fromOffsets(1, 2, 1),
                SBlockOffset.fromOffsets(-1, 2, 0),
                SBlockOffset.fromOffsets(-1, 2, -1)
        ));

        return Collections.unmodifiableMap(offsetsMap);
    }

    private class PhysicsListener implements Listener {

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onStackedBlockPhysics(BlockPhysicsEvent e) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
                e.setCancelled(true);
        }

    }

    private class SpongeAbsorbListener implements Listener {

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onSpongeAbsorb(org.bukkit.event.block.SpongeAbsorbEvent e) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
                e.setCancelled(true);
        }

    }

}
