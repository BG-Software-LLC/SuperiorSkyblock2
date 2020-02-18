package com.bgsoftware.superiorskyblock.utils.entities;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class EntityUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<UUID, ItemStack[]> armorStandsContent = new HashMap<>();

    public static boolean isEquipment(LivingEntity livingEntity, ItemStack itemStack){
        if(livingEntity instanceof Pig){
            return ((Pig) livingEntity).hasSaddle() && itemStack.getType() == Material.SADDLE;
        }
        else if(livingEntity instanceof Horse){
            HorseInventory horseInventory = ((Horse) livingEntity).getInventory();
            List<ItemStack> itemStacks = Arrays.stream(horseInventory.getContents()).filter(Objects::nonNull).collect(Collectors.toList());
            if(horseInventory.getSaddle() != null)
                itemStacks.add(horseInventory.getSaddle());
            if(horseInventory.getArmor() != null)
                itemStacks.add(horseInventory.getArmor());
            return itemStacks.contains(itemStack);
        }
        else if(livingEntity instanceof ArmorStand){
            if(armorStandsContent.containsKey(livingEntity.getUniqueId())) {
                Bukkit.broadcastMessage(Arrays.asList(armorStandsContent.get(livingEntity.getUniqueId())) + "");
                return contains(armorStandsContent.get(livingEntity.getUniqueId()), itemStack);
            }
        }

        EntityEquipment entityEquipment = livingEntity.getEquipment();

        return contains(plugin.getNMSAdapter().getEquipment(entityEquipment), itemStack);
    }

    public static void cacheArmorStandEquipment(ArmorStand armorStand){
        armorStandsContent.put(armorStand.getUniqueId(), plugin.getNMSAdapter().getEquipment(armorStand.getEquipment()));
    }

    public static boolean isPlayerDamager(EntityDamageEvent e){
        if(e instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
            if(event.getDamager() instanceof Projectile){
                ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                return shooter instanceof Player;
            }
            else {
                return event.getDamager() instanceof Player;
            }
        }

        return false;
    }

    private static <T> boolean contains(T[] arr, T val){
        for(T element : arr){
            if(val.equals(element))
                return true;
        }

        return false;
    }

}
