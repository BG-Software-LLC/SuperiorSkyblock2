package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EntitiesProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitEntities {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<UUID, List<ItemStack>> entityContent = new ConcurrentHashMap<>();
    @Nullable
    private static final EntityType CAMEL_TYPE = EnumHelper.getEnum(EntityType.class, "CAMEL");

    private static final EnumMap<EntityType, EntityCategory> ENTITY_CATEGORIES_CACHE = new EnumMap<>(EntityType.class);

    static {
        outerLoop:
        for (EntityType entityType : EntityType.values()) {
            for (EntityCategory entityCategory : EntityCategory.values()) {
                if (entityCategory.isFromCategory(entityType)) {
                    ENTITY_CATEGORIES_CACHE.put(entityType, entityCategory);
                    continue outerLoop;
                }
            }
        }
    }

    private BukkitEntities() {

    }

    public static boolean isEquipment(LivingEntity livingEntity, ItemStack itemStack) {
        List<ItemStack> entityEquipment = entityContent.computeIfAbsent(livingEntity.getUniqueId(), u ->
                cacheEntityEquipmentInternal(livingEntity));

        return entityEquipment.stream().anyMatch(equipmentItem -> equipmentItem != null &&
                equipmentItem.getType() == itemStack.getType());
    }

    public static void cacheEntityEquipment(LivingEntity livingEntity) {
        List<ItemStack> entityEquipment = cacheEntityEquipmentInternal(livingEntity);
        entityContent.put(livingEntity.getUniqueId(), entityEquipment);
    }

    private static List<ItemStack> cacheEntityEquipmentInternal(LivingEntity livingEntity) {
        List<ItemStack> entityEquipment = new LinkedList<>(Arrays.asList(plugin.getNMSEntities().getEquipment(livingEntity.getEquipment())));

        if (livingEntity instanceof Pig) {
            if (((Pig) livingEntity).hasSaddle())
                entityEquipment.add(new ItemStack(Material.SADDLE));
        } else if (livingEntity instanceof Horse) {
            HorseInventory horseInventory = ((Horse) livingEntity).getInventory();
            horseInventory.forEach(itemStack -> {
                if (itemStack != null)
                    entityEquipment.add(itemStack);
            });

            entityEquipment.add(new ItemStack(Material.CHEST));

            if (horseInventory.getSaddle() != null)
                entityEquipment.add(horseInventory.getSaddle());
            if (horseInventory.getArmor() != null)
                entityEquipment.add(horseInventory.getArmor());
        }

        try {
            if (livingEntity instanceof AbstractHorse) {
                AbstractHorseInventory horseInventory = ((AbstractHorse) livingEntity).getInventory();
                horseInventory.forEach(itemStack -> {
                    if (itemStack != null)
                        entityEquipment.add(itemStack);
                });

                entityEquipment.add(new ItemStack(Material.CHEST));

                if (horseInventory.getSaddle() != null)
                    entityEquipment.add(horseInventory.getSaddle());
                if (horseInventory instanceof HorseInventory && ((HorseInventory) horseInventory).getArmor() != null)
                    entityEquipment.add(((HorseInventory) horseInventory).getArmor());
            }
        } catch (Throwable ignored) {
        }

        return entityEquipment.isEmpty() ? Collections.emptyList() : entityEquipment;
    }

    public static void clearEntityEquipment(LivingEntity livingEntity) {
        entityContent.remove(livingEntity.getUniqueId());
    }

    public static Optional<Player> getPlayerSource(Entity damager) {
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player)
                return Optional.of((Player) shooter);
        } else if (damager instanceof Player) {
            return Optional.of((Player) damager);
        }

        return Optional.empty();
    }

    public static Key getLimitEntityType(Entity entity) {
        // TODO - Is this really necessary?
        return Keys.of(entity.getType());
    }

    public static boolean canHaveLimit(EntityType entityType) {
        Class<?> entityClass = entityType.getEntityClass();
        return (entityClass != null && (LivingEntity.class.isAssignableFrom(entityClass) ||
                Hanging.class.isAssignableFrom(entityClass) || Vehicle.class.isAssignableFrom(entityClass)));
    }

    public static boolean canBypassEntityLimit(Entity entity) {
        return canBypassEntityLimit(entity, true);
    }

    public static boolean canBypassEntityLimit(Entity entity, boolean checkProviders) {
        if (entity instanceof ArmorStand && !((ArmorStand) entity).isVisible())
            return true;

        if (checkProviders) {
            for (EntitiesProvider entitiesProvider : plugin.getProviders().getEntitiesProviders()) {
                if (!entitiesProvider.shouldTrackEntity(entity))
                    return true;
            }
        }

        return false;
    }

    public static EntityCategory getCategory(EntityType entityType) {
        return ENTITY_CATEGORIES_CACHE.getOrDefault(entityType, EntityCategory.UNKNOWN);
    }

    public static boolean isTameable(Entity entity) {
        return entity instanceof Tameable && ((Tameable) entity).isTamed();
    }

    public static boolean isHorse(Entity entity) {
        return entity instanceof Horse || entity.getType() == CAMEL_TYPE ||
                (ServerVersion.isAtLeast(ServerVersion.v1_11) && (entity instanceof Mule || entity instanceof Donkey));
    }

}
