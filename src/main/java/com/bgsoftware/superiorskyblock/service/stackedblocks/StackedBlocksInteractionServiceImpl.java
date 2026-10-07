package com.bgsoftware.superiorskyblock.service.stackedblocks;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IStackedBlocksListener;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.StackedBlocksInteractionService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.BlockLimitReservations;
import com.bgsoftware.superiorskyblock.player.inventory.FoliaInventoryReturns;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.service.region.ProtectionHelper;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import com.google.common.base.Preconditions;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

public class StackedBlocksInteractionServiceImpl implements StackedBlocksInteractionService, IService {

    private static final KeyMap<Key> BLOCK_KEY_TRANSFORMER = createBlockKeyTransformer();

    private final LazyReference<RegionManagerService> regionManagerService = new LazyReference<RegionManagerService>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };
    private final SuperiorSkyblockPlugin plugin;

    public StackedBlocksInteractionServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return StackedBlocksInteractionService.class;
    }

    @Override
    public InteractionResult handleStackedBlockPlace(SuperiorPlayer superiorPlayer, Block block, EquipmentSlot usedHand) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");
        Preconditions.checkNotNull(usedHand, "usedHand cannot be null");

        if (!plugin.getSettings().getStackedBlocks().isEnabled())
            return InteractionResult.STACKED_BLOCKS_DISABLED;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return InteractionResult.DISABLED_WORLD;

        Player onlinePlayer = superiorPlayer.asPlayer();
        ItemStack handItem = onlinePlayer == null ? null : BukkitItems.getHandItem(onlinePlayer, PlayerHand.of(usedHand));

        InteractionResult interactionResult = checkBlockStackInternal(superiorPlayer, block, handItem);
        if (interactionResult != InteractionResult.SUCCESS)
            return interactionResult;

        int amountToDeposit = onlinePlayer == null ? 1 : handItem == null ? 1 : onlinePlayer.isSneaking() ? handItem.getAmount() : 1;

        return handleBlockStackInternal(superiorPlayer, block, amountToDeposit, Either.left(usedHand));
    }

    @Override
    public InteractionResult handleStackedBlockPlace(SuperiorPlayer superiorPlayer, Block block,
                                                     int amountToDeposit, OnItemRemovalCallback itemRemovalCallback) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");
        Preconditions.checkNotNull(itemRemovalCallback, "itemRemovalCallback cannot be null");

        if (!plugin.getSettings().getStackedBlocks().isEnabled())
            return InteractionResult.STACKED_BLOCKS_DISABLED;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return InteractionResult.DISABLED_WORLD;

        InteractionResult interactionResult = checkBlockStackInternal(superiorPlayer, block, null);
        if (interactionResult != InteractionResult.SUCCESS)
            return interactionResult;

        return handleBlockStackInternal(superiorPlayer, block, amountToDeposit, Either.right(itemRemovalCallback));
    }

    @Override
    public InteractionResult checkStackedBlockInteraction(SuperiorPlayer superiorPlayer, Block block, ItemStack itemStack) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null");
        Preconditions.checkNotNull(block, "block parameter cannot be null");
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null");

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return InteractionResult.DISABLED_WORLD;

        return checkBlockStackInternal(superiorPlayer, block, itemStack);
    }

    @Override
    public InteractionResult handleStackedBlockBreak(Block block, @Nullable SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(block, "block cannot be null");

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return InteractionResult.DISABLED_WORLD;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            int blockAmount = plugin.getStackedBlocks().getStackedBlockAmount(blockLocation);
            if (blockAmount <= 1)
                return InteractionResult.NOT_STACKED_BLOCK;

            Island island = plugin.getGrid().getIslandAt(blockLocation);
            if (superiorPlayer != null && island != null) {
                com.bgsoftware.superiorskyblock.api.service.region.InteractionResult interactionResult =
                        this.regionManagerService.get().handleBlockBreak(superiorPlayer, block);
                if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
                    return InteractionResult.STACKED_BLOCK_PROTECTED;
            }

            Player onlinePlayer = superiorPlayer == null ? null : superiorPlayer.asPlayer();

            int amountToBreak = Math.min(blockAmount, onlinePlayer != null && onlinePlayer.isSneaking() ? 64 : 1);

            int leftAmount = blockAmount - amountToBreak;

            if (!PluginEventsFactory.callBlockUnstackEvent(block, onlinePlayer, blockAmount, leftAmount))
                return InteractionResult.EVENT_CANCELLED;

            if (!plugin.getStackedBlocks().setStackedBlock(block, leftAmount))
                return InteractionResult.GLITCHED_STACKED_BLOCK;

            plugin.getNMSWorld().playBreakAnimation(block);

            if (superiorPlayer != null) {
                OfflinePlayer offlinePlayer = onlinePlayer == null ? superiorPlayer.asOfflinePlayer() : onlinePlayer;
                plugin.getProviders().notifyStackedBlocksListeners(offlinePlayer, block, IStackedBlocksListener.Action.BLOCK_BREAK);
            }

            if (island != null)
                island.handleBlockBreak(block, amountToBreak);

            ItemStack blockItem = ServerVersion.isLegacy() ?
                    block.getState().getData().toItemStack(amountToBreak) :
                    new ItemStack(block.getType(), amountToBreak);

            if (leftAmount <= 0)
                block.setType(Material.AIR);

            // Dropping the item
            if (onlinePlayer != null && plugin.getSettings().getStackedBlocks().isAutoCollect()) {
                BukkitItems.addItem(blockItem, onlinePlayer.getInventory(), blockLocation);
            } else {
                block.getWorld().dropItemNaturally(blockLocation.add(0, 0.5, 0), blockItem);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private InteractionResult checkBlockStackInternal(SuperiorPlayer superiorPlayer, Block block, @Nullable ItemStack itemStack) {
        return checkStackedBlockInteraction(plugin, superiorPlayer, Keys.of(block), block.getType(),
                block.getWorld().getName(), itemStack, null);
    }

    public static InteractionResult checkStackedBlockInteraction(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer,
                                                                  Key blockKey, Material blockType, String worldName,
                                                                  @Nullable ItemStack itemStack, @Nullable Boolean permission) {
        if (!superiorPlayer.hasBlocksStackerEnabled())
            return InteractionResult.PLAYER_STACKED_BLOCKS_DISABLED;

        if (plugin.getSettings().getStackedBlocks().getDisabledWorlds().contains(worldName))
            return InteractionResult.DISABLED_WORLD;

        if (itemStack != null) {
            if (itemStack.hasItemMeta() && (itemStack.getItemMeta().hasDisplayName() || itemStack.getItemMeta().hasLore()))
                return InteractionResult.CUSTOMIZED_ITEM;

            Key itemKey = Keys.of(itemStack);
            if (!itemKey.equals(blockKey))
                return InteractionResult.PLAYER_HOLDING_DIFFERENT_ITEM;
        }

        boolean hasPermission = permission != null ? permission :
                superiorPlayer.hasPermission("superior.island.stacker.*") ||
                        superiorPlayer.hasPermission("superior.island.stacker." + blockType);
        if (!hasPermission)
            return InteractionResult.PLAYER_MISSING_PERMISSION;

        Key newBlockKey = BLOCK_KEY_TRANSFORMER.getOrDefault(blockKey, blockKey);

        KeySet whitelist = (KeySet) plugin.getSettings().getStackedBlocks().getWhitelisted();

        if (!whitelist.contains(newBlockKey))
            return InteractionResult.STACKED_BLOCK_NOT_WHITELISTED;

        return InteractionResult.SUCCESS;
    }

    public void depositReservedItems(SuperiorPlayer superiorPlayer, Location location, Key expectedBlock,
                                     ItemStack[] contents, boolean permission, FoliaInventoryReturns.Reservation reservation) {
        BukkitExecutor.submit(location, () -> {
            if (!plugin.getSettings().getStackedBlocks().isEnabled() || !plugin.getGrid().isIslandsWorld(location.getWorld()))
                return null;
            Block block = location.getBlock();
            Key blockKey = Keys.of(block);
            if (!blockKey.equals(expectedBlock))
                return null;
            boolean[] eligible = new boolean[contents.length];
            int amount = 0;
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack item = contents[slot];
                if (item != null && item.getType() != Material.AIR &&
                        checkStackedBlockInteraction(plugin, superiorPlayer, blockKey, block.getType(),
                                location.getWorld().getName(), item, permission) == InteractionResult.SUCCESS) {
                    eligible[slot] = true;
                    amount += item.getAmount();
                }
            }
            if (amount > 0) {
                InteractionResult result = handleBlockStackInternal(superiorPlayer, block, location, amount,
                        Either.right(deposited -> reservation.consume(deposited, eligible)), true);
                if (result == InteractionResult.SUCCESS)
                    plugin.getNMSWorld().playPlaceSound(location);
            }
            return null;
        }).whenComplete((ignored, error) -> {
            if (error != null)
                Log.error(error, "Unable to deposit reserved stacked blocks");
            reservation.finish();
        });
    }

    private InteractionResult handleBlockStackInternal(SuperiorPlayer superiorPlayer, Block stackedBlock,
                                                       int amountToDeposit,
                                                       Either<EquipmentSlot, OnItemRemovalCallback> removalData) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            return handleBlockStackInternal(superiorPlayer, stackedBlock, stackedBlock.getLocation(wrapper.getHandle()),
                    amountToDeposit, removalData);
        }
    }

    private InteractionResult handleBlockStackInternal(SuperiorPlayer superiorPlayer, Block stackedBlock,
                                                       Location stackedBlockLocation, int amountToDeposit,
                                                       Either<EquipmentSlot, OnItemRemovalCallback> removalData) {
        return handleBlockStackInternal(superiorPlayer, stackedBlock, stackedBlockLocation, amountToDeposit, removalData, false);
    }

    private InteractionResult handleBlockStackInternal(SuperiorPlayer superiorPlayer, Block stackedBlock,
                                                       Location stackedBlockLocation, int amountToDeposit,
                                                       Either<EquipmentSlot, OnItemRemovalCallback> removalData,
                                                       boolean commitBeforeNotifications) {
        Player onlinePlayer = superiorPlayer.asPlayer();
        boolean consumeBeforeNotifications = commitBeforeNotifications || BukkitExecutor.isFolia();

        int blockAmount = plugin.getStackedBlocks().getStackedBlockAmount(stackedBlockLocation);
        Key blockKey = plugin.getStackedBlocks().getStackedBlockKey(stackedBlockLocation);
        if (blockKey == null)
            blockKey = Keys.of(stackedBlock);

        int blockLimit = plugin.getSettings().getStackedBlocks().getLimits().getOrDefault(blockKey, Integer.MAX_VALUE);
        // We make sure the amountToDeposit does not exceed the block limit
        if (blockAmount + amountToDeposit > blockLimit)
            amountToDeposit = blockLimit - blockAmount;

        if (amountToDeposit <= 0) {
            // Inform GUI/caller that nothing was deposited so it can refund its previewed removal
            removalData.ifRight(cb -> cb.accept(0));
            return InteractionResult.NOT_ENOUGH_BLOCKS;
        }

        Island island = plugin.getGrid().getIslandAt(stackedBlockLocation);
        if (island != null) {
            // We ensure we do not exceed island's block limit
            BigInteger amountToDepositBig = BigInteger.valueOf(amountToDeposit);

            // Checking for the specific block key
            BigInteger islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(blockKey));
            BigInteger islandBlockCount = island.getBlockCountAsBigInteger(blockKey);
            if (islandBlockLimit.compareTo(BigInteger.ZERO) >= 0 &&
                    islandBlockCount.add(amountToDepositBig).compareTo(islandBlockLimit) > 0) {
                amountToDeposit = islandBlockLimit.subtract(islandBlockCount).intValue();
            } else {
                // Checking for the global block key
                Key globalKey = ((BaseKey<?>) blockKey).toGlobalKey();
                islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(globalKey));
                islandBlockCount = island.getBlockCountAsBigInteger(globalKey);
                if (islandBlockLimit.compareTo(BigInteger.ZERO) >= 0 &&
                        islandBlockCount.add(amountToDepositBig).compareTo(islandBlockLimit) > 0) {
                    amountToDeposit = islandBlockLimit.subtract(islandBlockCount).intValue();
                }
            }
        }

        if (amountToDeposit <= 0) {
            // Island/global limit reached ⇒ notify caller so it can refund
            removalData.ifRight(cb -> cb.accept(0));
            return InteractionResult.NOT_ENOUGH_BLOCKS;
        }

        BlockLimitReservations.Reservation limitReservation = BukkitExecutor.isFolia() && island != null ?
                BlockLimitReservations.reserveStacked(island, blockKey, amountToDeposit) : null;
        if (BukkitExecutor.isFolia() && island != null && limitReservation == null) {
            removalData.ifRight(cb -> cb.accept(0));
            return InteractionResult.NOT_ENOUGH_BLOCKS;
        }
        if (limitReservation != null)
            amountToDeposit = limitReservation.getAmount();

        try {
            int newStackedBlockAmount = blockAmount + amountToDeposit;

            if (onlinePlayer != null && !PluginEventsFactory.callBlockStackEvent(stackedBlock, onlinePlayer, blockAmount, newStackedBlockAmount)) {
                // Event cancelled ⇒ nothing deposited, request refund
                removalData.ifRight(cb -> cb.accept(0));
                return InteractionResult.EVENT_CANCELLED;
            }

            if (consumeBeforeNotifications && plugin.getStackedBlocks().getStackedBlockAmount(stackedBlockLocation) != blockAmount) {
                removalData.ifRight(cb -> cb.accept(0));
                return InteractionResult.EVENT_CANCELLED;
            }

            try {
                if (!plugin.getStackedBlocks().setStackedBlock(stackedBlockLocation, blockKey, newStackedBlockAmount)) {
                    // Failed to persist/update ⇒ request refund
                    removalData.ifRight(cb -> cb.accept(0));
                    return InteractionResult.GLITCHED_STACKED_BLOCK;
                }
            } catch (RuntimeException | Error error) {
                if (BukkitExecutor.isFolia() &&
                        plugin.getStackedBlocks().getStackedBlockAmount(stackedBlockLocation) == newStackedBlockAmount &&
                        blockKey.equals(plugin.getStackedBlocks().getStackedBlockKey(stackedBlockLocation))) {
                    consumeDepositedItems(onlinePlayer, removalData, amountToDeposit);
                    if (island != null)
                        island.handleBlockPlace(blockKey, amountToDeposit);
                }
                throw error;
            }

            if (consumeBeforeNotifications)
                consumeDepositedItems(onlinePlayer, removalData, amountToDeposit);

            if (island != null)
                island.handleBlockPlace(blockKey, amountToDeposit);

            plugin.getProviders().notifyStackedBlocksListeners(onlinePlayer == null ? superiorPlayer.asOfflinePlayer() : onlinePlayer,
                    stackedBlock, IStackedBlocksListener.Action.BLOCK_PLACE);

            if (!consumeBeforeNotifications)
                consumeDepositedItems(onlinePlayer, removalData, amountToDeposit);

            return InteractionResult.SUCCESS;
        } finally {
            if (limitReservation != null)
                limitReservation.close();
        }
    }

    private void consumeDepositedItems(@Nullable Player player,
                                       Either<EquipmentSlot, OnItemRemovalCallback> removalData, int amount) {
        removalData.ifRight(itemRemovalCallback -> itemRemovalCallback.accept(amount)).ifLeft(usedHand -> {
            if (player != null && player.getGameMode() != GameMode.CREATIVE)
                BukkitItems.removeHandItem(player, PlayerHand.of(usedHand), amount);
        });
    }

    private static KeyMap<Key> createBlockKeyTransformer() {
        KeyMap<Key> blockKeyTransformer = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);

        Material GLOWING_REDSTONE_ORE = EnumHelper.getEnum(Material.class, "GLOWING_REDSTONE_ORE");
        if (GLOWING_REDSTONE_ORE != null)
            blockKeyTransformer.put(Keys.of(GLOWING_REDSTONE_ORE), Keys.of(Material.REDSTONE_ORE));

        return KeyMaps.unmodifiableKeyMap(blockKeyTransformer);
    }

}
