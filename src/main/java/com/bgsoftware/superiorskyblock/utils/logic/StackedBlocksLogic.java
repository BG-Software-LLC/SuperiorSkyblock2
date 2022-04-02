package com.bgsoftware.superiorskyblock.utils.logic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IStackedBlocksListener;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class StackedBlocksLogic {

    @Nullable
    private static final Material CAULDRON_ITEM = Materials.getMaterialSafe("CAULDRON_ITEM");

    @SuppressWarnings("unchecked")
    private static final Map<Material, Material> AGAINST_BLOCK_CHANGE_MATERIAL = buildImmutableMap(
            new Pair<>(Materials.getMaterialSafe("GLOWING_REDSTONE_ORE"), Material.REDSTONE_ORE)
    );
    private static final Set<Material> DATA_REMOVAL_MATERIALS = buildImmutableSet(
            Materials.END_PORTAL_FRAME.toBukkitType(),
            Materials.getMaterialSafe("PRISMARINE_BRICKS"),
            Materials.getMaterialSafe("DARK_PRISMARINE")
    );

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private StackedBlocksLogic() {
    }

    public static boolean canStackBlocks(Player player, ItemStack placeItem, Block againstBlock, BlockState replaceState) {
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

        //noinspection deprecation
        byte blockData = againstBlock.getData();
        Material blockType = againstBlock.getType();

        if (CAULDRON_ITEM != null && blockType == Material.CAULDRON && CAULDRON_ITEM == placeItem.getType()) {
            blockType = CAULDRON_ITEM;
        }

        if (DATA_REMOVAL_MATERIALS.contains(blockType)) {
            blockData = 0;
        }

        if (blockType != placeItem.getType() || blockData != placeItem.getDurability() ||
                (replaceState != null && replaceState.getType() != Material.AIR))
            return false;

        if (!plugin.getSettings().getStackedBlocks().getWhitelisted().contains(KeyImpl.of(againstBlock)))
            return false;

        return superiorPlayer.hasPermission("superior.island.stacker.*") ||
                superiorPlayer.hasPermission("superior.island.stacker." + placeItem.getType());
    }

    public static boolean tryStack(Player player, ItemStack itemToDeposit, Location stackedBlock, Event event) {
        return tryStack(plugin, player, !player.isSneaking() ? 1 : itemToDeposit.getAmount(), stackedBlock, depositedAmount -> {
            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack inHand = itemToDeposit.clone();
                inHand.setAmount(depositedAmount);
                ItemUtils.removeItem(inHand, event, player);
            }
        });
    }

    public static boolean tryStack(SuperiorSkyblockPlugin plugin, Player player, int amount, Location stackedBlock, Consumer<Integer> depositedAmount) {
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

        if (!EventsCaller.callBlockStackEvent(block, player, blockAmount, blockAmount + amount)) {
            depositedAmount.accept(0);
            return false;
        }

        Island island = plugin.getGrid().getIslandAt(stackedBlock);

        if (island != null) {
            BigInteger islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(blockKey));
            BigInteger islandBlockCount = island.getBlockCountAsBigInteger(blockKey);
            BigInteger bigAmount = BigInteger.valueOf(amount);

            //Checking for the specific provided key.
            if (islandBlockLimit.compareTo(BigInteger.valueOf(IslandUtils.NO_LIMIT.get())) > 0 &&
                    islandBlockCount.add(bigAmount).compareTo(islandBlockLimit) > 0) {
                amount = islandBlockLimit.subtract(islandBlockCount).intValue();
            } else {
                //Getting the global key values.
                Key globalKey = KeyImpl.of(blockKey.getGlobalKey());
                islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(globalKey));
                islandBlockCount = island.getBlockCountAsBigInteger(globalKey);
                if (islandBlockLimit.compareTo(BigInteger.valueOf(IslandUtils.NO_LIMIT.get())) > 0 &&
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

    public static boolean tryUnstack(Player player, Block block, SuperiorSkyblockPlugin plugin) {
        int blockAmount = plugin.getStackedBlocks().getStackedBlockAmount(block);

        if (blockAmount <= 1)
            return false;

        // When sneaking, you'll break 64 from the stack. Otherwise, 1.
        int amount = player == null || !player.isSneaking() ? 1 : 64;

        // Fix amount so it won't be more than the stack's amount
        amount = Math.min(amount, blockAmount);

        if (!EventsCaller.callBlockUnstackEvent(block, player, blockAmount, blockAmount - amount))
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
            ItemUtils.addItem(blockItem, player.getInventory(), block.getLocation());
        } else {
            block.getWorld().dropItemNaturally(block.getLocation(), blockItem);
        }

        return true;
    }

    private static Map<Material, Material> buildImmutableMap(Pair<Material, Material>... materials) {
        ImmutableMap.Builder<Material, Material> builder = new ImmutableMap.Builder<>();
        for (Pair<Material, Material> material : materials) {
            if (material.getKey() != null && material.getValue() != null)
                builder.put(material.getKey(), material.getValue());
        }
        return builder.build();
    }

    private static Set<Material> buildImmutableSet(Material... materials) {
        ImmutableSet.Builder<Material> builder = new ImmutableSet.Builder<>();
        for (Material material : materials) {
            if (material != null)
                builder.add(material);
        }
        return builder.build();
    }

}
