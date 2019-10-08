package com.bgsoftware.superiorskyblock.utils.items;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class ItemUtils {

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void removeItem(ItemStack itemStack, BlockPlaceEvent event){
        try{
            EquipmentSlot equipmentSlot = (EquipmentSlot) BlockPlaceEvent.class.getMethod("getHand").invoke(event);
            if(equipmentSlot.name().equals("OFF_HAND")){
                ItemStack offHand = (ItemStack) PlayerInventory.class.getMethod("getItemInOffHand").invoke(event.getPlayer().getInventory());
                if(offHand.isSimilar(itemStack)){
                    offHand.setAmount(offHand.getAmount() - itemStack.getAmount());
                    PlayerInventory.class.getMethod("setItemInOffHand", ItemStack.class)
                            .invoke(event.getPlayer().getInventory(), offHand);
                    return;
                }
            }
        }catch(Exception ignored){}

        event.getPlayer().getInventory().removeItem(itemStack);
    }

}
