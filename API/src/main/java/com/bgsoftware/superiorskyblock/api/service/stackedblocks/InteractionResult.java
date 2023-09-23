package com.bgsoftware.superiorskyblock.api.service.stackedblocks;

import com.bgsoftware.superiorskyblock.api.events.BlockStackEvent;
import com.bgsoftware.superiorskyblock.api.events.BlockUnstackEvent;

public enum InteractionResult {

    /**
     * The stacked-blocks system is disabled.
     */
    STACKED_BLOCKS_DISABLED,

    /**
     * The world that the player tries to stack blocks in is disabled for stacking blocks.
     */
    DISABLED_WORLD,

    /**
     * The player has toggled off his ability to stack blocks.
     */
    PLAYER_STACKED_BLOCKS_DISABLED,

    /**
     * The used item to stack the block has a name or a lore, and cannot be used to stack blocks.
     */
    CUSTOMIZED_ITEM,

    /**
     * The item used by the player to stack blocks is different from the block.
     */
    PLAYER_HOLDING_DIFFERENT_ITEM,

    /**
     * The player does not have the permission to stack this type of block.
     */
    PLAYER_MISSING_PERMISSION,

    /**
     * The block cannot be stacked as it is not whitelisted.
     */
    STACKED_BLOCK_NOT_WHITELISTED,

    /**
     * The player does not have enough items in his inventory in order to stack the blocks.
     */
    NOT_ENOUGH_BLOCKS,

    /**
     * The event that had been fired ({@link BlockStackEvent}, {@link BlockUnstackEvent}) was cancelled.
     */
    EVENT_CANCELLED,

    /**
     * An unexpected behavior occurred and the plugin prevented the block from being stacked.
     */
    GLITCHED_STACKED_BLOCK,

    /**
     * The block that was being unstacked is not an actual stacked block.
     */
    NOT_STACKED_BLOCK,

    /**
     * The block is protected by another island.
     */
    STACKED_BLOCK_PROTECTED,

    /**
     * The block was successfully stacked or unstacked.
     */
    SUCCESS

}
