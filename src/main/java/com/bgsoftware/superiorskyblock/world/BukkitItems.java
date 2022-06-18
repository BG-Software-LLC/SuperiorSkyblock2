package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.Map;

public class BukkitItems {

    private static final ReflectMethod<EquipmentSlot> GET_HAND_BLOCK_PLACE = new ReflectMethod<>(BlockPlaceEvent.class, "getHand");
    private static final ReflectMethod<EquipmentSlot> GET_HAND_PLAYER_INTERACT = new ReflectMethod<>(PlayerInteractEvent.class, "getHand");
    private static final ReflectMethod<ItemStack> GET_ITEM_IN_OFF_HAND = new ReflectMethod<>(PlayerInventory.class, "getItemInOffHand");
    private static final ReflectMethod<ItemStack> SET_ITEM_IN_OFF_HAND = new ReflectMethod<>(PlayerInventory.class, "setItemInOffHand", ItemStack.class);

    private BukkitItems() {

    }

    public static boolean removeItemFromHand(ItemStack itemStack, Event event, Player player) {
        ReflectMethod<EquipmentSlot> reflectMethod = null;

        if (event instanceof BlockPlaceEvent) {
            reflectMethod = GET_HAND_BLOCK_PLACE;
        } else if (event instanceof PlayerInteractEvent) {
            reflectMethod = GET_HAND_PLAYER_INTERACT;
        }

        if (reflectMethod == null || !reflectMethod.isValid())
            return false;

        EquipmentSlot equipmentSlot = reflectMethod.invoke(event);

        if (equipmentSlot == EquipmentSlot.HAND) {
            ItemStack mainHand = player.getInventory().getItemInHand();
            if (mainHand.isSimilar(itemStack)) {
                mainHand.setAmount(mainHand.getAmount() - itemStack.getAmount());
                player.getInventory().setItemInHand(mainHand);
                return true;
            }
        } else if (equipmentSlot.name().equals("OFF_HAND")) {
            ItemStack offHand = GET_ITEM_IN_OFF_HAND.invoke(player.getInventory());
            if (offHand.isSimilar(itemStack)) {
                offHand.setAmount(offHand.getAmount() - itemStack.getAmount());
                SET_ITEM_IN_OFF_HAND.invoke(player.getInventory(), offHand);
                return true;
            }
        }

        return false;
    }

    public static void removeItem(ItemStack itemStack, Event event, Player player) {
        if (!removeItemFromHand(itemStack, event, player))
            player.getInventory().removeItem(itemStack);
    }

    public static void setItem(ItemStack itemStack, Event event, Player player) {
        ReflectMethod<EquipmentSlot> reflectMethod = null;

        if (event instanceof BlockPlaceEvent)
            reflectMethod = GET_HAND_BLOCK_PLACE;
        else if (event instanceof PlayerInteractEvent)
            reflectMethod = GET_HAND_PLAYER_INTERACT;

        if (reflectMethod != null && reflectMethod.isValid()) {
            EquipmentSlot equipmentSlot = reflectMethod.invoke(event);
            if (equipmentSlot != null && equipmentSlot.name().equals("OFF_HAND")) {
                player.getInventory().setItem(40, itemStack);
                return;
            }
        }

        player.getInventory().setItemInHand(itemStack);
    }

    @SuppressWarnings("deprecation")
    public static EntityType getEntityType(ItemStack itemStack) {
        if (!isValidAndSpawnEgg(itemStack))
            return itemStack.getType() == Material.ARMOR_STAND ? EntityType.ARMOR_STAND : EntityType.UNKNOWN;

        if (ServerVersion.isLegacy()) {
            try {
                SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemStack.getItemMeta();
                return spawnEggMeta.getSpawnedType() == null ? EntityType.PIG : spawnEggMeta.getSpawnedType();
            } catch (NoClassDefFoundError error) {
                return EntityType.fromId(itemStack.getDurability());
            }
        } else {
            return EntityType.fromName(itemStack.getType().name().replace("_SPAWN_EGG", ""));
        }
    }

    public static void addItem(ItemStack itemStack, PlayerInventory playerInventory, Location toDrop) {
        Map<Integer, ItemStack> additionalItems = playerInventory.addItem(itemStack);
        for (ItemStack additionalItem : additionalItems.values())
            toDrop.getWorld().dropItemNaturally(toDrop, additionalItem);
    }

    public static boolean isValidAndSpawnEgg(ItemStack itemStack) {
        return !itemStack.getType().isBlock() && itemStack.getType().name().contains(ServerVersion.isLegacy() ? "MONSTER_EGG" : "SPAWN_EGG");
    }

}
