package com.bgsoftware.superiorskyblock.utils.items;

import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SpawnEggMeta;

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

    public static EntityType getEntityType(ItemStack itemStack){
        if(!isValidAndSpawnEgg(itemStack))
            return itemStack.getType() == Material.ARMOR_STAND ? EntityType.ARMOR_STAND : EntityType.UNKNOWN;

        if(ServerVersion.isLegacy()) {
            try {
                SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemStack.getItemMeta();
                return spawnEggMeta.getSpawnedType() == null ? EntityType.PIG : spawnEggMeta.getSpawnedType();
            } catch (NoClassDefFoundError error) {
                return EntityType.fromId(itemStack.getDurability());
            }
        }else{
            return EntityType.fromName(itemStack.getType().name().replace("_SPAWN_EGG", ""));
        }
    }

    private static boolean isValidAndSpawnEgg(ItemStack itemStack){
        return !itemStack.getType().isBlock() && itemStack.getType().name().contains(ServerVersion.isLegacy() ? "MONSTER_EGG" : "SPAWN_EGG");
    }

    public static int getLightLevel(Material material){
        switch (material.name()){
            case "BEACON":
            case "ENDER_PORTAL":
            case "END_PORTAL":
            case "END_GATEWAY":
            case "FIRE":
            case "LAVA":
            case "STATIONARY_LAVA":
            case "GLOWSTONE":
            case "JACK_O_LANTERN":
            case "REDSTONE_LAMP":
            case "REDSTONE_LAMP_ON":
            case "SEA_LANTERN":
            case "CONDUIT":
            case "LANTERN":
            case "CAMPFIRE":
                return 15;
            case "END_ROD":
            case "TORCH":
                return 14;
            case "FURNACE":
            case "BLAST_FURNACE":
            case "SMOKER":
                return 13;
            case "NETHER_PORTAL":
                return 11;
            case "ENDER_CHEST":
            case "REDSTONE_TORCH":
            case "REDSTONE_WALL_TORCH":
            case "REDSTONE_TORCH_ON":
                return 7;
            case "MAGMA_BLOCK":
                return 3;
            case "BREWING_STAND":
            case "BROWN_MUSHROOM":
            case "DRAGON_EGG":
            case "ENDER_PORTAL_FRAME":
            case "END_PORTAL_FRAME":
                return 1;
            default:
                return 0;
        }
    }

}
