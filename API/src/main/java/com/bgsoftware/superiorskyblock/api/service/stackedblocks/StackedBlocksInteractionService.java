package com.bgsoftware.superiorskyblock.api.service.stackedblocks;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.block.Block;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface StackedBlocksInteractionService {

    /**
     * Handle a stacked block being placed.
     *
     * @param superiorPlayer The player that stacked the block.
     * @param block          The block to stack into.
     * @param usedHand       The hand that was used when placing the block.
     * @return The result of stacking the block.
     */
    InteractionResult handleStackedBlockPlace(SuperiorPlayer superiorPlayer, Block block, EquipmentSlot usedHand);

    /**
     * Handle a stacked block being placed.
     *
     * @param superiorPlayer      The player that stacked the block.
     * @param block               The block to stack into.
     * @param amountToDeposit     The amount of blocks that were stacked.
     * @param itemRemovalCallback Callback to remove items from the inventory of the player.
     * @return The result of stacking the block.
     */
    InteractionResult handleStackedBlockPlace(SuperiorPlayer superiorPlayer, Block block, int amountToDeposit,
                                              OnItemRemovalCallback itemRemovalCallback);

    /**
     * Check if a block can be stacked.
     *
     * @param superiorPlayer The player that stacked the block.
     * @param block          The block to stack into.
     * @param itemStack      The item that is used to stack into the block.
     * @return The result of stacking the block.
     */
    InteractionResult checkStackedBlockInteraction(SuperiorPlayer superiorPlayer, Block block, ItemStack itemStack);

    /**
     * Handle a stacked block being broken.
     *
     * @param block          The block to unstack from.
     * @param superiorPlayer The player that unstacked the block, if exists.
     * @return The result of unstacking the block.
     */
    InteractionResult handleStackedBlockBreak(Block block, @Nullable SuperiorPlayer superiorPlayer);

    /**
     * Callback for removing items from the inventory when stacking blocks.
     * Used in {@link #handleStackedBlockPlace(SuperiorPlayer, Block, int, OnItemRemovalCallback)}
     */
    interface OnItemRemovalCallback {

        void accept(int amount);

    }

}
