package com.bgsoftware.superiorskyblock.utils.entities;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.key.Key;
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
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class EntityUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<UUID, List<ItemStack>> entityContent = new HashMap<>();

    private EntityUtils(){

    }

    public static boolean isEquipment(LivingEntity livingEntity, ItemStack itemStack){
        List<ItemStack> entityEquipment = entityContent.get(livingEntity.getUniqueId());

        if(entityEquipment == null) {
            cacheEntityEquipment(livingEntity);
            entityEquipment = entityContent.get(livingEntity.getUniqueId());
        }

        return entityEquipment.stream().anyMatch(equipmentItem -> equipmentItem != null &&
                equipmentItem.getType() == itemStack.getType());
    }

    public static void cacheEntityEquipment(LivingEntity livingEntity){
        List<ItemStack> entityEquipment = new ArrayList<>(Arrays.asList(plugin.getNMSEntities().getEquipment(livingEntity.getEquipment())));

        if(livingEntity instanceof Pig){
            if(((Pig) livingEntity).hasSaddle())
                entityEquipment.add(new ItemStack(Material.SADDLE));
        }
        else if(livingEntity instanceof Horse){
            HorseInventory horseInventory = ((Horse) livingEntity).getInventory();
            entityEquipment.addAll(Arrays.stream(horseInventory.getContents()).filter(Objects::nonNull).collect(Collectors.toList()));

            entityEquipment.add(new ItemStack(Material.CHEST));

            if(horseInventory.getSaddle() != null)
                entityEquipment.add(horseInventory.getSaddle());
            if(horseInventory.getArmor() != null)
                entityEquipment.add(horseInventory.getArmor());
        }

        try{
            if(livingEntity instanceof AbstractHorse){
                AbstractHorseInventory horseInventory = ((AbstractHorse) livingEntity).getInventory();
                entityEquipment.addAll(Arrays.stream(horseInventory.getContents()).filter(Objects::nonNull).collect(Collectors.toList()));

                entityEquipment.add(new ItemStack(Material.CHEST));

                if(horseInventory.getSaddle() != null)
                    entityEquipment.add(horseInventory.getSaddle());
                if(horseInventory instanceof HorseInventory && ((HorseInventory) horseInventory).getArmor() != null)
                    entityEquipment.add(((HorseInventory) horseInventory).getArmor());
            }
        }catch(Throwable ignored){}

        entityContent.put(livingEntity.getUniqueId(), entityEquipment);
    }

    public static void clearEntityEquipment(LivingEntity livingEntity){
        entityContent.remove(livingEntity.getUniqueId());
    }

    public static SuperiorPlayer getPlayerDamager(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Projectile){
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if(shooter instanceof Player)
                return plugin.getPlayers().getSuperiorPlayer((Player) shooter);
        }
        else if(event.getDamager() instanceof Player){
            return plugin.getPlayers().getSuperiorPlayer((Player) event.getDamager());
        }

        return null;
    }

    public static Key getLimitEntityType(Entity entity){
        Key key = Key.of(entity.getType());
        return key.getGlobalKey().contains("MINECART") ?
                Key.of("MINECART" + (key.getSubKey().isEmpty() ? "" : ":" + key.getSubKey())) : key;
    }

    public static boolean canHaveLimit(EntityType entityType){
        Class<?> entityClass = entityType.getEntityClass();
        return entityType.name().contains("MINECART") || (entityClass != null &&
                (LivingEntity.class.isAssignableFrom(entityClass) || Hanging.class.isAssignableFrom(entityClass)));
    }

    public static boolean canBypassEntityLimit(Entity entity){
        return entity instanceof ArmorStand && !((ArmorStand) entity).isVisible();
    }

    public static boolean isMonster(EntityType entityType){
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        return entityClass != null && (Monster.class.isAssignableFrom(entityClass) ||
                // Checks for slimes & magma cubes
                Slime.class.isAssignableFrom(entityClass) ||
                // Checks for ghasts and phantoms
                Flying.class.isAssignableFrom(entityClass));
    }

    public static boolean isAnimal(EntityType entityType){
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        return entityClass != null && (Creature.class.isAssignableFrom(entityType.getEntityClass()) ||
                Ambient.class.isAssignableFrom(entityType.getEntityClass()));
    }

}
