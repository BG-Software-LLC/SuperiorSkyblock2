package com.bgsoftware.superiorskyblock.api.hooks.listener;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

/**
 * Listener for changes of stacked-blocks
 */
public interface IStackedBlocksListener {

    /**
     * Record a block-action related to stacked-blocks.
     *
     * @param offlinePlayer The player that interacted with the stacked-block.
     * @param block The stacked-block
     * @param action The action that was performed.
     */
    void recordBlockAction(OfflinePlayer offlinePlayer, Block block, Action action);

    enum Action {

        BLOCK_PLACE,
        BLOCK_BREAK

    }

}
