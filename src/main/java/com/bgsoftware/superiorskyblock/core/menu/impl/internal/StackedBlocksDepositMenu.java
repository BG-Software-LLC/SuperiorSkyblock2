package com.bgsoftware.superiorskyblock.core.menu.impl.internal;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.StackedBlocksInteractionService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class StackedBlocksDepositMenu implements InventoryHolder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<StackedBlocksInteractionService> stackedBlocksInteractionService = new LazyReference<StackedBlocksInteractionService>() {
        @Override
        protected StackedBlocksInteractionService create() {
            return plugin.getServices().getService(StackedBlocksInteractionService.class);
        }
    };

    private final Inventory inventory;
    private final Location stackedBlock;

    public StackedBlocksDepositMenu(Location stackedBlock) {
        this.inventory = Bukkit.createInventory(this, 36, plugin.getSettings().getStackedBlocks().getDepositMenu().getTitle());
        this.stackedBlock = stackedBlock;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void onInteract(InventoryClickEvent e) {
        ItemStack itemToDeposit = null;

        switch (e.getAction()) {
            case HOTBAR_SWAP:
                itemToDeposit = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                break;
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
                itemToDeposit = e.getCursor();
                break;
            case MOVE_TO_OTHER_INVENTORY:
                itemToDeposit = e.getCurrentItem();
                break;
        }

        if (itemToDeposit == null || itemToDeposit.getType() == Material.AIR)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getWhoClicked());
        InteractionResult interactionResult = stackedBlocksInteractionService.get().checkStackedBlockInteraction(
                superiorPlayer, stackedBlock.getBlock(), itemToDeposit);
        if (interactionResult != InteractionResult.SUCCESS)
            e.setCancelled(true);
    }

    public void onClose(InventoryCloseEvent e) {
        int depositAmount = 0;
        ItemStack blockItem = null;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Block block = stackedBlock.getBlock();

        for (ItemStack itemStack : e.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                InteractionResult interactionResult = stackedBlocksInteractionService.get()
                        .checkStackedBlockInteraction(superiorPlayer, block, itemStack);
                if (interactionResult == InteractionResult.SUCCESS) {
                    depositAmount += itemStack.getAmount();
                    blockItem = itemStack;
                } else {
                    stackedBlock.getWorld().dropItemNaturally(stackedBlock, itemStack);
                }
            }
        }

        if (depositAmount > 0) {
            int finalDepositAmount = depositAmount;
            ItemStack finalBlockItem = blockItem;

            InteractionResult interactionResult = stackedBlocksInteractionService.get().
                    handleStackedBlockPlace(superiorPlayer, block, finalDepositAmount, amount -> {
                        int leftOvers = finalDepositAmount - amount;
                        if (leftOvers > 0) {
                            ItemStack toAddBack = finalBlockItem.clone();
                            toAddBack.setAmount(leftOvers);
                            BukkitItems.addItem(toAddBack, e.getPlayer().getInventory(), stackedBlock);
                        }
                    });

            if (interactionResult == InteractionResult.SUCCESS)
                plugin.getNMSWorld().playPlaceSound(stackedBlock);
        }
    }

}
