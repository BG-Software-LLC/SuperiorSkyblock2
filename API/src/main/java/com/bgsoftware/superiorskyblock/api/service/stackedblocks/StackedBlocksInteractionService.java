package com.bgsoftware.superiorskyblock.api.service.stackedblocks;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.block.Block;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface StackedBlocksInteractionService {

    InteractionResult handleStackedBlockPlace(SuperiorPlayer superiorPlayer, Block block, EquipmentSlot usedHand);

    InteractionResult handleStackedBlockPlace(SuperiorPlayer superiorPlayer, Block block, int amountToDeposit,
                                              OnItemRemovalCallback itemRemovalCallback);

    InteractionResult checkStackedBlockInteraction(SuperiorPlayer superiorPlayer, Block block, ItemStack itemStack);

    InteractionResult handleStackedBlockBreak(Block block, @Nullable SuperiorPlayer superiorPlayer);

    interface OnItemRemovalCallback {

        void accept(int amount);

    }

}
