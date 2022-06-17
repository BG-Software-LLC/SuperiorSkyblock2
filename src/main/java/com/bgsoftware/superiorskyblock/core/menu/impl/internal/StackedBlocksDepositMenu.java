package com.bgsoftware.superiorskyblock.core.menu.impl.internal;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.listener.StackedBlocksListener;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class StackedBlocksDepositMenu implements InventoryHolder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Singleton<StackedBlocksListener> stackedBlocksListener = plugin.getListener(StackedBlocksListener.class);

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

        if (!stackedBlocksListener.get().canStackBlocks((Player) e.getWhoClicked(), itemToDeposit, stackedBlock.getBlock()))
            e.setCancelled(true);
    }

    public void onClose(InventoryCloseEvent e) {
        int depositAmount = 0;
        ItemStack blockItem = null;

        for (ItemStack itemStack : e.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                if (stackedBlocksListener.get().canStackBlocks((Player) e.getPlayer(), itemStack, stackedBlock.getBlock())) {
                    depositAmount += itemStack.getAmount();
                    blockItem = itemStack;
                } else {
                    stackedBlock.getWorld().dropItemNaturally(stackedBlock, itemStack);
                }
            }
        }

        if (depositAmount > 0) {
            int DEPOSIT_AMOUNT = depositAmount;
            ItemStack BLOCK_ITEM = blockItem;
            boolean success = stackedBlocksListener.get().tryStack((Player) e.getPlayer(), depositAmount, stackedBlock, amount -> {
                int leftOvers = DEPOSIT_AMOUNT - amount;
                if (leftOvers > 0) {
                    ItemStack toAddBack = BLOCK_ITEM.clone();
                    toAddBack.setAmount(leftOvers);
                    BukkitItems.addItem(toAddBack, e.getPlayer().getInventory(), stackedBlock);
                }
            });
            if (success) {
                plugin.getNMSWorld().playPlaceSound(stackedBlock);
            }
        }
    }

}
