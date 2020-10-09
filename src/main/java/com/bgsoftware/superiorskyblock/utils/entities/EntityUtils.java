package com.bgsoftware.superiorskyblock.utils.entities;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class EntityUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Registry<UUID, ItemStack[]> entityContent = Registry.createRegistry();

    private EntityUtils(){

    }

    public static boolean isEquipment(LivingEntity livingEntity, ItemStack itemStack){
        if(livingEntity instanceof Pig){
            return ((Pig) livingEntity).hasSaddle() && itemStack.getType() == Material.SADDLE;
        }
        else if(livingEntity instanceof Horse){
            HorseInventory horseInventory = ((Horse) livingEntity).getInventory();
            List<ItemStack> itemStacks = Arrays.stream(horseInventory.getContents()).filter(Objects::nonNull).collect(Collectors.toList());

            itemStacks.add(new ItemStack(Material.CHEST));

            if(horseInventory.getSaddle() != null)
                itemStacks.add(horseInventory.getSaddle());
            if(horseInventory.getArmor() != null)
                itemStacks.add(horseInventory.getArmor());

            return itemStacks.contains(itemStack);
        }

        try{
            if(livingEntity instanceof AbstractHorse){
                AbstractHorseInventory horseInventory = ((AbstractHorse) livingEntity).getInventory();
                List<ItemStack> itemStacks = Arrays.stream(horseInventory.getContents()).filter(Objects::nonNull).collect(Collectors.toList());

                itemStacks.add(new ItemStack(Material.CHEST));

                if(horseInventory.getSaddle() != null)
                    itemStacks.add(horseInventory.getSaddle());
                if(horseInventory instanceof HorseInventory && ((HorseInventory) horseInventory).getArmor() != null)
                    itemStacks.add(((HorseInventory) horseInventory).getArmor());

                return itemStacks.contains(itemStack);
            }
        }catch(Throwable ignored){}

        ItemStack[] entityEquipment = entityContent.get(livingEntity.getUniqueId());

        if(entityEquipment == null)
            entityEquipment = plugin.getNMSAdapter().getEquipment(livingEntity.getEquipment());

        return contains(entityEquipment, itemStack);
    }

    public static void cacheEntityEquipment(LivingEntity livingEntity){
        entityContent.add(livingEntity.getUniqueId(), plugin.getNMSAdapter().getEquipment(livingEntity.getEquipment()));
    }

    public static void clearEntityEquipment(LivingEntity livingEntity){
        entityContent.remove(livingEntity.getUniqueId());
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

    public static Key getLimitEntityType(Entity entity){
        Key key = Key.of(entity.getType());
        return key.getGlobalKey().contains("MINECART") ?
                Key.of("MINECART" + (key.getSubKey().isEmpty() ? "" : ":" + key.getSubKey())) : key;
    }

    public static EntityType getEntityTypeOrUnknown(com.bgsoftware.superiorskyblock.api.key.Key key){
        try{
            return EntityType.valueOf(key.toString());
        }catch (Exception ex){
            try{
                return EntityType.valueOf(key.getGlobalKey());
            }catch (Exception ignored) {}
        }

        return EntityType.UNKNOWN;
    }

    public static boolean canHaveLimit(EntityType entityType){
        Class<?> entityClass = entityType.getEntityClass();
        return entityType.name().contains("MINECART") || (entityClass != null &&
                (LivingEntity.class.isAssignableFrom(entityClass) || Hanging.class.isAssignableFrom(entityClass)));
    }

    public static boolean canBypassEntityLimit(Entity entity){
        return !(entity instanceof  ArmorStand) || !((ArmorStand) entity).isVisible();
    }

    public static boolean isMonster(EntityType entityType){
        return Monster.class.isAssignableFrom(entityType.getEntityClass()) ||
                // Checks for slimes & magma cubes
                Slime.class.isAssignableFrom(entityType.getEntityClass()) ||
                // Checks for ghasts and phantoms
                Flying.class.isAssignableFrom(entityType.getEntityClass());
    }

    public static boolean isAnimal(EntityType entityType){
        return Creature.class.isAssignableFrom(entityType.getEntityClass()) ||
                Ambient.class.isAssignableFrom(entityType.getEntityClass());
    }

    private static <T> boolean contains(T[] arr, T val){
        for(T element : arr){
            if(val.equals(element))
                return true;
        }

        return false;
    }

}
