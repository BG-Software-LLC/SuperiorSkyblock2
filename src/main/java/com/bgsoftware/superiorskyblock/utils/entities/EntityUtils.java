package com.bgsoftware.superiorskyblock.utils.entities;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EntityUtils {

    public static boolean isEquipment(LivingEntity livingEntity, ItemStack itemStack){
        if(livingEntity instanceof Pig){
            return ((Pig) livingEntity).hasSaddle() && itemStack.getType() == Material.SADDLE;
        }
        else if(livingEntity instanceof Horse){
            HorseInventory horseInventory = (HorseInventory) livingEntity;
            List<ItemStack> itemStacks = Arrays.stream(horseInventory.getContents()).filter(Objects::nonNull).collect(Collectors.toList());
            if(horseInventory.getSaddle() != null)
                itemStacks.add(horseInventory.getSaddle());
            if(horseInventory.getArmor() != null)
                itemStacks.add(horseInventory.getArmor());
            return itemStacks.contains(itemStack);
        }

        EntityEquipment entityEquipment = livingEntity.getEquipment();
        return contains(entityEquipment.getArmorContents(), itemStack) || itemStack.equals(entityEquipment.getItemInHand());
    }

    public static EntityDamageEvent.DamageCause getDamager(EntityDamageEvent e){
        if(e instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
            if(event.getDamager() instanceof Projectile){
                ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                if(shooter instanceof Player)
                    return EntityDamageEvent.DamageCause.ENTITY_ATTACK;
            }
        }

        return e.getCause();
    }

    private static <T> boolean contains(T[] arr, T val){
        for(T element : arr){
            if(val.equals(element))
                return true;
        }

        return false;
    }

}
