package com.bgsoftware.superiorskyblock.utils.items;

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

@SuppressWarnings("deprecation")
public final class ItemUtils {

    private ItemUtils(){

    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void removeItem(ItemStack itemStack, Event event, Player player){
        try{
            EquipmentSlot equipmentSlot = (EquipmentSlot) BlockPlaceEvent.class.getMethod("getHand").invoke(event);
            if(equipmentSlot.name().equals("OFF_HAND")){
                ItemStack offHand = (ItemStack) PlayerInventory.class.getMethod("getItemInOffHand").invoke(player.getInventory());
                if(offHand.isSimilar(itemStack)){
                    offHand.setAmount(offHand.getAmount() - itemStack.getAmount());
                    PlayerInventory.class.getMethod("setItemInOffHand", ItemStack.class)
                            .invoke(player.getInventory(), offHand);
                    return;
                }
            }
        }catch(Exception ignored){}

        player.getInventory().removeItem(itemStack);
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void setItem(ItemStack itemStack, Event event, Player player){
        try{
            EquipmentSlot equipmentSlot = (EquipmentSlot) PlayerInteractEvent.class.getMethod("getHand").invoke(event);
            if(equipmentSlot.name().equals("OFF_HAND")){
                player.getInventory().setItem(40, itemStack);
                return;
            }
        }catch(Exception ignored){}

        player.getInventory().setItemInHand(itemStack);
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

    private static boolean isValidAndSpawnEgg(ItemStack itemStack){
        return !itemStack.getType().isBlock() && itemStack.getType().name().contains(ServerVersion.isLegacy() ? "MONSTER_EGG" : "SPAWN_EGG");
    }

}
