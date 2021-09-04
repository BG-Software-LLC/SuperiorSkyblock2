package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.logic.StackedBlocksLogic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class StackedBlocksDepositMenu implements InventoryHolder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Inventory inventory;
    private final Location stackedBlock;

    public StackedBlocksDepositMenu(Location stackedBlock){
        this.inventory = Bukkit.createInventory(this, 36, plugin.getSettings().getStackedBlocks().getDepositMenu().getTitle());
        this.stackedBlock = stackedBlock;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void onInteract(InventoryClickEvent e){
        ItemStack itemToDeposit = null;

        switch (e.getAction()){
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

        if(itemToDeposit == null || itemToDeposit.getType() == Material.AIR)
            return;

        if(!StackedBlocksLogic.canStackBlocks((Player) e.getWhoClicked(), itemToDeposit, stackedBlock.getBlock(), null))
            e.setCancelled(true);
    }

    public void onClose(InventoryCloseEvent e){
        int depositAmount = 0;
        ItemStack blockItem = null;

        for(ItemStack itemStack : e.getInventory().getContents()){
            if(itemStack != null && itemStack.getType() != Material.AIR) {
                if(StackedBlocksLogic.canStackBlocks((Player) e.getPlayer(), itemStack, stackedBlock.getBlock(), null)){
                    depositAmount += itemStack.getAmount();
                    blockItem = itemStack;
                }
                else{
                    stackedBlock.getWorld().dropItemNaturally(stackedBlock, itemStack);
                }
            }
        }

        if(depositAmount > 0){
            int DEPOSIT_AMOUNT = depositAmount;
            ItemStack BLOCK_ITEM = blockItem;
            boolean success = StackedBlocksLogic.tryStack(plugin, (Player) e.getPlayer(), depositAmount, stackedBlock, amount -> {
                int leftOvers = DEPOSIT_AMOUNT - amount;
                if (leftOvers > 0) {
                    ItemStack toAddBack = BLOCK_ITEM.clone();
                    toAddBack.setAmount(leftOvers);
                    ItemUtils.addItem(toAddBack, e.getPlayer().getInventory(), stackedBlock);
                }
            });
            if (success) {
                plugin.getNMSWorld().playPlaceSound(stackedBlock);
            }
        }
    }

}
