package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IStackedBlocksListener;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.StackedBlocksDepositMenu;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class StackedBlocksListener implements Listener {

    private static final ReflectMethod<EquipmentSlot> INTERACT_GET_HAND = new ReflectMethod<>(
            PlayerInteractEvent.class, "getHand");
    @Nullable
    private static final Material COPPER_BLOCK = Materials.getMaterialSafe("COPPER_BLOCK");
    private static final Material HONEYCOMB = Materials.getMaterialSafe("HONEYCOMB");
    private final Map<CreatureSpawnEvent.SpawnReason, List<BlockOffset>> ENTITY_TEMPLATE_OFFSETS = buildEntityTemplateOffsetsMap();
    @Nullable
    private static final Material CAULDRON_ITEM = Materials.getMaterialSafe("CAULDRON_ITEM");
    @SuppressWarnings("unchecked")
    private static final Map<Material, Material> AGAINST_BLOCK_CHANGE_MATERIAL = buildImmutableMap(
            new Pair<>(Materials.getMaterialSafe("GLOWING_REDSTONE_ORE"), Material.REDSTONE_ORE)
    );


    private final SuperiorSkyblockPlugin plugin;
    private final Singleton<ProtectionListener> protectionListener;

    public StackedBlocksListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.protectionListener = plugin.getListener(ProtectionListener.class);
        this.registerPhysicsListener();
        this.registerSpongeListener();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(PlayerInteractEvent e) {
        Block clickedBlock = e.getClickedBlock();

        if (clickedBlock == null)
            return;

        ItemStack inHand = e.getItem();

        Material clickedBlockType = clickedBlock.getType();

        if (clickedBlockType == Material.DRAGON_EGG) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) > 1) {
                e.setCancelled(true);
                if (inHand == null)
                    tryUnstack(e.getPlayer(), clickedBlock);
            }

            if (inHand != null && canStackBlocks(e.getPlayer(), inHand, clickedBlock) &&
                    tryStack(e.getPlayer(), inHand, clickedBlock.getLocation(), e)) {
                e.setCancelled(true);
            }
        } else if (clickedBlockType == COPPER_BLOCK && inHand != null && inHand.getType() == HONEYCOMB &&
                plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) > 1) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(BlockPlaceEvent e) {
        if (e.getBlockAgainst().equals(e.getBlock()))
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
            plugin.getStackedBlocks().setStackedBlock(e.getBlock(), 1);

        if (!canStackBlocks(e.getPlayer(), e.getItemInHand(), e.getBlockAgainst()))
            return;

        if (tryStack(e.getPlayer(), e.getItemInHand(), e.getBlockAgainst().getLocation(), e))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(BlockBreakEvent e) {
        if (tryUnstack(e.getPlayer(), e.getBlock()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() != null)
            return;

        if (INTERACT_GET_HAND.isValid() && INTERACT_GET_HAND.invoke(e) != EquipmentSlot.HAND)
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getClickedBlock()) <= 1)
            return;

        if (plugin.getSettings().getStackedBlocks().getDepositMenu().isEnabled() && e.getPlayer().isSneaking()) {
            StackedBlocksDepositMenu depositMenu = new StackedBlocksDepositMenu(e.getClickedBlock().getLocation());
            e.getPlayer().openInventory(depositMenu.getInventory());
        } else if (protectionListener.get().preventBlockBreak(e.getClickedBlock(), e.getPlayer(), ProtectionListener.Flag.SEND_MESSAGES) ||
                tryUnstack(e.getPlayer(), e.getClickedBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(EntityChangeBlockEvent e) {
        if (tryUnstack(null, e.getBlock()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        List<Block> blockList = new LinkedList<>(e.blockList());
        ItemStack blockItem;

        for (Block block : blockList) {
            // Check if block is stackable
            if (!plugin.getSettings().getStackedBlocks().getWhitelisted().contains(KeyImpl.of(block)))
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

    public boolean canStackBlocks(Player player, ItemStack placeItem, Block againstBlock) {
        if (!plugin.getSettings().getStackedBlocks().isEnabled())
            return false;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (!superiorPlayer.hasBlocksStackerEnabled())
            return false;

        if (plugin.getSettings().getStackedBlocks().getDisabledWorlds().contains(againstBlock.getWorld().getName()))
            return false;

        if (placeItem.hasItemMeta() && (placeItem.getItemMeta().hasDisplayName() || placeItem.getItemMeta().hasLore()))
            return false;

        Material newAgainstBlockType = AGAINST_BLOCK_CHANGE_MATERIAL.get(againstBlock.getType());
        if (newAgainstBlockType != null)
            againstBlock.setType(newAgainstBlockType);

        KeySet whitelist = (KeySet) plugin.getSettings().getStackedBlocks().getWhitelisted();

        Key againstBlockKey = whitelist.getKey(KeyImpl.of(againstBlock));

        if (!whitelist.contains(againstBlockKey))
            return false;

        Key placeItemBlockKey = whitelist.getKey(KeyImpl.of(placeItem));

        if (!Objects.equals(againstBlockKey, placeItemBlockKey))
            return false;

        return superiorPlayer.hasPermission("superior.island.stacker.*") ||
                superiorPlayer.hasPermission("superior.island.stacker." + placeItem.getType());
    }

    public boolean tryStack(Player player, ItemStack itemToDeposit, Location stackedBlock, Event event) {
        return tryStack(player, !player.isSneaking() ? 1 : itemToDeposit.getAmount(), stackedBlock, depositedAmount -> {
            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack inHand = itemToDeposit.clone();
                inHand.setAmount(depositedAmount);
                BukkitItems.removeItem(inHand, event, player);
            }
        });
    }

    public boolean tryStack(Player player, int amount, Location stackedBlock, Consumer<Integer> depositedAmount) {
        // When sneaking, you'll stack all the items in your hand. Otherwise, you'll stack only 1 block
        int blockAmount = plugin.getStackedBlocks().getStackedBlockAmount(stackedBlock);
        Key blockKey = plugin.getStackedBlocks().getStackedBlockKey(stackedBlock);

        if (blockKey == null)
            blockKey = KeyImpl.of(stackedBlock.getBlock());

        int blockLimit = plugin.getSettings().getStackedBlocks().getLimits().getOrDefault(blockKey, Integer.MAX_VALUE);

        if (amount + blockAmount > blockLimit) {
            amount = blockLimit - blockAmount;
        }

        if (amount <= 0) {
            depositedAmount.accept(0);
            return false;
        }

        Block block = stackedBlock.getBlock();

        if (!plugin.getEventsBus().callBlockStackEvent(block, player, blockAmount, blockAmount + amount)) {
            depositedAmount.accept(0);
            return false;
        }

        Island island = plugin.getGrid().getIslandAt(stackedBlock);

        if (island != null) {
            BigInteger islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(blockKey));
            BigInteger islandBlockCount = island.getBlockCountAsBigInteger(blockKey);
            BigInteger bigAmount = BigInteger.valueOf(amount);

            //Checking for the specific provided key.
            if (islandBlockLimit.compareTo(BigInteger.ZERO) >= 0 &&
                    islandBlockCount.add(bigAmount).compareTo(islandBlockLimit) > 0) {
                amount = islandBlockLimit.subtract(islandBlockCount).intValue();
            } else {
                //Getting the global key values.
                Key globalKey = KeyImpl.of(blockKey.getGlobalKey());
                islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(globalKey));
                islandBlockCount = island.getBlockCountAsBigInteger(globalKey);
                if (islandBlockLimit.compareTo(BigInteger.ZERO) >= 0 &&
                        islandBlockCount.add(bigAmount).compareTo(islandBlockLimit) > 0) {
                    amount = islandBlockLimit.subtract(islandBlockCount).intValue();
                }
            }
        }

        if (!plugin.getStackedBlocks().setStackedBlock(block, blockAmount + amount)) {
            depositedAmount.accept(0);
            return false;
        }

        if (island != null) {
            island.handleBlockPlace(block, amount);
        }

        plugin.getProviders().notifyStackedBlocksListeners(player, block, IStackedBlocksListener.Action.BLOCK_PLACE);

        depositedAmount.accept(amount);

        return true;
    }

    public boolean tryUnstack(@Nullable Player player, Block block) {
        int blockAmount = plugin.getStackedBlocks().getStackedBlockAmount(block);

        if (blockAmount <= 1)
            return false;

        // When sneaking, you'll break 64 from the stack. Otherwise, 1.
        int amount = player == null || !player.isSneaking() ? 1 : 64;

        // Fix amount so it won't be more than the stack's amount
        amount = Math.min(amount, blockAmount);

        if (!plugin.getEventsBus().callBlockUnstackEvent(block, player, blockAmount, blockAmount - amount))
            return false;

        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        int leftAmount;
        boolean stackedBlockSuccess = plugin.getStackedBlocks().setStackedBlock(block, (leftAmount = blockAmount - amount));

        plugin.getNMSWorld().playBreakAnimation(block);

        plugin.getProviders().notifyStackedBlocksListeners(player, block, IStackedBlocksListener.Action.BLOCK_BREAK);

        if (!stackedBlockSuccess) {
            if (island != null)
                island.handleBlockBreak(KeyImpl.of(block), blockAmount - 1);
            leftAmount = 0;
            amount = 1;
        }

        ItemStack blockItem = ServerVersion.isLegacy() ? block.getState().getData().toItemStack(amount) :
                new ItemStack(block.getType(), amount);

        Material newAgainstBlockType = AGAINST_BLOCK_CHANGE_MATERIAL.get(blockItem.getType());
        if (newAgainstBlockType != null)
            blockItem.setType(newAgainstBlockType);

        if (CAULDRON_ITEM != null && CAULDRON_ITEM == blockItem.getType()) {
            blockItem.setType(CAULDRON_ITEM);
        }

        if (island != null) {
            island.handleBlockBreak(KeyImpl.of(blockItem), amount);
        }

        // If the amount of the stack is less than 0, it should be air.
        if (leftAmount <= 0) {
            block.setType(Material.AIR);
        }

        // Dropping the item
        if (player != null && plugin.getSettings().getStackedBlocks().isAutoCollect()) {
            BukkitItems.addItem(blockItem, player.getInventory(), block.getLocation());
        } else {
            block.getWorld().dropItemNaturally(block.getLocation(), blockItem);
        }

        return true;
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

    private static Map<Material, Material> buildImmutableMap(Pair<Material, Material>... materials) {
        ImmutableMap.Builder<Material, Material> builder = new ImmutableMap.Builder<>();
        for (Pair<Material, Material> material : materials) {
            if (material.getKey() != null && material.getValue() != null)
                builder.put(material.getKey(), material.getValue());
        }
        return builder.build();
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
