package com.bgsoftware.superiorskyblock.utils.items;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.tags.TagUtils;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.Map;

public final class ItemUtils {

    private static final ReflectMethod<EquipmentSlot> GET_HAND_BLOCK_PLACE = new ReflectMethod<>(BlockPlaceEvent.class, "getHand");
    private static final ReflectMethod<EquipmentSlot> GET_HAND_PLAYER_INTERACT = new ReflectMethod<>(PlayerInteractEvent.class, "getHand");
    private static final ReflectMethod<ItemStack> GET_ITEM_IN_OFF_HAND = new ReflectMethod<>(PlayerInventory.class, "getItemInOffHand");
    private static final ReflectMethod<ItemStack> SET_ITEM_IN_OFF_HAND = new ReflectMethod<>(PlayerInventory.class, "setItemInOffHand", ItemStack.class);

    private ItemUtils(){

    }

    public static void removeItem(ItemStack itemStack, Event event, Player player){
        ReflectMethod<EquipmentSlot> reflectMethod = null;

        if(event instanceof BlockPlaceEvent)
            reflectMethod = GET_HAND_BLOCK_PLACE;
        else if(event instanceof PlayerInteractEvent)
            reflectMethod = GET_HAND_PLAYER_INTERACT;

        if(reflectMethod != null && reflectMethod.isValid()){
            EquipmentSlot equipmentSlot = reflectMethod.invoke(event);
            if(equipmentSlot.name().equals("OFF_HAND")){
                ItemStack offHand = GET_ITEM_IN_OFF_HAND.invoke(player.getInventory());
                if(offHand.isSimilar(itemStack)){
                    offHand.setAmount(offHand.getAmount() - itemStack.getAmount());
                    SET_ITEM_IN_OFF_HAND.invoke(player.getInventory(), offHand);
                    return;
                }
            }
        }

        player.getInventory().removeItem(itemStack);
    }

    public static void setItem(ItemStack itemStack, Event event, Player player){
        ReflectMethod<EquipmentSlot> reflectMethod = null;

        if(event instanceof BlockPlaceEvent)
            reflectMethod = GET_HAND_BLOCK_PLACE;
        else if(event instanceof PlayerInteractEvent)
            reflectMethod = GET_HAND_PLAYER_INTERACT;

        if(reflectMethod != null && reflectMethod.isValid()){
            EquipmentSlot equipmentSlot = reflectMethod.invoke(event);
            if(equipmentSlot != null && equipmentSlot.name().equals("OFF_HAND")){
                player.getInventory().setItem(40, itemStack);
                return;
            }
        }

        player.getInventory().setItemInHand(itemStack);
    }

    @SuppressWarnings("deprecation")
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

    public static void addItem(ItemStack itemStack, PlayerInventory playerInventory, Location toDrop){
        Map<Integer, ItemStack> additionalItems = playerInventory.addItem(itemStack);
        for(ItemStack additionalItem : additionalItems.values())
            toDrop.getWorld().dropItemNaturally(toDrop, additionalItem);
    }

    public static String serialize(ItemStack[] contents){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.setInt("Length", contents.length);

        for(int i = 0; i < contents.length; i++) {
            if(contents[i] != null && contents[i].getType() != Material.AIR)
                compoundTag.setTag(i + "", TagUtils.itemToCompound(contents[i]));
        }

        try {
            compoundTag.write(dataOutput);
        }catch (Exception ex){
            ex.printStackTrace();
            return "";
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    public static String serializeItem(ItemStack itemStack){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        try {
            TagUtils.itemToCompound(itemStack).write(dataOutput);
        }catch (Exception ex){
            ex.printStackTrace();
            return "";
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    public static ItemStack[] deserialize(String serialized){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());
        CompoundTag compoundTag;

        try {
            compoundTag = (CompoundTag) Tag.fromStream(new DataInputStream(inputStream), 0);
        }catch (Exception ex){
            ex.printStackTrace();
            return new ItemStack[0];
        }

        ItemStack[] contents = new ItemStack[compoundTag.getInt("Length")];

        for(int i = 0; i < contents.length; i++) {
            CompoundTag itemCompound = compoundTag.getCompound(i + "");
            if(itemCompound != null)
                contents[i] = TagUtils.compoundToItem(itemCompound);
        }

        return contents;
    }

    public static ItemStack deserializeItem(String serialized){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try{
            CompoundTag compoundTag = (CompoundTag) Tag.fromStream(new DataInputStream(inputStream), 0);
            return TagUtils.compoundToItem(compoundTag);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    public static boolean isValidAndSpawnEgg(ItemStack itemStack){
        return !itemStack.getType().isBlock() && itemStack.getType().name().contains(ServerVersion.isLegacy() ? "MONSTER_EGG" : "SPAWN_EGG");
    }

}
