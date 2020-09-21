package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.utils.key.Key;
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
        this.inventory = Bukkit.createInventory(this, 36, plugin.getSettings().stackedBlocksMenuTitle);
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

        Key blockKey = plugin.getGrid().getBlockKey(stackedBlock);
        Key itemKey = Key.of(itemToDeposit);

        if(!blockKey.equals(itemKey))
            e.setCancelled(true);
    }

    public void onClose(InventoryCloseEvent e){
        int depositAmount = 0;
        ItemStack blockItem = null;

        Key blockKey = plugin.getGrid().getBlockKey(stackedBlock);

        for(ItemStack itemStack : e.getInventory().getContents()){
            if(itemStack != null && itemStack.getType() != Material.AIR) {
                Key itemKey = Key.of(itemStack);
                if(blockKey.equals(itemKey)){
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
            boolean success = BlocksListener.tryStack(plugin, (Player) e.getPlayer(), depositAmount, stackedBlock, amount -> {
                int leftOvers = amount - DEPOSIT_AMOUNT;
                if(leftOvers > 0) {
                    ItemStack toDrop = BLOCK_ITEM.clone();
                    toDrop.setAmount(leftOvers);
                    stackedBlock.getWorld().dropItemNaturally(stackedBlock, toDrop);
                }
            });
            if(success)
                plugin.getNMSAdapter().playPlaceSound(stackedBlock);
        }
    }

}
